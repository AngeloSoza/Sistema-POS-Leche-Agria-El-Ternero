package com.lecheagriaelternero.model

data class Pago(
    val id: Long? = null,
    val orden: OrdenBackend, // Aquí está la corrección clave
    val sesionCaja: SesionCaja,
    val monto: Double,
    val metodoPago: String,
    val fechaPago: String? = null
)