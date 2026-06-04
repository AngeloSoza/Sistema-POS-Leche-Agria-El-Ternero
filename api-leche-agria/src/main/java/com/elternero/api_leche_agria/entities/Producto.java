package com.elternero.api_leche_agria.entities;

import com.elternero.api_leche_agria.entities.Categoria;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    private String descripcion;

    @Column(nullable = false)
    private Double precioBase;

    @Column(nullable = false)
    private Boolean disponible = true;

    private Boolean requiereNota = false;

    @ManyToOne
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;
}