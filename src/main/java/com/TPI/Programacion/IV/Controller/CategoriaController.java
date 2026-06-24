package com.TPI.Programacion.IV.Controller;

import com.TPI.Programacion.IV.DTO.CategoriaRequestDTO;
import com.TPI.Programacion.IV.DTO.CategoriaResponseDTO;
import com.TPI.Programacion.IV.Service.CategoriaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    @PostMapping
    public ResponseEntity<CategoriaResponseDTO> crearCategoria(@Valid @RequestBody CategoriaRequestDTO request) {
        CategoriaResponseDTO nueva = categoriaService.crear(request);
        return new ResponseEntity<>(nueva, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<CategoriaResponseDTO>> listarCategorias() {
        return ResponseEntity.ok(categoriaService.listarTodas());
    }
}
