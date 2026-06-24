package com.TPI.Programacion.IV.Service;

import com.TPI.Programacion.IV.DTO.ReclamoDisputaRequestDTO;
import com.TPI.Programacion.IV.DTO.ReclamoDisputaResponseDTO;
import com.TPI.Programacion.IV.Model.ReclamoDisputa;
import com.TPI.Programacion.IV.Model.Subasta;
import com.TPI.Programacion.IV.Model.Usuario;
import com.TPI.Programacion.IV.Repository.ReclamoDisputaRepository;
import com.TPI.Programacion.IV.Repository.SubastaRepository;
import com.TPI.Programacion.IV.Repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class ReclamoDisputaService {

    @Autowired
    private ReclamoDisputaRepository reclamoRepository;

    @Autowired
    private SubastaRepository subastaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional
    public ReclamoDisputaResponseDTO crearReclamo(ReclamoDisputaRequestDTO request, Long usuarioId) {
        Subasta subasta = subastaRepository.findById(request.subastaId())
                .orElseThrow(() -> new RuntimeException("Subasta no encontrada"));
        Usuario demandante = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!subasta.tieneTiempoParaDisputa()) {
            throw new IllegalStateException("El tiempo límite para iniciar un reclamo sobre esta subasta ha expirado.");
        }

        ReclamoDisputa reclamo = new ReclamoDisputa();
        reclamo.setMotivo(request.motivo());
        reclamo.setDescripcion(request.descripcion());
        reclamo.setFechaCreacion(LocalDateTime.now(ZoneOffset.UTC));
        reclamo.setSubasta(subasta);
        reclamo.setUsuarioDemandante(demandante);

        ReclamoDisputa guardado = reclamoRepository.save(reclamo);
        return mapearADto(guardado);
    }

    @Transactional
    public ReclamoDisputaResponseDTO resolver(Long id, String resolucion) {
        ReclamoDisputa reclamo = reclamoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reclamo no encontrado"));

        reclamo.setResolucionAdministrativa(resolucion);
        ReclamoDisputa actualizado = reclamoRepository.save(reclamo);
        return mapearADto(actualizado);
    }

    private ReclamoDisputaResponseDTO mapearADto(ReclamoDisputa r) {
        return new ReclamoDisputaResponseDTO(
                r.getId(), r.getMotivo(), r.getDescripcion(), r.getFechaCreacion(),
                r.getResolucionAdministrativa(), r.getSubasta().getId(),
                r.getUsuarioDemandante().getId(), r.getUsuarioDemandante().getNombreUsuario()
        );
    }
}