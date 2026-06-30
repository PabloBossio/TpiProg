package com.TPI.Programacion.IV.Service;

import com.TPI.Programacion.IV.DTO.ReclamoDisputaRequestDTO;
import com.TPI.Programacion.IV.DTO.ReclamoDisputaResponseDTO;
import com.TPI.Programacion.IV.Model.*;
import com.TPI.Programacion.IV.Repository.ReclamoDisputaRepository;
import com.TPI.Programacion.IV.Repository.SubastaRepository;
import com.TPI.Programacion.IV.Repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReclamoDisputaService {

    @Autowired private ReclamoDisputaRepository reclamoRepository;
    @Autowired private SubastaRepository subastaRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    @Transactional
    public ReclamoDisputaResponseDTO crearReclamo(ReclamoDisputaRequestDTO request, Long usuarioId) {
        Subasta subasta = subastaRepository.findById(request.subastaId())
                .orElseThrow(() -> new RuntimeException("Subasta no encontrada"));
        Usuario demandante = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (subasta.getEstado() != EstadoSubasta.ADJUDICADA) {
            throw new IllegalStateException("Solo se puede abrir una disputa sobre una subasta ADJUDICADA.");
        }
        if (!subasta.tieneTiempoParaDisputa()) {
            throw new IllegalStateException("El tiempo límite de 24 horas para iniciar una disputa ha expirado.");
        }

        boolean esVendedor = subasta.getVendedor().getId().equals(usuarioId);
        boolean esGanador = subasta.getGanador() != null && subasta.getGanador().getId().equals(usuarioId);
        if (!esVendedor && !esGanador) {
            throw new SecurityException("Solo el vendedor o el ganador pueden abrir una disputa.");
        }

        ReclamoDisputa reclamo = new ReclamoDisputa();
        reclamo.setMotivo(request.motivo());
        reclamo.setDescripcion(request.descripcion());
        reclamo.setFechaCreacion(LocalDateTime.now(ZoneOffset.UTC));
        reclamo.setSubasta(subasta);
        reclamo.setUsuarioDemandante(demandante);
        ReclamoDisputa guardado = reclamoRepository.save(reclamo);

        subasta.cambiarEstado(EstadoSubasta.EN_DISPUTA, demandante, "Disputa abierta: " + request.motivo());
        subastaRepository.save(subasta);

        return mapearADto(guardado);
    }

    @Transactional
    public ReclamoDisputaResponseDTO resolver(Long id, String resolucion, String estadoFinalStr) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario admin = usuarioRepository.findByNombreUsuario(auth.getName())
                .orElseThrow(() -> new RuntimeException("Admin no encontrado"));

        ReclamoDisputa reclamo = reclamoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reclamo no encontrado"));

        reclamo.setResolucionAdministrativa(resolucion);
        ReclamoDisputa actualizado = reclamoRepository.save(reclamo);

        if (estadoFinalStr != null && !estadoFinalStr.isBlank()) {
            EstadoSubasta estadoFinal = EstadoSubasta.valueOf(estadoFinalStr.toUpperCase());
            Subasta subasta = reclamo.getSubasta();
            subasta.cambiarEstado(estadoFinal, admin, "Resuelto por admin: " + resolucion);
            subastaRepository.save(subasta);
        }

        return mapearADto(actualizado);
    }

    @Transactional(readOnly = true)
    public List<ReclamoDisputaResponseDTO> listarTodos() {
        return reclamoRepository.findAll().stream().map(this::mapearADto).collect(Collectors.toList());
    }

    private ReclamoDisputaResponseDTO mapearADto(ReclamoDisputa r) {
        return new ReclamoDisputaResponseDTO(
                r.getId(), r.getMotivo(), r.getDescripcion(), r.getFechaCreacion(),
                r.getResolucionAdministrativa(), r.getSubasta().getId(),
                r.getUsuarioDemandante().getId(), r.getUsuarioDemandante().getNombreUsuario());
    }
}
