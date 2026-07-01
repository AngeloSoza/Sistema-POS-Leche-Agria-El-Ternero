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

    @PatchMapping("/{id}/estado")
    public Orden actualizarEstado(@PathVariable("id") Long id, @RequestBody Map<String, String> body) {
        Orden orden = ordenRepository.findById(id).orElseThrow();
        String nuevoEstado = body.get("estado");
        orden.setEstado(nuevoEstado);

        if (body.containsKey("metodoPago")) {
            orden.setMetodoPago(body.get("metodoPago"));
        }

        // 🛡️ SOLUCIÓN: Guardamos la orden primero para asegurar persistencia
        orden = ordenRepository.save(orden);

        if ("PAGADO".equals(nuevoEstado)) {
            Mesa mesa = orden.getMesa();
            if (mesa != null) {
                mesa.setEstado("DISPONIBLE"); // Cambiado de LIBRE a DISPONIBLE para coincidir con el SQL
                mesa.setTotal(0.0);
                mesaRepository.save(mesa);
            }
        }

        return orden;
    }

    @PostMapping("/enviar-pedido/{mesaId}")
    public Orden enviarPedido(@PathVariable("mesaId") Long mesaId, @RequestBody Map<String, Object> payload) {
        Mesa mesa = mesaRepository.findById(mesaId).orElseThrow();

        Orden orden = ordenRepository.findAll().stream()
                .filter(o -> o.getMesa().getId().equals(mesaId) && !"PAGADO".equals(o.getEstado()))
                .findFirst()
                .orElse(new Orden());

        orden.setMesa(mesa);
        String notasNuevas = (String) payload.get("notas");
        
        if (orden.getId() != null) {
            String historial = orden.getNotas() != null ? orden.getNotas() : "";
            historial = historial.replace("⭐ [NUEVO] ", "✅ [ENTREGADO] ");
            String nuevasConMarca = notasNuevas.replace("- ", "⭐ [NUEVO] ");
            orden.setNotas(historial + "\n\n--- ADICIÓN SOLICITADA ---\n" + nuevasConMarca);
        } else {
            orden.setNotas(notasNuevas.replace("- ", "⭐ [NUEVO] "));
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

    @PostMapping("/editar-pedido/{id}")
    public Orden editarManual(@PathVariable("id") Long id, @RequestBody Map<String, Object> payload) {
        Orden orden = ordenRepository.findById(id).orElseThrow();
        if (payload.containsKey("notas")) orden.setNotas((String) payload.get("notas"));
        if (payload.containsKey("total")) {
            Object t = payload.get("total");
            if (t instanceof Number) orden.setTotal(((Number) t).doubleValue());
        }
        orden.setEstado("PENDIENTE");
        Mesa mesa = orden.getMesa();
        if (mesa != null) {
            mesa.setTotal(orden.getTotal());
            mesaRepository.save(mesa);
        }
        return ordenRepository.save(orden);
    }
}
