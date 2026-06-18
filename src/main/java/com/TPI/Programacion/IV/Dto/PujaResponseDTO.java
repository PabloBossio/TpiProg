package com.TPI.Programacion.IV.Dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PujaResponseDTO(
        Long id,
        BigDecimal monto,
        LocalDateTime fechaPuja,
        Long usuarioId,
        String nombreUsuario
) {}
