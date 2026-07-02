package com.TPI.Programacion.IV.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "subastas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Subasta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "precio_base", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioBase;

    @Column(name = "monto_actual", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoActual;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_cierre", nullable = false)
    private LocalDateTime fechaCierre;

    @Column(name = "fecha_adjudicacion")
    private LocalDateTime fechaAdjudicacion;

    @Column(name = "incremento_minimo", nullable = false, precision = 12, scale = 2)
    private BigDecimal incrementoMinimo;

    @Column(name = "descripcion", length = 1000)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoSubasta estado;

    @Version
    @Column(name = "version")
    private Integer version;



    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "producto_id", nullable = false, unique = true)
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendedor_id", nullable = false)
    private Usuario vendedor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ganador_id")
    private Usuario ganador;


    @OneToMany(mappedBy = "subasta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Puja> pujas = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "subasta_id")
    private List<HistorialEstado> historialEstados = new ArrayList<>();




    public boolean validarPeriodoMaximo() {
        if (this.fechaInicio == null || this.fechaCierre == null) {
            return false;
        }
        if (this.fechaCierre.isBefore(this.fechaInicio)) {
            return false;
        }
        LocalDateTime limiteMaximo = this.fechaInicio.plusWeeks(2);
        return !this.fechaCierre.isAfter(limiteMaximo);
    }

    public boolean tieneTiempoParaDisputa() {
        if (this.fechaAdjudicacion == null) {
            return false;
        }
        LocalDateTime ahoraUtc = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime limiteDisputa = this.fechaAdjudicacion.plusHours(24);
        return ahoraUtc.isBefore(limiteDisputa);
    }

    public boolean validarNuevaPuja(BigDecimal montoOferta, Usuario usuarioOferente) {

        if (this.estado != EstadoSubasta.ACTIVA) {
            return false;
        }


        LocalDateTime ahoraUtc = LocalDateTime.now(ZoneOffset.UTC);
        if (ahoraUtc.isAfter(this.fechaCierre)) {
            return false;
        }

        if (this.vendedor != null && usuarioOferente != null &&
                java.util.Objects.equals(usuarioOferente.getId(), this.vendedor.getId())) {
            return false;
        }

        if (usuarioOferente != null && usuarioOferente.isEstaBloqueado()) {
            return false;
        }

        if (this.pujas == null || this.pujas.isEmpty()) {
            if (montoOferta.compareTo(this.precioBase) < 0) {
                return false;
            }
        } else {
            BigDecimal minimoRequerido = this.montoActual.add(this.incrementoMinimo);
            if (montoOferta.compareTo(minimoRequerido) < 0) {
                return false;
            }
        }

        return true;
    }


    public void cambiarEstado(EstadoSubasta nuevoEstado, Usuario responsable, String motivo) {
        if (!esTransicionValida(this.estado, nuevoEstado)) {
            throw new IllegalStateException(
                    "Transición de estado inválida: " + this.estado + " → " + nuevoEstado);
        }

        HistorialEstado registro = new HistorialEstado();
        registro.setEstadoAnterior(this.estado);
        registro.setEstadoNuevo(nuevoEstado);
        registro.setFechaCambio(LocalDateTime.now(ZoneOffset.UTC));
        registro.setUsuarioResp(responsable);
        registro.setMotivo(motivo);

        this.historialEstados.add(registro);
        this.estado = nuevoEstado;
    }

    /**
     * Grafo estricto de transiciones permitido por las reglas de negocio.
     * BORRADOR -> PUBLICADA -> ACTIVA -> {FINALIZADA | ADJUDICADA}
     * PUBLICADA / ACTIVA -> CANCELADA
     * ADJUDICADA -> EN_DISPUTA -> {ADJUDICADA | FINALIZADA | CANCELADA}
     */
    private static boolean esTransicionValida(EstadoSubasta desde, EstadoSubasta hasta) {
        if (desde == null) {
            return true; // estado inicial de la entidad recién creada
        }
        return switch (desde) {
            case BORRADOR -> hasta == EstadoSubasta.PUBLICADA;
            case PUBLICADA -> hasta == EstadoSubasta.ACTIVA || hasta == EstadoSubasta.CANCELADA;
            case ACTIVA -> hasta == EstadoSubasta.FINALIZADA
                    || hasta == EstadoSubasta.ADJUDICADA
                    || hasta == EstadoSubasta.CANCELADA;
            case ADJUDICADA -> hasta == EstadoSubasta.EN_DISPUTA;
            case EN_DISPUTA -> hasta == EstadoSubasta.ADJUDICADA
                    || hasta == EstadoSubasta.FINALIZADA
                    || hasta == EstadoSubasta.CANCELADA;
            case FINALIZADA, CANCELADA -> false;
        };
    }
}