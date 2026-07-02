package com.TPI.Programacion.IV.Service;

import com.TPI.Programacion.IV.DTO.NotificacionResponseDTO;
import com.TPI.Programacion.IV.Exception.RecursoNoEncontradoException;
import com.TPI.Programacion.IV.Model.Notificacion;
import com.TPI.Programacion.IV.Repository.NotificacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificacionService {

    @Autowired
    private NotificacionRepository notificacionRepository;

    @Transactional(readOnly = true)
    public List<NotificacionResponseDTO> obtenerPorUsuario(Long usuarioId) {
        List<Notificacion> notificaciones = notificacionRepository.findByUsId(usuarioId);
        return notificaciones.stream().map(n -> new NotificacionResponseDTO(
                n.getId(),
                n.getMensaje(),
                n.getFechaEnvio(),
                n.getLeido()
        )).collect(Collectors.toList());
    }

    @Transactional
    public void marcarLeida(Long id) {
        Notificacion n = notificacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Notificación no encontrada"));
        n.setLeido(true);
        notificacionRepository.save(n);
    }
}