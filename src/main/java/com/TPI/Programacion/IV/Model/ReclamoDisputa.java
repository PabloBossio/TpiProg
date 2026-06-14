package com.TPI.Programacion.IV.Model;

import java.time.LocalDateTime;

public class ReclamoDisputa {
    private Long id;
    private String motivo;
    private String descripcion;
    private LocalDateTime fechaCreacion;
    private String resolucionAdministrativa;

    public ReclamoDisputa() {}

    public ReclamoDisputa(Long id, String motivo, String descripcion,
                          LocalDateTime fechaCreacion,
                          String resolucionAdministrativa) {
        this.id = id;
        this.motivo = motivo;
        this.descripcion = descripcion;
        this.fechaCreacion = fechaCreacion;
        this.resolucionAdministrativa = resolucionAdministrativa;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getResolucionAdministrativa() {
        return resolucionAdministrativa;
    }

    public void setResolucionAdministrativa(String resolucionAdministrativa) {
        this.resolucionAdministrativa = resolucionAdministrativa;
    }
}