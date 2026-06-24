package com.TPI.Programacion.IV.Controller;

import com.TPI.Programacion.IV.DTO.ProductoRequestDTO;
import com.TPI.Programacion.IV.DTO.ProductoResponseDTO;
import com.TPI.Programacion.IV.Service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> actualizarProducto(
            @PathVariable Long id,
            @Valid @RequestBody ProductoRequestDTO request) {
        ProductoResponseDTO actualizado = productoService.actualizar(id, request);
        return ResponseEntity.ok(actualizado);
    }
}
