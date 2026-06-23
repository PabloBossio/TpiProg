package com.TPI.Programacion.IV.Service;


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
            throw new RuntimeException("El producto debe tener nombre");
        }

        return productoRepository.save(producto);
    }

    public List<Producto> listar(){
        return productoRepository.findAll();
    }

    public Producto buscarPorId(Long id){

        return productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
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
}