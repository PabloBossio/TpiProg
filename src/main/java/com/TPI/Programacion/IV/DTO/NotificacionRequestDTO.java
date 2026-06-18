package com.TPI.Programacion.IV.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NotificacionRequestDTO(
        @NotBlank(message = "El mensaje no puede estar vacío")
        String mensaje,

        @NotNull(message = "El ID del usuario destino es obligatorio")
        Long usuarioId
) {}
