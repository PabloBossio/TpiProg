package com.TPI.Programacion.IV.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RolRequestDTO(
        @NotBlank(message = "El nombre del rol es obligatorio")
        @Size(max = 50, message = "El rol no puede superar los 50 caracteres")
        String nombreRol
) {}
