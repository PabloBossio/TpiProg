package com.TPI.Programacion.IV.Service;

import com.TPI.Programacion.IV.DTO.*;
import com.TPI.Programacion.IV.Model.*;
import com.TPI.Programacion.IV.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class SubastaService {

    @Autowired
    private SubastaRepository subastaRepository;

    @Autowired
    private CategoriaRepository categoryRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional
    public SubastaResponseDTO crearSubasta(SubastaRequestDTO request, Long vendedorId) {
        // 1. Buscar relaciones obligatorias
        Categoria categoria = categoryRepository.findById(request.categoriaId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        Usuario vendedor = usuarioRepository.findById(vendedorId)
                .orElseThrow(() -> new RuntimeException("Vendedor no encontrado"));

        Producto producto = new Producto();
        producto.setNombre(request.producto().nombre());
        producto.setDescripcion(request.producto().descripcion());


        Subasta subasta = new Subasta();
        subasta.setPrecioBase(request.precioBase());
        subasta.setMontoActual(request.precioBase());
        subasta.setFechaInicio(request.fechaInicio());
        subasta.setFechaCierre(request.fechaCierre());
        subasta.setIncrementoMinimo(request.incrementoMinimo());
        subasta.setDescripcion(request.descripcion());
        subasta.setEstado(EstadoSubasta.BORRADOR);

        subasta.setCategoria(categoria);
        subasta.setVendedor(vendedor);
        subasta.setProducto(producto);

        if (!subasta.validarPeriodoMaximo()) {
            throw new IllegalArgumentException("El período de la subasta excede el máximo permitido de 2 semanas.");
        }

        Subasta guardada = subastaRepository.save(subasta);
        return mapearADto(guardada);
    }

    @Transactional
    public SubastaResponseDTO procesarPuja(Long subastaId, Long usuarioOferenteId, PujaRequestDTO pujaRequest) {
        Subasta subasta = subastaRepository.findById(subastaId)
                .orElseThrow(() -> new RuntimeException("Subasta no encontrada"));
        Usuario oferente = usuarioRepository.findById(usuarioOferenteId)
                .orElseThrow(() -> new RuntimeException("Usuario oferente no encontrado"));

        boolean esValida = subasta.validarNuevaPuja(pujaRequest.monto(), oferente);
        if (!esValida) {
            throw new IllegalStateException("La puja no cumple con los requisitos mínimos o la subasta no está activa.");
        }

        Puja nuevaPuja = new Puja();
        nuevaPuja.setMonto(pujaRequest.monto());
        nuevaPuja.setFechaPuja(LocalDateTime.now(ZoneOffset.UTC));
        nuevaPuja.setUs(oferente);
        nuevaPuja.setSubasta(subasta);

        subasta.getPujas().add(nuevaPuja);
        subasta.setMontoActual(pujaRequest.monto());
        subasta.setGanador(oferente); // Va ganando el último en ofertar de forma válida

        Subasta actualizada = subastaRepository.save(subasta);
        return mapearADto(actualizada);
    }

    private SubastaResponseDTO mapearADto(Subasta s) {
        ProductoResponseDTO prodDto = new ProductoResponseDTO(s.getProducto().getId(), s.getProducto().getNombre(), s.getProducto().getDescripcion());
        CategoriaResponseDTO catDto = new CategoriaResponseDTO(s.getCategoria().getId(), s.getCategoria().getNombre());

        Long ganadorId = s.getGanador() != null ? s.getGanador().getId() : null;
        String ganadorNombre = s.getGanador() != null ? s.getGanador().getNombreUsuario() : "Sin pujas";

        return new SubastaResponseDTO(
                s.getId(), s.getPrecioBase(), s.getMontoActual(), s.getFechaInicio(), s.getFechaCierre(),
                s.getIncrementoMinimo(), s.getDescripcion(), s.getEstado(),
                prodDto, catDto, s.getVendedor().getId(), s.getVendedor().getNombreUsuario(),
                ganadorId, ganadorNombre
        );
    }
}