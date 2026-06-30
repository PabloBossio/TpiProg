package com.TPI.Programacion.IV.Service;

import com.TPI.Programacion.IV.DTO.ProductoRequestDTO;
import com.TPI.Programacion.IV.DTO.PujaRequestDTO;
import com.TPI.Programacion.IV.DTO.SubastaRequestDTO;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubastaServiceTest {

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

    // ───── Helpers ────────────────────────────────────────────────────────────

    private void autenticar(String username, String... roles) {
        List<SimpleGrantedAuthority> auths = new ArrayList<>();
        for (String r : roles) auths.add(new SimpleGrantedAuthority(r));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(username, null, auths));
    }

    private Subasta subastaBase(EstadoSubasta estado) {
        Categoria cat = new Categoria();
        cat.setId(1L);
        cat.setNombre("Rodados");

        Producto prod = new Producto();
        prod.setId(1L);
        prod.setNombre("Bici MTB");

        Usuario vendedor = new Usuario();
        vendedor.setId(1L);
        vendedor.setNombreUsuario("vendedor1");

        Subasta s = new Subasta();
        s.setId(1L);
        s.setEstado(estado);
        s.setPrecioBase(BigDecimal.valueOf(1000));
        s.setMontoActual(BigDecimal.valueOf(1000));
        s.setIncrementoMinimo(BigDecimal.valueOf(100));
        s.setFechaInicio(LocalDateTime.now(ZoneOffset.UTC));
        s.setFechaCierre(LocalDateTime.now(ZoneOffset.UTC).plusDays(3));
        s.setCategoria(cat);
        s.setProducto(prod);
        s.setVendedor(vendedor);
        s.setPujas(new ArrayList<>());
        return s;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // 1. Tests de Creación — Duración Máxima 14 días
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    void crearSubasta_duracionSuperiorA14Dias_lanzaIllegalArgumentException() {
        LocalDateTime inicio = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime cierre = inicio.plusDays(15); // excede el límite

        Categoria cat = new Categoria();
        cat.setId(1L);
        cat.setNombre("Test");

        Usuario vendedor = new Usuario();
        vendedor.setId(1L);
        vendedor.setNombreUsuario("vendedor1");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(cat));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(vendedor));

        SubastaRequestDTO req = new SubastaRequestDTO(
                BigDecimal.valueOf(1000), inicio, cierre,
                BigDecimal.valueOf(100), "desc", 1L,
                new ProductoRequestDTO("Laptop", null, null));

        assertThatThrownBy(() -> service.crearSubasta(req, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("máximo");

        verify(subastaRepository, never()).save(any());
    }

    @Test
    void crearSubasta_duracion14DiasExactos_permiteCreacion() {
        LocalDateTime inicio = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime cierre = inicio.plusWeeks(2); // exactamente 14 días

        Categoria cat = new Categoria();
        cat.setId(1L);
        cat.setNombre("Tech");

        Usuario vendedor = new Usuario();
        vendedor.setId(1L);
        vendedor.setNombreUsuario("vendedor1");

        Subasta guardada = subastaBase(EstadoSubasta.ACTIVA);
        guardada.setFechaInicio(inicio);
        guardada.setFechaCierre(cierre);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(cat));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(vendedor));
        when(subastaRepository.save(any())).thenReturn(guardada);
        autenticar("vendedor1", "ROLE_SELLER");

        SubastaRequestDTO req = new SubastaRequestDTO(
                BigDecimal.valueOf(1000), inicio, cierre,
                BigDecimal.valueOf(100), "desc", 1L,
                new ProductoRequestDTO("Laptop", null, null));

        assertThatCode(() -> service.crearSubasta(req, 1L)).doesNotThrowAnyException();
        verify(subastaRepository).save(any(Subasta.class));
    }

    // ═════════════════════════════════════════════════════════════════════════
    // 2. Tests de Pujas — Reglas de Negocio
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    void procesarPuja_subastaNoActiva_lanzaIllegalStateException() {
        Subasta subasta = subastaBase(EstadoSubasta.FINALIZADA);

        Usuario oferente = new Usuario();
        oferente.setId(2L);
        oferente.setNombreUsuario("comprador1");
        oferente.setEstaBloqueado(false);

        when(subastaRepository.findById(1L)).thenReturn(Optional.of(subasta));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(oferente));

        assertThatThrownBy(() -> service.procesarPuja(1L, 2L, new PujaRequestDTO(BigDecimal.valueOf(1200))))
                .isInstanceOf(IllegalStateException.class);

        verify(subastaRepository, never()).save(any());
    }

    @Test
    void procesarPuja_vendedorIntentaPujarEnSuPropia_lanzaIllegalStateException() {
        Subasta subasta = subastaBase(EstadoSubasta.ACTIVA);
        // El vendedor tiene id=1 y el oferente también tiene id=1
        Usuario vendedorComoOferente = subasta.getVendedor(); // id=1, nombre="vendedor1"
        vendedorComoOferente.setEstaBloqueado(false);

        when(subastaRepository.findById(1L)).thenReturn(Optional.of(subasta));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(vendedorComoOferente));

        assertThatThrownBy(() -> service.procesarPuja(1L, 1L, new PujaRequestDTO(BigDecimal.valueOf(1200))))
                .isInstanceOf(IllegalStateException.class);

        verify(subastaRepository, never()).save(any());
    }

    // ═════════════════════════════════════════════════════════════════════════
    // 3. Tests de Cancelación — Permisos y Reglas
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    void cancelarSubasta_sellerConPujas_lanzaIllegalStateException() {
        autenticar("vendedor1", "ROLE_SELLER");

        Subasta subasta = subastaBase(EstadoSubasta.ACTIVA);
        Puja puja = new Puja();
        puja.setMonto(BigDecimal.valueOf(1200));
        subasta.getPujas().add(puja); // la subasta ya tiene una puja

        when(subastaRepository.findById(1L)).thenReturn(Optional.of(subasta));

        assertThatThrownBy(() -> service.cancelarSubasta(1L, "me arrepentí"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("pujas");

        verify(subastaRepository, never()).save(any());
    }

    @Test
    void cancelarSubasta_sellerSinPujas_exitoso() {
        autenticar("vendedor1", "ROLE_SELLER");

        Subasta subasta = subastaBase(EstadoSubasta.ACTIVA);
        // sin pujas (ya inicializado vacío en subastaBase)

        when(subastaRepository.findById(1L)).thenReturn(Optional.of(subasta));
        when(usuarioRepository.findByNombreUsuario("vendedor1"))
                .thenReturn(Optional.of(subasta.getVendedor()));
        when(subastaRepository.save(any())).thenReturn(subasta);

        assertThatCode(() -> service.cancelarSubasta(1L, "cambié de idea"))
                .doesNotThrowAnyException();

        verify(subastaRepository).save(any(Subasta.class));
    }

    @Test
    void cancelarSubasta_adminConPujas_exitoso() {
        autenticar("superadmin", "ROLE_ADMIN");

        Usuario admin = new Usuario();
        admin.setId(99L);
        admin.setNombreUsuario("superadmin");

        Usuario pujador = new Usuario();
        pujador.setId(5L);
        pujador.setNombreUsuario("comprador2");

        Puja puja = new Puja();
        puja.setMonto(BigDecimal.valueOf(1500));
        puja.setFechaPuja(LocalDateTime.now(ZoneOffset.UTC));
        puja.setUsuario(pujador);

        Subasta subasta = subastaBase(EstadoSubasta.ACTIVA);
        subasta.getPujas().add(puja); // tiene puja activa

        when(subastaRepository.findById(1L)).thenReturn(Optional.of(subasta));
        when(usuarioRepository.findByNombreUsuario("superadmin")).thenReturn(Optional.of(admin));
        when(subastaRepository.save(any())).thenReturn(subasta);

        assertThatCode(() -> service.cancelarSubasta(1L, "fraude detectado"))
                .doesNotThrowAnyException();

        verify(subastaRepository).save(any(Subasta.class));
    }
}
