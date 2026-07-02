package com.TPI.Programacion.IV.Controller;

import com.TPI.Programacion.IV.Exception.RecursoNoEncontradoException;
import com.TPI.Programacion.IV.Model.Usuario;
import com.TPI.Programacion.IV.Repository.UsuarioRepository;
import com.TPI.Programacion.IV.Security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private UserDetailsService userDetailsService;
    @Autowired private JwtUtils jwtUtils;
    @Autowired private UsuarioRepository usuarioRepository;

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

        final UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        final String jwt = jwtUtils.generateToken(userDetails);

        Usuario usuario = usuarioRepository.findByNombreUsuario(username)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return Map.of(
                "token", jwt,
                "id", usuario.getId(),
                "username", usuario.getNombreUsuario(),
                "email", usuario.getEmail(),
                "roles", roles
        );
    }
}
