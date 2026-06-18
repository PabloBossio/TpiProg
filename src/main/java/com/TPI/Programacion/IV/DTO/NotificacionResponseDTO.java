package com.TPI.Programacion.IV.DTO;

import java.time.LocalDateTime;

public record NotificacionResponseDTO(
        Long id,
        String mensaje,
        LocalDateTime fechaEnvio,
        Boolean leido
) {}
