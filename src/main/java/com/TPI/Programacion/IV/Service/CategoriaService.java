package com.TPI.Programacion.IV.Service;

import com.TPI.Programacion.IV.DTO.CategoriaRequestDTO;
import com.TPI.Programacion.IV.DTO.CategoriaResponseDTO;
import com.TPI.Programacion.IV.Exception.RecursoNoEncontradoException;
import com.TPI.Programacion.IV.Model.Categoria;
import com.TPI.Programacion.IV.Repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Transactional
    public CategoriaResponseDTO crear(CategoriaRequestDTO request) {
        String nombre = request.nombre().trim();

        if (categoriaRepository.findByNombreIgnoreCase(nombre).isPresent()) {
            throw new IllegalArgumentException("Ya existe una categoría con ese nombre.");
        }

        Categoria nuevaCategoria = new Categoria();
        nuevaCategoria.setNombre(nombre);

        Categoria guardada = categoriaRepository.save(nuevaCategoria);

        return mapearADto(guardada);
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> listarTodas() {
        List<Categoria> categorias = categoriaRepository.findAll();

        return categorias.stream()
                .map(this::mapearADto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Categoria buscarPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada con el ID: " + id));
    }

    private CategoriaResponseDTO mapearADto(Categoria categoria) {
        return new CategoriaResponseDTO(
                categoria.getId(),
                categoria.getNombre()
        );
    }
}
