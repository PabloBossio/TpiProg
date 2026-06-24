package com.TPI.Programacion.IV.Repository;

import com.TPI.Programacion.IV.Model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {}