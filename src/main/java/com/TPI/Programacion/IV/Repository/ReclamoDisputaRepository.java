package com.TPI.Programacion.IV.Repository;

import com.TPI.Programacion.IV.Model.ReclamoDisputa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReclamoDisputaRepository extends JpaRepository<ReclamoDisputa, Long> {
    List<ReclamoDisputa> findBySubastaId(Long subastaId);
}
