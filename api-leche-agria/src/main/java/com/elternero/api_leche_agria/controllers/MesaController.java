package com.elternero.api_leche_agria.controllers;

import com.elternero.api_leche_agria.entities.Mesa;
import com.elternero.api_leche_agria.repositories.MesaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mesas")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MesaController {

    private final MesaRepository mesaRepository;

    @GetMapping
    public List<Mesa> obtenerMesas() {
        return mesaRepository.findAll();
    }

    @PostMapping
    public Mesa guardarMesa(@RequestBody Mesa mesa) {
        return mesaRepository.save(mesa);
    }
}
