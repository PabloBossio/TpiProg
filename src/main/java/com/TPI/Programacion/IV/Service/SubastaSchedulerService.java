package com.TPI.Programacion.IV.Service;

import com.TPI.Programacion.IV.Model.EstadoSubasta;
import com.TPI.Programacion.IV.Model.Subasta;
import com.TPI.Programacion.IV.Repository.SubastaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class SubastaSchedulerService {

    @Autowired
    private SubastaRepository subastaRepository;

    // Corre cada 15 segundos: barrido de respaldo para subastas que nadie consulta activamente.
    // Las consultas puntuales (SubastaService.obtenerPorId/procesarPuja) sincronizan el estado
    // al vuelo, así que este intervalo solo acota el peor caso para subastas "olvidadas".
    @Scheduled(fixedRate = 15000)
    @Transactional
    public void procesarTransicionesAutomaticas() {
        LocalDateTime ahora = LocalDateTime.now(ZoneOffset.UTC);

        // PUBLICADA → ACTIVA cuando llega la fechaInicio
        List<Subasta> publicadas = subastaRepository.findByEstado(EstadoSubasta.PUBLICADA);
        for (Subasta s : publicadas) {
            if (!ahora.isBefore(s.getFechaInicio())) {
                s.cambiarEstado(EstadoSubasta.ACTIVA, null, "Inicio automático programado");
                subastaRepository.save(s);
            }
        }

        // ACTIVA → ADJUDICADA o FINALIZADA cuando llega la fechaCierre
        List<Subasta> activas = subastaRepository.findByEstado(EstadoSubasta.ACTIVA);
        for (Subasta s : activas) {
            if (ahora.isAfter(s.getFechaCierre())) {
                if (s.getGanador() != null) {
                    s.setFechaAdjudicacion(ahora);
                    s.cambiarEstado(EstadoSubasta.ADJUDICADA, null,
                            "Adjudicada automáticamente. Precio final: " + s.getMontoActual());
                } else {
                    s.cambiarEstado(EstadoSubasta.FINALIZADA, null, "Finalizada sin pujas");
                }
                subastaRepository.save(s);
            }
        }
    }
}
