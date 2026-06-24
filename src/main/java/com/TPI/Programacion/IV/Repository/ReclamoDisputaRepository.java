package com.TPI.Programacion.IV.Repository;

import com.TPI.Programacion.IV.Model.ReclamoDisputa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReclamoDisputaRepository extends JpaRepository<ReclamoDisputa, Long> {}