package com.TPI.Programacion.IV.Controller;

import com.TPI.Programacion.IV.DTO.RolRequestDTO;
import com.TPI.Programacion.IV.DTO.RolResponseDTO;
import com.TPI.Programacion.IV.Service.RolService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RolController {

    @Autowired
    private RolService rolService;

    @GetMapping
    public ResponseEntity<List<RolResponseDTO>> listarRoles() {
        return ResponseEntity.ok(rolService.listar());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RolResponseDTO> crearRol(@Valid @RequestBody RolRequestDTO request) {
        RolResponseDTO nuevoRol = rolService.crear(request);
        return new ResponseEntity<>(nuevoRol, HttpStatus.CREATED);
    }
}
