package com.TPI.Programacion.IV.Service;

import com.TPI.Programacion.IV.DTO.HistorialEstadoResponseDTO;
import com.TPI.Programacion.IV.Model.HistorialEstado;
import com.TPI.Programacion.IV.Repository.HistorialEstadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HistorialEstadoService {

    @Autowired
    private HistorialEstadoRepository historialRepository;

    @Transactional(readOnly = true)
    public List<HistorialEstadoResponseDTO> obtenerPorSubasta(Long subastaId) {
        List<HistorialEstado> lista = historialRepository.findBySubastaId(subastaId);
        return lista.stream().map(h -> new HistorialEstadoResponseDTO(
                h.getId(),
                h.getEstadoAnterior(),
                h.getEstadoNuevo(),
                h.getFechaCambio(),
                h.getMotivo(),
                h.getUsuarioResp() != null ? h.getUsuarioResp().getNombreUsuario() : "Sistema"
        )).collect(Collectors.toList());
    }
}