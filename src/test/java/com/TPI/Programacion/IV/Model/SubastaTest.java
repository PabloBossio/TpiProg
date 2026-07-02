package com.TPI.Programacion.IV.Model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Verifica que Subasta.cambiarEstado() respete estrictamente el grafo de
 * transiciones definido por las reglas de negocio (BORRADOR -> PUBLICADA -> ACTIVA -> ...).
 */
class SubastaTest {

    private Subasta subastaEn(EstadoSubasta estado) {
        Subasta s = new Subasta();
        s.setEstado(estado);
        return s;
    }

    @Test
    void cambiarEstado_desdeNull_permiteCualquierEstadoInicial() {
        Subasta s = new Subasta(); // estado == null, recién instanciada
        assertThatCode(() -> s.cambiarEstado(EstadoSubasta.BORRADOR, null, "creación"))
                .doesNotThrowAnyException();
        assertThat(s.getEstado()).isEqualTo(EstadoSubasta.BORRADOR);
        assertThat(s.getHistorialEstados()).hasSize(1);
    }

    @Test
    void cambiarEstado_borradorAPublicada_esValida() {
        Subasta s = subastaEn(EstadoSubasta.BORRADOR);
        assertThatCode(() -> s.cambiarEstado(EstadoSubasta.PUBLICADA, null, "publicar"))
                .doesNotThrowAnyException();
        assertThat(s.getEstado()).isEqualTo(EstadoSubasta.PUBLICADA);
    }

    @Test
    void cambiarEstado_borradorDirectoAActiva_esInvalida() {
        Subasta s = subastaEn(EstadoSubasta.BORRADOR);
        assertThatThrownBy(() -> s.cambiarEstado(EstadoSubasta.ACTIVA, null, "salteo PUBLICADA"))
                .isInstanceOf(IllegalStateException.class);
        assertThat(s.getEstado()).isEqualTo(EstadoSubasta.BORRADOR); // no debe mutar en el intento fallido
    }

    @Test
    void cambiarEstado_activaAFinalizadaOAdjudicada_sonValidas() {
        assertThatCode(() -> subastaEn(EstadoSubasta.ACTIVA).cambiarEstado(EstadoSubasta.FINALIZADA, null, "sin pujas"))
                .doesNotThrowAnyException();
        assertThatCode(() -> subastaEn(EstadoSubasta.ACTIVA).cambiarEstado(EstadoSubasta.ADJUDICADA, null, "con pujas"))
                .doesNotThrowAnyException();
    }

    @Test
    void cambiarEstado_finalizadaEsTerminal_noPermiteNingunaTransicion() {
        Subasta s = subastaEn(EstadoSubasta.FINALIZADA);
        assertThatThrownBy(() -> s.cambiarEstado(EstadoSubasta.ACTIVA, null, "reabrir"))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> s.cambiarEstado(EstadoSubasta.CANCELADA, null, "cancelar"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cambiarEstado_canceladaEsTerminal_noPermiteNingunaTransicion() {
        Subasta s = subastaEn(EstadoSubasta.CANCELADA);
        assertThatThrownBy(() -> s.cambiarEstado(EstadoSubasta.PUBLICADA, null, "revivir"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cambiarEstado_adjudicadaAEnDisputa_esValida() {
        assertThatCode(() -> subastaEn(EstadoSubasta.ADJUDICADA).cambiarEstado(EstadoSubasta.EN_DISPUTA, null, "reclamo"))
                .doesNotThrowAnyException();
    }

    @Test
    void cambiarEstado_adjudicadaDirectoACancelada_esInvalida() {
        Subasta s = subastaEn(EstadoSubasta.ADJUDICADA);
        assertThatThrownBy(() -> s.cambiarEstado(EstadoSubasta.CANCELADA, null, "sin pasar por disputa"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cambiarEstado_enDisputaPuedeResolverATresEstados() {
        assertThatCode(() -> subastaEn(EstadoSubasta.EN_DISPUTA).cambiarEstado(EstadoSubasta.ADJUDICADA, null, "resuelto a favor del vendedor"))
                .doesNotThrowAnyException();
        assertThatCode(() -> subastaEn(EstadoSubasta.EN_DISPUTA).cambiarEstado(EstadoSubasta.FINALIZADA, null, "resuelto sin adjudicar"))
                .doesNotThrowAnyException();
        assertThatCode(() -> subastaEn(EstadoSubasta.EN_DISPUTA).cambiarEstado(EstadoSubasta.CANCELADA, null, "resuelto cancelando"))
                .doesNotThrowAnyException();
    }

    @Test
    void cambiarEstado_registraHistorialConEstadoAnteriorYNuevo() {
        Subasta s = subastaEn(EstadoSubasta.PUBLICADA);
        s.cambiarEstado(EstadoSubasta.ACTIVA, null, "inicio automático");

        assertThat(s.getHistorialEstados()).hasSize(1);
        HistorialEstado registro = s.getHistorialEstados().get(0);
        assertThat(registro.getEstadoAnterior()).isEqualTo(EstadoSubasta.PUBLICADA);
        assertThat(registro.getEstadoNuevo()).isEqualTo(EstadoSubasta.ACTIVA);
        assertThat(registro.getFechaCambio()).isNotNull();
    }
}
