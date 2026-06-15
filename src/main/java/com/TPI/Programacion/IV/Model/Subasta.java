package com.TPI.Programacion.IV.Model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class Subasta {


    private Long id;
    private BigDecimal precioBase;
    private BigDecimal montoActual;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaCierre;
    private LocalDateTime fechaAdjudicacion;
    private BigDecimal incrementoMinimo;
    private String descripcion;
    private EstadoSubasta estado; // Enum (BORRADOR, ACTIVA, ADJUDICADA, etc.)
    private Integer version; // Para el Bloqueo Optimista de Concurrencia


    private Producto producto;
    private Categoria categoria;
    private Usuario vendedor;
    private Usuario ganador;
    private List<Puja> pujas = new ArrayList<>();
    private List<HistorialEstado> historialEstados = new ArrayList<>();


    public Subasta() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BigDecimal getPrecioBase() { return precioBase; }
    public void setPrecioBase(BigDecimal precioBase) { this.precioBase = precioBase; }

    public BigDecimal getMontoActual() { return montoActual; }
    public void setMontoActual(BigDecimal montoActual) { this.montoActual = montoActual; }

    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDateTime fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDateTime getFechaCierre() { return fechaCierre; }
    public void setFechaCierre(LocalDateTime fechaCierre) { this.fechaCierre = fechaCierre; }

    public LocalDateTime getFechaAdjudicacion() { return fechaAdjudicacion; }
    public void setFechaAdjudicacion(LocalDateTime fechaAdjudicacion) { this.fechaAdjudicacion = fechaAdjudicacion; }

    public BigDecimal getIncrementoMinimo() { return incrementoMinimo; }
    public void setIncrementoMinimo(BigDecimal incrementoMinimo) { this.incrementoMinimo = incrementoMinimo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public EstadoSubasta getEstado() { return estado; }
    public void setEstado(EstadoSubasta estado) { this.estado = estado; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }

    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }

    public Usuario getVendedor() { return vendedor; }
    public void setVendedor(Usuario vendedor) { this.vendedor = vendedor; }

    public Usuario getGanador() { return ganador; }
    public void setGanador(Usuario ganador) { this.ganador = ganador; }

    public List<Puja> getPujas() { return pujas; }
    public void setPujas(List<Puja> pujas) { this.pujas = pujas; }

    public List<HistorialEstado> getHistorialEstados() { return historialEstados; }
    public void setHistorialEstados(List<HistorialEstado> historialEstados) { this.historialEstados = historialEstados; }


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
        // 1. Debe estar estrictamente en estado ACTIVA
        if (this.estado != EstadoSubasta.ACTIVA) {
            return false;
        }


        LocalDateTime ahoraUtc = LocalDateTime.now(ZoneOffset.UTC);
        if (ahoraUtc.isAfter(this.fechaCierre)) {
            return false;
        }


        if (this.vendedor != null && usuarioOferente.getId().equals(this.vendedor.getId())) {
            return false;
        }


        if (usuarioOferente.isEstaBloqueado()) {
            return false;
        }


        if (this.pujas == null || this.pujas.isEmpty()) {
            // Primera puja: debe ser igual o mayor al precio base
            if (montoOferta.compareTo(this.precioBase) < 0) {
                return false;
            }
        } else {

            BigDecimal minimoRequerido = this.montoActual.add(this.incrementoMinimo);
            if (montoOferta.compareTo(minimoRequerido) < 0) {
                return false;
            }
        }

        return true; // Cumple con todos los requisitos
    }


    public void cambiarEstado(EstadoSubasta nuevoEstado, Usuario responsable, String motivo) {
        HistorialEstado registro = new HistorialEstado();
        registro.setEstadoAnterior(this.estado);
        registro.setEstadoNuevo(nuevoEstado);
        registro.setFechaCambio(LocalDateTime.now(ZoneOffset.UTC));
        registro.setUsuarioResp(responsable);
        registro.setMotivo(motivo);

        this.historialEstados.add(registro);
        this.estado = nuevoEstado;
    }

}