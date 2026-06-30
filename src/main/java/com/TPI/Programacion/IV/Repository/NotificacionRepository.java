package com.TPI.Programacion.IV.Repository;

import com.TPI.Programacion.IV.Model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    List<Notificacion> findByUsId(Long usuarioId);
}