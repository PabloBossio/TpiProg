package com.TPI.Programacion.IV.Service;

import com.TPI.Programacion.IV.DTO.PujaRequestDTO;
import com.TPI.Programacion.IV.DTO.SubastaResponseDTO;
import com.TPI.Programacion.IV.Model.*;
import com.TPI.Programacion.IV.Repository.CategoriaRepository;
import com.TPI.Programacion.IV.Repository.SubastaRepository;
import com.TPI.Programacion.IV.Repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Verifica el comportamiento ante pujas concurrentes (bloqueo optimista).
 * Se simula la excepción OptimisticLockingFailureException que Hibernate lanza
 * cuando dos transacciones intentan actualizar la misma entidad al mismo tiempo.
 */
@ExtendWith(MockitoExtension.class)
class SubastaConcurrenciaTest {

    @InjectMocks
    SubastaService service;

    @Mock
    SubastaRepository subastaRepository;
    @Mock
    CategoriaRepository categoryRepository;
    @Mock
    UsuarioRepository usuarioRepository;

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    // ───── Helper ─────────────────────────────────────────────────────────────

    private Subasta buildSubasta(long id, Usuario vendedor) {
        Categoria cat = new Categoria();
        cat.setId(1L);
        cat.setNombre("Electrónica");

        Producto prod = new Producto();
        prod.setId(id);
        prod.setNombre("Laptop");

        Subasta s = new Subasta();
        s.setId(1L);
        s.setEstado(EstadoSubasta.ACTIVA);
        s.setPrecioBase(BigDecimal.valueOf(1000));
        s.setMontoActual(BigDecimal.valueOf(1000));
        s.setIncrementoMinimo(BigDecimal.valueOf(100));
        s.setFechaInicio(LocalDateTime.now(ZoneOffset.UTC).minusHours(1));
        s.setFechaCierre(LocalDateTime.now(ZoneOffset.UTC).plusHours(3));
        s.setCategoria(cat);
        s.setProducto(prod);
        s.setVendedor(vendedor);
        s.setPujas(new ArrayList<>());       // lista independiente por instancia
        s.setHistorialEstados(new ArrayList<>());
        return s;
    }

    private void autenticarEnHilo(String username, String role) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        username, null,
                        List.of(new SimpleGrantedAuthority(role))));
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Test principal de concurrencia
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    void dosPujasSimultaneas_unaExitosaOtraConflictoDeVersion() throws Exception {

        Usuario vendedor = new Usuario();
        vendedor.setId(1L);
        vendedor.setNombreUsuario("vendedor1");

        Usuario comprador1 = new Usuario();
        comprador1.setId(2L);
        comprador1.setNombreUsuario("comprador1");
        comprador1.setEstaBloqueado(false);

        Usuario comprador2 = new Usuario();
        comprador2.setId(3L);
        comprador2.setNombreUsuario("comprador2");
        comprador2.setEstaBloqueado(false);

        // Cada hilo obtiene una instancia distinta de Subasta para evitar
        // ConcurrentModificationException en el ArrayList de pujas.
        Subasta subasta1 = buildSubasta(10L, vendedor);
        Subasta subasta2 = buildSubasta(11L, vendedor);

        AtomicInteger findCount = new AtomicInteger(0);
        when(subastaRepository.findById(1L)).thenAnswer(inv ->
                Optional.of(findCount.incrementAndGet() == 1 ? subasta1 : subasta2));

        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(comprador1));
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(comprador2));

        // Primera llamada a save(): devuelve la subasta (puja procesada con éxito).
        // Segunda llamada a save(): lanza OptimisticLockingFailureException (conflicto de versión).
        AtomicInteger saveCount = new AtomicInteger(0);
        when(subastaRepository.save(any())).thenAnswer(inv -> {
            if (saveCount.incrementAndGet() == 1) {
                return inv.getArgument(0);
            }
            throw new OptimisticLockingFailureException(
                    "Conflicto de versión: otro hilo ya guardó esta entidad.");
        });

        // CountDownLatch garantiza que ambos hilos empiecen al mismo tiempo
        CountDownLatch largada = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        Future<SubastaResponseDTO> futuro1 = executor.submit(() -> {
            largada.await();
            autenticarEnHilo("comprador1", "ROLE_COMPRADOR");
            return service.procesarPuja(1L, 2L, new PujaRequestDTO(BigDecimal.valueOf(1200)));
        });

        Future<SubastaResponseDTO> futuro2 = executor.submit(() -> {
            largada.await();
            autenticarEnHilo("comprador2", "ROLE_COMPRADOR");
            return service.procesarPuja(1L, 3L, new PujaRequestDTO(BigDecimal.valueOf(1300)));
        });

        largada.countDown(); // ¡Ya! Ambos hilos parten simultáneamente

        int exitos = 0;
        int conflictos = 0;

        for (Future<SubastaResponseDTO> f : List.of(futuro1, futuro2)) {
            try {
                f.get(5, TimeUnit.SECONDS);
                exitos++;
            } catch (ExecutionException e) {
                if (e.getCause() instanceof OptimisticLockingFailureException) {
                    conflictos++;
                }
            }
        }

        executor.shutdown();

        // Exactamente una puja debe haberse procesado y la otra rechazada por conflicto
        assertThat(exitos).as("Debe haber exactamente 1 puja exitosa").isEqualTo(1);
        assertThat(conflictos).as("Debe haber exactamente 1 conflicto de versión").isEqualTo(1);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Verifica que la excepción de optimistic locking se propague al llamador
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    void procesarPuja_cuandoSaveArrojaOptimisticLockException_propagaExcepcion() {
        autenticarEnHilo("comprador1", "ROLE_COMPRADOR");

        Usuario vendedor = new Usuario();
        vendedor.setId(1L);
        vendedor.setNombreUsuario("vendedor1");

        Usuario comprador = new Usuario();
        comprador.setId(2L);
        comprador.setNombreUsuario("comprador1");
        comprador.setEstaBloqueado(false);

        Subasta subasta = buildSubasta(1L, vendedor);

        when(subastaRepository.findById(1L)).thenReturn(Optional.of(subasta));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(comprador));
        when(subastaRepository.save(any()))
                .thenThrow(new OptimisticLockingFailureException("Versión desactualizada"));

        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> service.procesarPuja(1L, 2L, new PujaRequestDTO(BigDecimal.valueOf(1200))))
                .isInstanceOf(OptimisticLockingFailureException.class);
    }
}
