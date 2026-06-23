package com.TPI.Programacion.IV.Repository;
import com.TPI.Programacion.IV.Model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {}