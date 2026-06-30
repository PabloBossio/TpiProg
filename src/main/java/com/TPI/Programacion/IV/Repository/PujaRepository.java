package com.TPI.Programacion.IV.Repository;

import com.TPI.Programacion.IV.Model.Puja;
import com.TPI.Programacion.IV.Model.Subasta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PujaRepository extends JpaRepository<Puja, Long> {
    List<Puja> findBySubasta(Subasta subasta);
    List<Puja> findByUsuarioIdOrderByFechaPujaDesc(Long usuarioId);
}
