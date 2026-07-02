package com.TPI.Programacion.IV.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReclamoResolucionRequestDTO(
        @NotNull(message = "Debés indicar si el reclamo se acepta o se rechaza")
        Boolean aceptado,

        @Size(max = 500, message = "El comentario no puede superar los 500 caracteres")
        String comentario
) {}
