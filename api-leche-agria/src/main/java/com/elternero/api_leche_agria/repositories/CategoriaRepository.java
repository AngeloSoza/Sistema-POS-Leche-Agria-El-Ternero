package com.elternero.api_leche_agria.repositories;

import com.elternero.api_leche_agria.entities.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
}