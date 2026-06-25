package com.TPI.Programacion.IV.Controller;

import com.TPI.Programacion.IV.DTO.PujaRequestDTO;
import com.TPI.Programacion.IV.DTO.SubastaRequestDTO;
import com.TPI.Programacion.IV.DTO.SubastaResponseDTO;
import com.TPI.Programacion.IV.Service.SubastaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subastas")
public class SubastaController {

    @Autowired
    private SubastaService subastaService;

    @PostMapping
    public ResponseEntity<SubastaResponseDTO> crearSubasta(
            @Valid @RequestBody SubastaRequestDTO request,
            @RequestParam Long vendedorId) {
        SubastaResponseDTO nuevaSubasta = subastaService.crearSubasta(request, vendedorId);
        return new ResponseEntity<>(nuevaSubasta, HttpStatus.CREATED);
    }

    @PostMapping("/{id}/pujar")
    public ResponseEntity<SubastaResponseDTO> realizarPuja(
            @PathVariable Long id,
            @RequestParam Long oferenteId,
            @Valid @RequestBody PujaRequestDTO pujaRequest) {
        SubastaResponseDTO subastaActualizada = subastaService.procesarPuja(id, oferenteId, pujaRequest);
        return ResponseEntity.ok(subastaActualizada);
    }
}