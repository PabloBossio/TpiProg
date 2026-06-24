package com.TPI.Programacion.IV.Controller;

import com.TPI.Programacion.IV.DTO.HistorialEstadoResponseDTO;
import com.TPI.Programacion.IV.Service.HistorialEstadoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/historiales")
public class HistorialEstadoController {

    @Autowired
    private HistorialEstadoService historialService;

    @GetMapping("/subasta/{subastaId}")
    public ResponseEntity<List<HistorialEstadoResponseDTO>> obtenerHistorialSubasta(@PathVariable Long subastaId) {
        return ResponseEntity.ok(historialService.obtenerPorSubasta(subastaId));
    }
}