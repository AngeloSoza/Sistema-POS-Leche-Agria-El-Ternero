package com.elternero.api_leche_agria.entities;

import com.elternero.api_leche_agria.entities.Producto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "categorias")
public class Categoria  {

    @Id
    private Long id;

    private String nombre;

    @JsonIgnore
    @OneToMany(mappedBy = "categoria")
    private List<Producto> productos;

    public Categoria() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<Producto> getProductos() {
        return productos;
    }

    public void setProductos(List<Producto> productos) {
        this.productos = productos;
    }
} 
