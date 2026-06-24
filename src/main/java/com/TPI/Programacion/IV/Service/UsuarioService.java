package com.TPI.Programacion.IV.Service;

import com.TPI.Programacion.IV.DTO.UsuarioRequestDTO;
import com.TPI.Programacion.IV.DTO.UsuarioResponseDTO;
import com.TPI.Programacion.IV.Model.Usuario;
import com.TPI.Programacion.IV.Repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional
    public UsuarioResponseDTO registrarUsuario(UsuarioRequestDTO request) {
        Usuario usuario = new Usuario();
        usuario.setNombreUsuario(request.nombreUsuario());
        usuario.setEmail(request.email());

        usuario.setPasswordHash("$2a$10$" + request.password().hashCode());
        usuario.setEstaBloqueado(false);

        Usuario guardado = usuarioRepository.save(usuario);

        return mapearADto(guardado);
    }

    public UsuarioResponseDTO obtenerPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
        return mapearADto(usuario);
    }

    public UsuarioResponseDTO mapearADto(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNombreUsuario(),
                usuario.getEmail(),
                usuario.isEstaBloqueado(),
                usuario.getRoles().stream().map(rol -> rol.getNombreRol()).collect(Collectors.toList())
        );
    }
}