package com.TPI.Programacion.IV.Controller;

import com.TPI.Programacion.IV.DTO.ReclamoDisputaRequestDTO;
import com.TPI.Programacion.IV.DTO.ReclamoDisputaResponseDTO;
import com.TPI.Programacion.IV.Service.ReclamoDisputaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reclamos")
public class ReclamoDisputaController {

    @Autowired
    private ReclamoDisputaService reclamoService;

    @PostMapping
    public ResponseEntity<ReclamoDisputaResponseDTO> abrirReclamo(
            @Valid @RequestBody ReclamoDisputaRequestDTO request,
            @RequestParam Long usuarioId) {
        ReclamoDisputaResponseDTO nuevoReclamo = reclamoService.crearReclamo(request, usuarioId);
        return new ResponseEntity<>(nuevoReclamo, HttpStatus.CREATED);
    }

    @PutMapping("/{id}/resolver")
    public ResponseEntity<ReclamoDisputaResponseDTO> resolverReclamo(
            @PathVariable Long id,
            @RequestParam String resolucion) {
        ReclamoDisputaResponseDTO resuelto = reclamoService.resolver(id, resolucion);
        return ResponseEntity.ok(resuelto);
    }
}