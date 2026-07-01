package com.elternero.api_leche_agria.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "ordenes")
public class Orden {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "mesa_id")
    private Mesa mesa;

    private LocalDateTime fecha = LocalDateTime.now();

    private String notas;

    private String estado = "PENDIENTE";

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "orden_id")
    private List<DetalleOrden> items;

    private Double total = 0.0;

    private String metodoPago;
}
