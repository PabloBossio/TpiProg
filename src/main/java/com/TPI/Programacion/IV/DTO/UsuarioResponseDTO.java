package com.TPI.Programacion.IV.DTO;

import java.util.List;

public record UsuarioResponseDTO(
        Long id,
        String nombreUsuario,
        String email,
        boolean estaBloqueado,
        List<String> roles
) {}