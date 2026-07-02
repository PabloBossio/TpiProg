package com.TPI.Programacion.IV.Config;

import com.TPI.Programacion.IV.Model.Rol;
import com.TPI.Programacion.IV.Repository.RolRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Garantiza que los roles base (USER, SELLER, ADMIN) existan siempre en la base de datos,
 * ya que no hay ningún script de migración/seed y el formulario de registro necesita
 * poder ofrecerlos para selección desde el arranque.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final RolRepository rolRepository;

    public DataSeeder(RolRepository rolRepository) {
        this.rolRepository = rolRepository;
    }

    @Override
    public void run(String... args) {
        for (String nombre : List.of("USER", "SELLER", "ADMIN")) {
            if (rolRepository.findByNombreRol(nombre).isEmpty()) {
                Rol rol = new Rol();
                rol.setNombreRol(nombre);
                rolRepository.save(rol);
            }
        }
    }
}
