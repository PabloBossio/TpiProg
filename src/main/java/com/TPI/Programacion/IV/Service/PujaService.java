package com.TPI.Programacion.IV.Service;

import com.TPI.Programacion.IV.Model.EstadoSubasta;
import com.TPI.Programacion.IV.Model.Puja;
import com.TPI.Programacion.IV.Model.Subasta;
import com.TPI.Programacion.IV.Repository.PujaRepository;
import com.TPI.Programacion.IV.Repository.SubastaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PujaService {

    @Autowired
    private PujaRepository pujaRepository;

    @Autowired
    private SubastaRepository subastaRepository;

    public Puja registrarPuja(Long idSubasta, Puja puja){

        Subasta subasta = subastaRepository.findById(idSubasta)
                .orElseThrow(() -> new RuntimeException("Subasta no encontrada"));

        if(subasta.getEstado() != EstadoSubasta.ACTIVA){
            throw new RuntimeException("La subasta no está activa");
        }

        BigDecimal montoActual = subasta.getMontoActual();

        BigDecimal minimo = montoActual.add(subasta.getIncrementoMinimo());

        if(puja.getMonto().compareTo(minimo) < 0){
            throw new RuntimeException("La puja es muy baja");
        }

        puja.setSubasta(subasta);

        subasta.setMontoActual(puja.getMonto());

        subastaRepository.save(subasta);

        return pujaRepository.save(puja);
    }
}