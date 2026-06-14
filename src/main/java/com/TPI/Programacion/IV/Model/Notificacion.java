package com.TPI.Programacion.IV.Model;

import java.time.LocalDateTime;

public class Notificacion {
    private Long id;
    private String mensaje;
    private LocalDateTime fechaEnvio;
    private Boolean leido;

    public Notificacion() {}

    public Notificacion(Long id, String mensaje, LocalDateTime fechaEnvio, Boolean leido) {
        this.id = id;
        this.mensaje = mensaje;
        this.fechaEnvio = fechaEnvio;
        this.leido = leido;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public LocalDateTime getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(LocalDateTime fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }

    public Boolean getLeido() {
        return leido;
    }

    public void setLeido(Boolean leido) {
        this.leido = leido;
    }
}
