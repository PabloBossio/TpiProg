package com.TPI.Programacion.IV.Service;

import com.TPI.Programacion.IV.DTO.UsuarioRequestDTO;
import com.TPI.Programacion.IV.DTO.UsuarioResponseDTO;
import com.TPI.Programacion.IV.Exception.RecursoNoEncontradoException;
import com.TPI.Programacion.IV.Model.Rol;
import com.TPI.Programacion.IV.Model.Usuario;
import com.TPI.Programacion.IV.Repository.RolRepository;
import com.TPI.Programacion.IV.Repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private RolRepository rolRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Transactional
    public UsuarioResponseDTO registrarUsuario(UsuarioRequestDTO request) {
        Usuario usuario = new Usuario();
        usuario.setNombreUsuario(request.nombreUsuario());
        usuario.setEmail(request.email());
        usuario.setPasswordHash(passwordEncoder.encode(request.password()));
        usuario.setEstaBloqueado(false);

        List<Rol> rolesSeleccionados = new ArrayList<>();
        if (request.rolesIds() != null && !request.rolesIds().isEmpty()) {
            for (Long rolId : request.rolesIds()) {
                Rol rol = rolRepository.findById(rolId)
                        .orElseThrow(() -> new IllegalArgumentException("El rol seleccionado (id " + rolId + ") no existe."));
                rolesSeleccionados.add(rol);
            }
        } else {
            // Sin selección explícita: todo usuario nuevo es al menos comprador (USER)
            rolRepository.findByNombreRol("USER").ifPresent(rolesSeleccionados::add);
        }
        usuario.setRoles(rolesSeleccionados);

        return mapearADto(usuarioRepository.save(usuario));
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listarTodos() {
        return usuarioRepository.findAll().stream().map(this::mapearADto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO obtenerPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con ID: " + id));
        return mapearADto(usuario);
    }

    @Transactional
    public UsuarioResponseDTO bloquear(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con ID: " + id));
        usuario.bloquearUsuario();
        return mapearADto(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponseDTO desbloquear(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con ID: " + id));
        usuario.desbloquearUsuario();
        return mapearADto(usuarioRepository.save(usuario));
    }

    public UsuarioResponseDTO mapearADto(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNombreUsuario(),
                usuario.getEmail(),
                usuario.isEstaBloqueado(),
                usuario.getRoles() != null
                        ? usuario.getRoles().stream().map(Rol::getNombreRol).collect(Collectors.toList())
                        : Collections.emptyList());
    }
}
