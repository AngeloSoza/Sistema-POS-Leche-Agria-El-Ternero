package com.elternero.api_leche_agria.controllers;

import com.elternero.api_leche_agria.entities.Producto;
import com.elternero.api_leche_agria.repositories.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MenuController {

    private final ProductoRepository productoRepository;

    @GetMapping
    public List<Producto> obtenerMenu() {
        return productoRepository.findAll();
    }

    @PostMapping
    public Producto guardarProducto(@RequestBody Producto producto) {
        System.out.println("Intentando guardar producto: " + producto.getNombre());
        if (producto.getCategoria() != null) {
            System.out.println("Categoría recibida: ID=" + producto.getCategoria().getId());
        } else {
            System.out.println("ADVERTENCIA: Categoría es NULL");
        }
        return productoRepository.save(producto);
    }

    @PutMapping("/{id}")
    public Producto actualizarProducto(@PathVariable Long id, @RequestBody Producto productoActualizado) {
        return productoRepository.findById(id).map(producto -> {
            producto.setNombre(productoActualizado.getNombre());
            producto.setDescripcion(productoActualizado.getDescripcion());
            producto.setPrecioBase(productoActualizado.getPrecioBase());
            producto.setDisponible(productoActualizado.getDisponible());

            if (productoActualizado.getCategoria() != null) {
                producto.setCategoria(productoActualizado.getCategoria());
            }
            return productoRepository.save(producto);
        }).orElseThrow(() -> new RuntimeException("Producto no encontrado"));
    }

    @DeleteMapping("/{id}")
    public void eliminarProducto(@PathVariable Long id) {
        productoRepository.deleteById(id);
    }
}
