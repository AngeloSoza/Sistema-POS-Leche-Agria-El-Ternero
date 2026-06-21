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

    @PostMapping("/{mesaId}/enviar")
    public Orden enviarPedido(@PathVariable Long mesaId, @RequestBody Map<String, Object> payload) {
        Mesa mesa = mesaRepository.findById(mesaId).orElseThrow();

        // Buscamos orden activa para esta mesa
        Orden orden = ordenRepository.findAll().stream()
                .filter(o -> o.getMesa().getId().equals(mesaId) && !"PAGADO".equals(o.getEstado()))
                .findFirst()
                .orElse(new Orden());

        orden.setMesa(mesa);
        String notasNuevas = (String) payload.get("notas");
        
        if (orden.getId() != null) {
            // SI YA EXISTE UNA ORDEN: Marcamos lo que ya estaba como entregado visualmente
            String historial = orden.getNotas();
            // Limpiamos marcas de "NUEVO" previas del historial si existen
            historial = historial.replace("⭐ [NUEVO] ", "✅ [ENTREGADO] ");
            
            // Añadimos lo nuevo con una marca especial
            String nuevasConMarca = notasNuevas.replace("- ", "⭐ [NUEVO] - ");
            orden.setNotas(historial + "\n\n--- ADICIÓN SOLICITADA ---\n" + nuevasConMarca);
        } else {
            // SI ES NUEVA: Marcamos todo como nuevo
            orden.setNotas(notasNuevas.replace("- ", "⭐ [NUEVO] - "));
        }

        orden.setEstado("PENDIENTE");

        Object totalNuevoObj = payload.get("total");
        Double totalAdicional = 0.0;
        if (totalNuevoObj instanceof Number) {
            totalAdicional = ((Number) totalNuevoObj).doubleValue();
        }
        orden.setTotal((orden.getTotal() != null ? orden.getTotal() : 0.0) + totalAdicional);

        mesa.setEstado("OCUPADA");
        mesa.setTotal(orden.getTotal());
        mesaRepository.save(mesa);

        return ordenRepository.save(orden);
    }

    @PostMapping("/{id}/editar-manual")
    public Orden editarManual(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        Orden orden = ordenRepository.findById(id).orElseThrow();
        if (payload.containsKey("notas")) orden.setNotas((String) payload.get("notas"));
        if (payload.containsKey("total")) {
            Object t = payload.get("total");
            if (t instanceof Number) orden.setTotal(((Number) t).doubleValue());
        }
        orden.setEstado("PENDIENTE");
        Mesa mesa = orden.getMesa();
        mesa.setTotal(orden.getTotal());
        mesaRepository.save(mesa);
        return ordenRepository.save(orden);
    }
}
