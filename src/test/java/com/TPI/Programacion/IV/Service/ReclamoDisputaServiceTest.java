package com.TPI.Programacion.IV.Service;

import com.TPI.Programacion.IV.DTO.ReclamoDisputaRequestDTO;
import com.TPI.Programacion.IV.Model.*;
import com.TPI.Programacion.IV.Repository.ReclamoDisputaRepository;
import com.TPI.Programacion.IV.Repository.SubastaRepository;
import com.TPI.Programacion.IV.Repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReclamoDisputaServiceTest {

    @InjectMocks
    ReclamoDisputaService service;

    @Mock
    ReclamoDisputaRepository reclamoRepository;
    @Mock
    SubastaRepository subastaRepository;
    @Mock
    UsuarioRepository usuarioRepository;

    // ───── Helper ─────────────────────────────────────────────────────────────

    private Subasta subastaAdjudicada(LocalDateTime fechaAdj, Usuario vendedor, Usuario ganador) {
        Subasta s = new Subasta();
        s.setId(1L);
        s.setEstado(EstadoSubasta.ADJUDICADA);
        s.setPrecioBase(BigDecimal.valueOf(1000));
        s.setMontoActual(BigDecimal.valueOf(1500));
        s.setIncrementoMinimo(BigDecimal.valueOf(100));
        s.setFechaInicio(LocalDateTime.now(ZoneOffset.UTC).minusDays(5));
        s.setFechaCierre(LocalDateTime.now(ZoneOffset.UTC).minusHours(26));
        s.setFechaAdjudicacion(fechaAdj);
        s.setVendedor(vendedor);
        s.setGanador(ganador);
        s.setPujas(new ArrayList<>());
        s.setHistorialEstados(new ArrayList<>());
        return s;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // 1. Disputa solo en subastas ADJUDICADAS
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    void crearReclamo_subastaNoAdjudicada_lanzaIllegalStateException() {
        Subasta subasta = new Subasta();
        subasta.setId(1L);
        subasta.setEstado(EstadoSubasta.ACTIVA); // estado incorrecto
        subasta.setFechaAdjudicacion(LocalDateTime.now(ZoneOffset.UTC).minusHours(1));

        Usuario demandante = new Usuario();
        demandante.setId(2L);

        when(subastaRepository.findById(1L)).thenReturn(Optional.of(subasta));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(demandante));

        ReclamoDisputaRequestDTO req = new ReclamoDisputaRequestDTO("Fraude", "Descripción", 1L);

        assertThatThrownBy(() -> service.crearReclamo(req, 2L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ADJUDICADA");
    }

    // ═════════════════════════════════════════════════════════════════════════
    // 2. Ventana de 24 horas para abrir la disputa
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    void crearReclamo_ventana24HorasExpirada_lanzaIllegalStateException() {
        Usuario vendedor = new Usuario();
        vendedor.setId(1L);
        vendedor.setNombreUsuario("vendedor1");

        Usuario ganador = new Usuario();
        ganador.setId(2L);
        ganador.setNombreUsuario("ganador1");

        // Adjudicada hace 25 horas — la ventana de 24h ya cerró
        LocalDateTime fechaAdj = LocalDateTime.now(ZoneOffset.UTC).minusHours(25);
        Subasta subasta = subastaAdjudicada(fechaAdj, vendedor, ganador);

        // El vendedor intenta abrir la disputa, pero pasó el plazo
        when(subastaRepository.findById(1L)).thenReturn(Optional.of(subasta));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(vendedor));

        ReclamoDisputaRequestDTO req = new ReclamoDisputaRequestDTO("Inconformidad", "El artículo no llegó", 1L);

        assertThatThrownBy(() -> service.crearReclamo(req, 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("24 horas");
    }

    @Test
    void crearReclamo_dentroDe24Horas_noLanzaExcepcionDeTiempo() {
        Usuario vendedor = new Usuario();
        vendedor.setId(1L);
        vendedor.setNombreUsuario("vendedor1");

        Usuario ganador = new Usuario();
        ganador.setId(2L);
        ganador.setNombreUsuario("ganador1");

        // Adjudicada hace solo 1 hora — dentro de la ventana válida
        LocalDateTime fechaAdj = LocalDateTime.now(ZoneOffset.UTC).minusHours(1);
        Subasta subasta = subastaAdjudicada(fechaAdj, vendedor, ganador);

        // El vendedor (id=1) abre la disputa — es parte legítima
        when(subastaRepository.findById(1L)).thenReturn(Optional.of(subasta));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(vendedor));

        ReclamoDisputa reclamoGuardado = new ReclamoDisputa();
        reclamoGuardado.setId(10L);
        reclamoGuardado.setMotivo("Inconformidad");
        reclamoGuardado.setDescripcion("El artículo no coincide");
        reclamoGuardado.setSubasta(subasta);
        reclamoGuardado.setUsuarioDemandante(vendedor);

        reclamoGuardado.setFechaCreacion(java.time.LocalDateTime.now(ZoneOffset.UTC));
        when(reclamoRepository.save(any())).thenReturn(reclamoGuardado);
        when(subastaRepository.save(any())).thenReturn(subasta);

        ReclamoDisputaRequestDTO req = new ReclamoDisputaRequestDTO("Inconformidad", "El artículo no coincide", 1L);
        assertThatCode(() -> service.crearReclamo(req, 1L))
                .doesNotThrowAnyException();
    }

    // ═════════════════════════════════════════════════════════════════════════
    // 3. Solo vendedor o ganador pueden abrir la disputa
    // ═════════════════════════════════════════════════════════════════════════

    @Test
    void crearReclamo_usuarioTerceroSinRelacion_lanzaSecurityException() {
        Usuario vendedor = new Usuario();
        vendedor.setId(1L);
        vendedor.setNombreUsuario("vendedor1");

        Usuario ganador = new Usuario();
        ganador.setId(2L);
        ganador.setNombreUsuario("ganador1");

        LocalDateTime fechaAdj = LocalDateTime.now(ZoneOffset.UTC).minusHours(1);
        Subasta subasta = subastaAdjudicada(fechaAdj, vendedor, ganador);

        // Usuario id=99 no es vendedor ni ganador
        Usuario tercero = new Usuario();
        tercero.setId(99L);
        tercero.setNombreUsuario("intruso");

        when(subastaRepository.findById(1L)).thenReturn(Optional.of(subasta));
        when(usuarioRepository.findById(99L)).thenReturn(Optional.of(tercero));

        ReclamoDisputaRequestDTO req = new ReclamoDisputaRequestDTO("Fraude", "Intento no autorizado", 1L);

        assertThatThrownBy(() -> service.crearReclamo(req, 99L))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("vendedor");
    }
}
