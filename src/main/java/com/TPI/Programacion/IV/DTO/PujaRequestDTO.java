package com.TPI.Programacion.IV.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record PujaRequestDTO(
        @NotNull(message = "El monto de la puja es obligatorio")
        @Positive(message = "El monto debe ser mayor a cero")
        BigDecimal monto
) {}
