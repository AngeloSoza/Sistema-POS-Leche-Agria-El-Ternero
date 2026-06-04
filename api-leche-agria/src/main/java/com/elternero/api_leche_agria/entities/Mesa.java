package com.elternero.api_leche_agria.entities;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "mesas")
public class Mesa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Integer numero;

    @Column(nullable = false)
    private String estado = "LIBRE";

    private Double total = 0.0;
}
