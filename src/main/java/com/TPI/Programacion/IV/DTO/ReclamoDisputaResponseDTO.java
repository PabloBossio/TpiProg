package com.TPI.Programacion.IV.DTO;

import java.time.LocalDateTime;

public record ReclamoDisputaResponseDTO(
        Long id,
        String motivo,
        String descripcion,
        LocalDateTime fechaCreacion,
        String resolucionAdministrativa,
        Long subastaId,
        Long usuarioDemandanteId,
        String usuarioDemandanteNombre
) {}