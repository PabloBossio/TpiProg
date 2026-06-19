package com.TPI.Programacion.IV.DTO;

import com.TPI.Programacion.IV.Model.EstadoSubasta;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SubastaResponseDTO(
        Long id,
        BigDecimal precioBase,
        BigDecimal montoActual,
        LocalDateTime fechaInicio,
        LocalDateTime fechaCierre,
        BigDecimal incrementoMinimo,
        String descripcion,
        EstadoSubasta estado,
        ProductoResponseDTO producto,
        CategoriaResponseDTO categoria,
        Long vendedorId,
        String vendedorNombre,
        Long ganadorId,
        String ganadorNombre
) {}