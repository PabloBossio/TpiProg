package com.TPI.Programacion.IV.Service;

import com.TPI.Programacion.IV.DTO.UsuarioRequestDTO;
import com.TPI.Programacion.IV.DTO.UsuarioResponseDTO;
import com.TPI.Programacion.IV.Model.Rol;
import com.TPI.Programacion.IV.Model.Usuario;
import com.TPI.Programacion.IV.Repository.RolRepository;
import com.TPI.Programacion.IV.Repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collections;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public UsuarioResponseDTO registrarUsuario(UsuarioRequestDTO request) {
        Usuario usuario = new Usuario();
        usuario.setNombreUsuario(request.nombreUsuario());
        usuario.setEmail(request.email());
        usuario.setPasswordHash(passwordEncoder.encode(request.password()));
        usuario.setEstaBloqueado(false);

        rolRepository.findByNombreRol("COMPRADOR").ifPresent(rol -> {
            usuario.getRoles().add(rol);
        });

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
                usuario.getRoles() != null
                        ? usuario.getRoles().stream().map(Rol::getNombreRol).collect(Collectors.toList())
                        : Collections.emptyList()
        );
    }
}