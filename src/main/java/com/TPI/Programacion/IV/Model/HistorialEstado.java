package com.TPI.Programacion.IV.Model;

import java.time.LocalDateTime;

public class HistorialEstado {
    private Long id;
    private EstadoSubasta estadoAnterior;
    private EstadoSubasta estadoNuevo;
    private LocalDateTime fechaCambio;
    private String motivo;

    private Usuario usuarioResp;

    public HistorialEstado() {}

    public HistorialEstado(Long id, EstadoSubasta estadoAnterior, EstadoSubasta estadoNuevo, LocalDateTime fechaCambio, String motivo, Usuario usuarioResp) {
        this.id = id;
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
        this.fechaCambio = fechaCambio;
        this.motivo = motivo;
        this.usuarioResp = usuarioResp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EstadoSubasta getEstadoAnterior() {
        return estadoAnterior;
    }

    public void setEstadoAnterior(EstadoSubasta estadoAnterior) {
        this.estadoAnterior = estadoAnterior;
    }

    public EstadoSubasta getEstadoNuevo() {
        return estadoNuevo;
    }

    public void setEstadoNuevo(EstadoSubasta estadoNuevo) {
        this.estadoNuevo = estadoNuevo;
    }

    public LocalDateTime getFechaCambio() {
        return fechaCambio;
    }

    public void setFechaCambio(LocalDateTime fechaCambio) {
        this.fechaCambio = fechaCambio;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public Usuario getUsuarioResp() {
        return usuarioResp;
    }

    public void setUsuarioResp(Usuario usuarioResp) {
        this.usuarioResp = usuarioResp;
    }
}