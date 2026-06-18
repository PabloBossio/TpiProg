package com.TPI.Programacion.IV.DTO;

import com.TPI.Programacion.IV.Model.EstadoSubasta;
import java.time.LocalDateTime;

public record HistorialEstadoResponseDTO(
        Long id,
        EstadoSubasta estadoAnterior,
        EstadoSubasta estadoNuevo,
        LocalDateTime fechaCambio,
        String motivo,
        String usuarioResponsableNombre
) {}
