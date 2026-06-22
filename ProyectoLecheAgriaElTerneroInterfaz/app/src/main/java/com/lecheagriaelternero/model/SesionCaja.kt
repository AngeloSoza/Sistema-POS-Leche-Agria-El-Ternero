package com.lecheagriaelternero.model

data class SesionCaja(
    val id: Long? = null,
    val fechaApertura: String? = null,
    val fechaCierre: String? = null,
    val montoInicial: Double,
    val montoFinal: Double? = null,
    val diferencia: Double? = null,
    val estado: String = "ABIERTA"
)