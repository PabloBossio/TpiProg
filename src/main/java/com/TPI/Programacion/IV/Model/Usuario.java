package com.TPI.Programacion.IV.Model;

import java.util.ArrayList;
import java.util.List;

public class Usuario {

    private Long id;
    private String nombreUsuario;
    private String email;
    private String passwordHash;
    private boolean estaBloqueado;


    private List<Rol> roles = new ArrayList<>();
    private List<Puja> misPujas = new ArrayList<>();
    private List<Notificacion> notificaciones = new ArrayList<>();


    public Usuario() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public boolean isEstaBloqueado() { return estaBloqueado; }
    public void setEstaBloqueado(boolean estaBloqueado) { this.estaBloqueado = estaBloqueado; }

    public List<Rol> getRoles() { return roles; }
    public void setRoles(List<Rol> roles) { this.roles = roles; }

    public List<Puja> getMisPujas() { return misPujas; }
    public void setMisPujas(List<Puja> misPujas) { this.misPujas = misPujas; }

    public List<Notificacion> getNotificaciones() { return notificaciones; }
    public void setNotificaciones(List<Notificacion> notificaciones) { this.notificaciones = notificaciones; }


    public void bloquearUsuario() {
        this.estaBloqueado = true;
    }


    public void desbloquearUsuario() {
        this.estaBloqueado = false;
    }

    public boolean tieneRol(String nombreRol) {
        if (this.roles == null) {
            return false;
        }
        for (Rol r : this.roles) {
            if (r.getNombreRol() != null && r.getNombreRol().equalsIgnoreCase(nombreRol)) {
                return true;
            }
        }
        return false;
    }

}

