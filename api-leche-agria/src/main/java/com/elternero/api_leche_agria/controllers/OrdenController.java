package com.elternero.api_leche_agria.controllers;

import com.elternero.api_leche_agria.entities.Mesa;
import com.elternero.api_leche_agria.entities.Orden;
import com.elternero.api_leche_agria.repositories.MesaRepository;
import com.elternero.api_leche_agria.repositories.OrdenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ordenes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OrdenController {

    private final OrdenRepository ordenRepository;
    private final MesaRepository mesaRepository;

    @GetMapping
    public List<Orden> obtenerTodasLasOrdenes() {
        return ordenRepository.findAll();
    }

    @GetMapping("/estado/{estado}")
    public List<Orden> obtenerOrdenesPorEstado(@PathVariable String estado) {
        return ordenRepository.findAll().stream()
                .filter(o -> o.getEstado().equalsIgnoreCase(estado))
                .toList();
    }

    @PatchMapping("/{id}/estado")
    public Orden actualizarEstado(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Orden orden = ordenRepository.findById(id).orElseThrow();
        String nuevoEstado = body.get("estado");
        orden.setEstado(nuevoEstado);

        if ("PAGADO".equals(nuevoEstado)) {
            Mesa mesa = orden.getMesa();
            mesa.setEstado("LIBRE");
            mesa.setTotal(0.0);
            mesaRepository.save(mesa);
        }

        return ordenRepository.save(orden);
    }

    @PostMapping("/{id}/actualizar")
    public Orden actualizarOrden(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        Orden orden = ordenRepository.findById(id).orElseThrow();
        
        if (payload.containsKey("notas")) {
            orden.setNotas((String) payload.get("notas"));
        }
        if (payload.containsKey("total")) {
            Object totalObj = payload.get("total");
            if (totalObj instanceof Number) {
                orden.setTotal(((Number) totalObj).doubleValue());
            }
        }
        
        orden.setEstado("PENDIENTE");
        
        Mesa mesa = orden.getMesa();
        mesa.setTotal(orden.getTotal());
        mesaRepository.save(mesa);
        
        return ordenRepository.save(orden);
    }

    @PostMapping("/{mesaId}")
    public Orden crearOActualizarOrden(@PathVariable Long mesaId, @RequestBody Map<String, Object> payload) {
        Mesa mesa = mesaRepository.findById(mesaId).orElseThrow();

        Orden orden = ordenRepository.findAll().stream()
                .filter(o -> o.getMesa().getId().equals(mesaId) && !"PAGADO".equals(o.getEstado()))
                .findFirst()
                .orElse(new Orden());

        orden.setMesa(mesa);

        String notasNuevas = (String) payload.get("notas");
        if (orden.getId() != null) {
            orden.setNotas(orden.getNotas() + "\n--- ACTUALIZACIÓN ---\n" + notasNuevas);
        } else {
            orden.setNotas(notasNuevas);
        }
        
        orden.setEstado("PENDIENTE");

        Object totalNuevoObj = payload.get("total");
        Double totalNuevo = 0.0;
        if (totalNuevoObj instanceof Number) {
            totalNuevo = ((Number) totalNuevoObj).doubleValue();
        }
        orden.setTotal((orden.getTotal() != null ? orden.getTotal() : 0.0) + totalNuevo);

        mesa.setEstado("OCUPADA");
        mesa.setTotal(orden.getTotal());
        mesaRepository.save(mesa);

        return ordenRepository.save(orden);
    }
}
