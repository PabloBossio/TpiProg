package com.TPI.Programacion.IV.Service;


import com.TPI.Programacion.IV.DTO.ProductoRequestDTO;
import com.TPI.Programacion.IV.DTO.ProductoResponseDTO;
import com.TPI.Programacion.IV.Exception.RecursoNoEncontradoException;
import com.TPI.Programacion.IV.Model.Producto;
import com.TPI.Programacion.IV.Repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    public Producto crear(Producto producto){

        if(producto.getNombre() == null || producto.getNombre().isEmpty()){
            throw new IllegalArgumentException("El producto debe tener nombre");
        }

        return productoRepository.save(producto);
    }

    public List<Producto> listar(){
        return productoRepository.findAll();
    }

    public Producto buscarPorId(Long id){

        return productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado"));
    }

    public Producto editar(Long id, Producto productoActualizado){

        Producto producto = buscarPorId(id);

        producto.setNombre(productoActualizado.getNombre());
        producto.setDescripcion(productoActualizado.getDescripcion());

        return productoRepository.save(producto);
    }

    public void eliminar(Long id){

        Producto producto = buscarPorId(id);

        productoRepository.delete(producto);
    }

    public ProductoResponseDTO actualizar(Long id, ProductoRequestDTO request) {
        Producto producto = buscarPorId(id);
        producto.setNombre(request.nombre());
        producto.setDescripcion(request.descripcion());
        producto.setImagenUrl(request.imagenUrl());
        Producto guardado = productoRepository.save(producto);
        return new ProductoResponseDTO(guardado.getId(), guardado.getNombre(),
                guardado.getDescripcion(), guardado.getImagenUrl());
    }
}