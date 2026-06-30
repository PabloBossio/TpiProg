package com.TPI.Programacion.IV.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_usuario", nullable = false, unique = true, length = 100)
    private String nombreUsuario;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "esta_bloqueado", nullable = false)
    private boolean estaBloqueado = false;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "usuario_rol",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "rol_id")
    )
    private List<Rol> roles = new ArrayList<>();

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Puja> misPujas = new ArrayList<>();

    @OneToMany(mappedBy = "us", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Notificacion> notificaciones = new ArrayList<>();


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

