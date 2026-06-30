package com.TPI.Programacion.IV.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PujaResponseDTO(
        Long id,
        BigDecimal monto,
        LocalDateTime fechaPuja,
        Long oferenteId,
        String oferenteNombre
) {}
