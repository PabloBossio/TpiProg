package com.TPI.Programacion.IV.Repository;

import com.TPI.Programacion.IV.Model.EstadoSubasta;
import com.TPI.Programacion.IV.Model.Subasta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SubastaRepository extends JpaRepository<Subasta, Long> {
    List<Subasta> findByEstado(EstadoSubasta estado);
    List<Subasta> findByVendedorIdOrderByFechaCierreDesc(Long vendedorId);
}
