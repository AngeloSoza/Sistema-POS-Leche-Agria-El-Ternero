package com.elternero.api_leche_agria.repositories;

import com.elternero.api_leche_agria.entities.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
} 
