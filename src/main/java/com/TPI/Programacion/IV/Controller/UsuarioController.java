package com.TPI.Programacion.IV.Controller;

import com.TPI.Programacion.IV.DTO.PujaResponseDTO;
import com.TPI.Programacion.IV.DTO.SubastaResponseDTO;
import com.TPI.Programacion.IV.DTO.UsuarioRequestDTO;
import com.TPI.Programacion.IV.DTO.UsuarioResponseDTO;
import com.TPI.Programacion.IV.Model.Puja;
import com.TPI.Programacion.IV.Repository.PujaRepository;
import com.TPI.Programacion.IV.Service.SubastaService;
import com.TPI.Programacion.IV.Service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired private UsuarioService usuarioService;
    @Autowired private SubastaService subastaService;
    @Autowired private PujaRepository pujaRepository;

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> registrarUsuario(@Valid @RequestBody UsuarioRequestDTO request) {
        return new ResponseEntity<>(usuarioService.registrarUsuario(request), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioResponseDTO>> listarUsuarios() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> obtenerUsuarioPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtenerPorId(id));
    }

    @GetMapping("/{id}/subastas")
    public ResponseEntity<List<SubastaResponseDTO>> obtenerSubastasDelUsuario(@PathVariable Long id) {
        return ResponseEntity.ok(subastaService.listarPorVendedor(id));
    }

    @GetMapping("/{id}/pujas")
    public ResponseEntity<List<PujaResponseDTO>> obtenerPujasDelUsuario(@PathVariable Long id) {
        List<Puja> pujas = pujaRepository.findByUsuarioIdOrderByFechaPujaDesc(id);
        List<PujaResponseDTO> dto = pujas.stream()
                .map(p -> new PujaResponseDTO(
                        p.getId(), p.getMonto(), p.getFechaPuja(),
                        p.getUsuario().getId(), p.getUsuario().getNombreUsuario()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}/bloquear")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponseDTO> bloquearUsuario(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.bloquear(id));
    }

    @PutMapping("/{id}/desbloquear")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponseDTO> desbloquearUsuario(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.desbloquear(id));
    }
}
