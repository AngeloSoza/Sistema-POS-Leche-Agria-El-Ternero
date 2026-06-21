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
        @PostMapping("/{id}/actualizar")
    public Orden actualizarOrden(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        Orden orden = ordenRepository.findById(id).orElseThrow();
        
        if (payload.containsKey("notas")) {
            orden.setNotas((String) payload.get("notas"));
        }
        if (payload.containsKey("total")) {
            // Manejar diferentes tipos numéricos posibles desde JSON
            Object totalObj = payload.get("total");
            if (totalObj instanceof Number) {
                orden.setTotal(((Number) totalObj).doubleValue());
            }
        }
        // Al editar manualmente, la dejamos en PENDIENTE por si acaso
        orden.setEstado("PENDIENTE");
        
        Mesa mesa = orden.getMesa();
        mesa.setTotal(orden.getTotal());
        mesaRepository.save(mesa);
        
        return ordenRepository.save(orden);
    }
}

    @GetMapping("/estado/{estado}")
    public List<Orden> obtenerOrdenesPorEstado(@PathVariable String estado) {
        return ordenRepository.findAll().stream()
                .filter(o -> o.getEstado().equalsIgnoreCase(estado))
                .toList();
        @PostMapping("/{id}/actualizar")
    public Orden actualizarOrden(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        Orden orden = ordenRepository.findById(id).orElseThrow();
        
        if (payload.containsKey("notas")) {
            orden.setNotas((String) payload.get("notas"));
        }
        if (payload.containsKey("total")) {
            // Manejar diferentes tipos numéricos posibles desde JSON
            Object totalObj = payload.get("total");
            if (totalObj instanceof Number) {
                orden.setTotal(((Number) totalObj).doubleValue());
            }
        }
        // Al editar manualmente, la dejamos en PENDIENTE por si acaso
        orden.setEstado("PENDIENTE");
        
        Mesa mesa = orden.getMesa();
        mesa.setTotal(orden.getTotal());
        mesaRepository.save(mesa);
        
        return ordenRepository.save(orden);
    }
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
            @PostMapping("/{id}/actualizar")
    public Orden actualizarOrden(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        Orden orden = ordenRepository.findById(id).orElseThrow();
        
        if (payload.containsKey("notas")) {
            orden.setNotas((String) payload.get("notas"));
        }
        if (payload.containsKey("total")) {
            // Manejar diferentes tipos numéricos posibles desde JSON
            Object totalObj = payload.get("total");
            if (totalObj instanceof Number) {
                orden.setTotal(((Number) totalObj).doubleValue());
            }
        }
        // Al editar manualmente, la dejamos en PENDIENTE por si acaso
        orden.setEstado("PENDIENTE");
        
        Mesa mesa = orden.getMesa();
        mesa.setTotal(orden.getTotal());
        mesaRepository.save(mesa);
        
        return ordenRepository.save(orden);
    }
}

        return ordenRepository.save(orden);
        @PostMapping("/{id}/actualizar")
    public Orden actualizarOrden(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        Orden orden = ordenRepository.findById(id).orElseThrow();
        
        if (payload.containsKey("notas")) {
            orden.setNotas((String) payload.get("notas"));
        }
        if (payload.containsKey("total")) {
            // Manejar diferentes tipos numéricos posibles desde JSON
            Object totalObj = payload.get("total");
            if (totalObj instanceof Number) {
                orden.setTotal(((Number) totalObj).doubleValue());
            }
        }
        // Al editar manualmente, la dejamos en PENDIENTE por si acaso
        orden.setEstado("PENDIENTE");
        
        Mesa mesa = orden.getMesa();
        mesa.setTotal(orden.getTotal());
        mesaRepository.save(mesa);
        
        return ordenRepository.save(orden);
    }
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
            // Si la orden ya existe, concatenamos las notas con un separador visual
            orden.setNotas(orden.getNotas() + "\n--- ACTUALIZACIÓN ---\n" + notasNuevas);
            // IMPORTANTE: Volvemos a poner la orden en PENDIENTE para que cocina la vea
            orden.setEstado("PENDIENTE");
        } else {
            orden.setNotas(notasNuevas);
            orden.setEstado("PENDIENTE");
            @PostMapping("/{id}/actualizar")
    public Orden actualizarOrden(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        Orden orden = ordenRepository.findById(id).orElseThrow();
        
        if (payload.containsKey("notas")) {
            orden.setNotas((String) payload.get("notas"));
        }
        if (payload.containsKey("total")) {
            // Manejar diferentes tipos numéricos posibles desde JSON
            Object totalObj = payload.get("total");
            if (totalObj instanceof Number) {
                orden.setTotal(((Number) totalObj).doubleValue());
            }
        }
        // Al editar manualmente, la dejamos en PENDIENTE por si acaso
        orden.setEstado("PENDIENTE");
        
        Mesa mesa = orden.getMesa();
        mesa.setTotal(orden.getTotal());
        mesaRepository.save(mesa);
        
        return ordenRepository.save(orden);
    }
}

        Double totalNuevo = (Double) payload.get("total");
        orden.setTotal((orden.getTotal() != null ? orden.getTotal() : 0.0) + totalNuevo);

        mesa.setEstado("OCUPADA");
        mesa.setTotal(orden.getTotal());
        mesaRepository.save(mesa);

        return ordenRepository.save(orden);
        @PostMapping("/{id}/actualizar")
    public Orden actualizarOrden(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        Orden orden = ordenRepository.findById(id).orElseThrow();
        
        if (payload.containsKey("notas")) {
            orden.setNotas((String) payload.get("notas"));
        }
        if (payload.containsKey("total")) {
            // Manejar diferentes tipos numéricos posibles desde JSON
            Object totalObj = payload.get("total");
            if (totalObj instanceof Number) {
                orden.setTotal(((Number) totalObj).doubleValue());
            }
        }
        // Al editar manualmente, la dejamos en PENDIENTE por si acaso
        orden.setEstado("PENDIENTE");
        
        Mesa mesa = orden.getMesa();
        mesa.setTotal(orden.getTotal());
        mesaRepository.save(mesa);
        
        return ordenRepository.save(orden);
    }
}
    @PatchMapping("/{id}")
    public Orden actualizarOrden(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        Orden orden = ordenRepository.findById(id).orElseThrow();
        
        if (payload.containsKey("notas")) {
            orden.setNotas((String) payload.get("notas"));
        }
        if (payload.containsKey("total")) {
            // Manejar diferentes tipos numéricos posibles desde JSON
            Number total = (Number) payload.get("total");
            orden.setTotal(total.doubleValue());
        }
        // Al editar manualmente, la dejamos en PENDIENTE por si acaso
        orden.setEstado("PENDIENTE");
        
        Mesa mesa = orden.getMesa();
        mesa.setTotal(orden.total);
        mesaRepository.save(mesa);

        return ordenRepository.save(orden);
    }
}
