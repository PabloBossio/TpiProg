package com.TPI.Programacion.IV.Controller;

import com.TPI.Programacion.IV.DTO.PujaRequestDTO;
import com.TPI.Programacion.IV.DTO.SubastaRequestDTO;
import com.TPI.Programacion.IV.DTO.SubastaResponseDTO;
import com.TPI.Programacion.IV.Service.SubastaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subastas")
public class SubastaController {

    @Autowired
    private SubastaService subastaService;

    @GetMapping
    public ResponseEntity<List<SubastaResponseDTO>> listarSubastas(
            @RequestParam(required = false) String estado) {
        return ResponseEntity.ok(subastaService.listarSubastas(estado));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubastaResponseDTO> obtenerSubasta(@PathVariable Long id) {
        return ResponseEntity.ok(subastaService.obtenerPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    public ResponseEntity<SubastaResponseDTO> crearSubasta(
            @Valid @RequestBody SubastaRequestDTO request,
            @RequestParam Long vendedorId) {
        return new ResponseEntity<>(subastaService.crearSubasta(request, vendedorId), HttpStatus.CREATED);
    }

    @PostMapping("/{id}/pujar")
    @PreAuthorize("hasAnyRole('USER','SELLER','ADMIN')")
    public ResponseEntity<SubastaResponseDTO> realizarPuja(
            @PathVariable Long id,
            @RequestParam Long oferenteId,
            @Valid @RequestBody PujaRequestDTO pujaRequest) {
        return ResponseEntity.ok(subastaService.procesarPuja(id, oferenteId, pujaRequest));
    }

    @PutMapping("/{id}/publicar")
    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    public ResponseEntity<SubastaResponseDTO> publicarSubasta(@PathVariable Long id) {
        return ResponseEntity.ok(subastaService.publicarSubasta(id));
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<SubastaResponseDTO> cancelarSubasta(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String motivo = body.getOrDefault("motivo", "Sin motivo especificado");
        return ResponseEntity.ok(subastaService.cancelarSubasta(id, motivo));
    }
}
