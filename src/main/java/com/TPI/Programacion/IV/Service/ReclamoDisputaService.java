package com.TPI.Programacion.IV.Service;

import com.TPI.Programacion.IV.DTO.ReclamoDisputaRequestDTO;
import com.TPI.Programacion.IV.DTO.ReclamoDisputaResponseDTO;
import com.TPI.Programacion.IV.Exception.RecursoNoEncontradoException;
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
                .orElseThrow(() -> new RecursoNoEncontradoException("Subasta no encontrada"));
        Usuario demandante = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

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

    /**
     * Resuelve una disputa con exactamente dos desenlaces posibles, elegidos por el ADMIN:
     * - Aceptado (disputa válida): la subasta FINALIZA, el artículo vuelve al vendedor
     *   y el pago se reintegra al comprador.
     * - Rechazado (disputa no válida): la subasta vuelve a ADJUDICADA, la venta se sostiene
     *   y los fondos quedan liberados definitivamente para el vendedor.
     */
    @Transactional
    public ReclamoDisputaResponseDTO resolver(Long id, boolean aceptado, String comentario) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario admin = usuarioRepository.findByNombreUsuario(auth.getName())
                .orElseThrow(() -> new RecursoNoEncontradoException("Admin no encontrado"));

        ReclamoDisputa reclamo = reclamoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reclamo no encontrado"));

        if (reclamo.getResolucionAdministrativa() != null && !reclamo.getResolucionAdministrativa().isBlank()) {
            throw new IllegalStateException("Este reclamo ya fue resuelto anteriormente.");
        }

        Subasta subasta = reclamo.getSubasta();
        if (subasta.getEstado() != EstadoSubasta.EN_DISPUTA) {
            throw new IllegalStateException("La subasta asociada ya no está en disputa.");
        }

        String comentarioLimpio = (comentario == null || comentario.isBlank()) ? null : comentario.trim();

        String motivo;
        if (aceptado) {
            reclamo.setResultado(ResultadoReclamo.ACEPTADO);
            motivo = "Reclamo aceptado: el artículo se devuelve al vendedor y el pago se reintegra al comprador.";
            if (comentarioLimpio != null) motivo += " " + comentarioLimpio;
            subasta.cambiarEstado(EstadoSubasta.FINALIZADA, admin, motivo);
        } else {
            reclamo.setResultado(ResultadoReclamo.RECHAZADO);
            motivo = "Reclamo rechazado: los fondos se liberan definitivamente para el vendedor.";
            if (comentarioLimpio != null) motivo += " " + comentarioLimpio;
            subasta.cambiarEstado(EstadoSubasta.ADJUDICADA, admin, motivo);
        }

        reclamo.setResolucionAdministrativa(motivo);
        subastaRepository.save(subasta);
        ReclamoDisputa actualizado = reclamoRepository.save(reclamo);

        return mapearADto(actualizado);
    }

    @Transactional(readOnly = true)
    public List<ReclamoDisputaResponseDTO> listarTodos() {
        return reclamoRepository.findAll().stream().map(this::mapearADto).collect(Collectors.toList());
    }

    private ReclamoDisputaResponseDTO mapearADto(ReclamoDisputa r) {
        return new ReclamoDisputaResponseDTO(
                r.getId(), r.getMotivo(), r.getDescripcion(), r.getFechaCreacion(),
                r.getResolucionAdministrativa(),
                r.getResultado() != null ? r.getResultado().name() : null,
                r.getSubasta().getId(),
                r.getUsuarioDemandante().getId(), r.getUsuarioDemandante().getNombreUsuario());
    }
}
