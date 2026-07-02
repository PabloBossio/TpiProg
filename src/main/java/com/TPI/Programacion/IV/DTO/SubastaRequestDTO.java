package com.TPI.Programacion.IV.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SubastaRequestDTO(
        @NotNull(message = "El precio base es obligatorio")
        @Positive(message = "El precio base debe ser mayor a cero")
        BigDecimal precioBase,

        // Opcional: si no se envía, el backend la fija en la hora UTC actual del servidor
        // (nunca se confía en la hora del cliente para esto).
        LocalDateTime fechaInicio,

        @NotNull(message = "La fecha de cierre es obligatoria")
        LocalDateTime fechaCierre,

        @NotNull(message = "El incremento mínimo es obligatorio")
        @Positive(message = "El incremento mínimo debe ser positivo")
        BigDecimal incrementoMinimo,

        String descripcion,

        @NotNull(message = "El ID de la categoría es obligatorio")
        Long categoriaId,

        @NotNull(message = "Los datos del producto son obligatorios")
        ProductoRequestDTO producto
) {}