package com.elternero.api_leche_agria.controllers;

import com.elternero.api_leche_agria.entities.Orden;
import com.elternero.api_leche_agria.repositories.OrdenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/estadisticas")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EstadisticasController {

    private final OrdenRepository ordenRepository;

    @GetMapping("/hoy")
    public Map<String, Object> getEstadisticasHoy() {
        List<Orden> ordenesPagadas = ordenRepository.findAll().stream()
                .filter(o -> "PAGADO".equals(o.getEstado()))
                .toList();

        double totalVentas = ordenesPagadas.stream().mapToDouble(Orden::getTotal).sum();
        int tickets = ordenesPagadas.size();
        double promedio = tickets > 0 ? totalVentas / tickets : 0.0;

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalVentas", totalVentas);
        stats.put("ticketsEmitidos", tickets);
        stats.put("ticketPromedio", promedio);
        stats.put("topProductos", List.of());

        return stats;
    }
}
