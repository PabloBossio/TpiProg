package com.TPI.Programacion.IV.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReclamoDisputaRequestDTO(
        @NotBlank(message = "El motivo del reclamo es obligatorio")
        @Size(max = 150)
        String motivo,

        @NotBlank(message = "La descripción del problema es obligatoria")
        @Size(max = 1000)
        String descripcion,

        @NotNull(message = "El ID de la subasta en conflicto es obligatorio")
        Long subastaId
) {}