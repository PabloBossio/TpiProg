package com.TPI.Programacion.IV.Controller;

import com.TPI.Programacion.IV.DTO.ReclamoDisputaRequestDTO;
import com.TPI.Programacion.IV.DTO.ReclamoDisputaResponseDTO;
import com.TPI.Programacion.IV.DTO.ReclamoResolucionRequestDTO;
import com.TPI.Programacion.IV.Service.ReclamoDisputaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reclamos")
public class ReclamoDisputaController {

    @Autowired
    private ReclamoDisputaService reclamoService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReclamoDisputaResponseDTO>> listarTodos() {
        return ResponseEntity.ok(reclamoService.listarTodos());
    }

    @PostMapping
    public ResponseEntity<ReclamoDisputaResponseDTO> abrirReclamo(
            @Valid @RequestBody ReclamoDisputaRequestDTO request,
            @RequestParam Long usuarioId) {
        return new ResponseEntity<>(reclamoService.crearReclamo(request, usuarioId), HttpStatus.CREATED);
    }

    @PutMapping("/{id}/resolver")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReclamoDisputaResponseDTO> resolverReclamo(
            @PathVariable Long id,
            @Valid @RequestBody ReclamoResolucionRequestDTO request) {
        return ResponseEntity.ok(reclamoService.resolver(id, request.aceptado(), request.comentario()));
    }
}
