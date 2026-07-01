package com.lecheagriaelternero.model

import com.google.gson.annotations.SerializedName

data class Mesa(
    @SerializedName("id") val id: String = "",
    @SerializedName("numero") val numero: String = "",
    @SerializedName("estado") val estado: String = "Libre",
    @SerializedName("tiempo") val tiempo: String = "",
    @SerializedName("comensales") val comensales: String = "",
    @SerializedName("total") val total: Double = 0.0
)

data class Producto(
    @SerializedName("id") val id: String = "",
    @SerializedName("nombre") val nombre: String = "",
    @SerializedName("descripcion") val descripcion: String = "",
    @SerializedName("precioBase") val precio: Double = 0.0,
    @SerializedName("categoria") val categoriaObj: CategoriaBackend? = null,
    @SerializedName("imagenUrl") val imagenUrl: String = "",
    @SerializedName("disponible") var disponible: Boolean = true
) {
    val categoria: String get() = categoriaObj?.nombre ?: "Sin Categoría"
}

data class CategoriaBackend(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("nombre") val nombre: String = ""
)

data class EstadisticasDia(
    @SerializedName("totalVentas") val totalVentas: Double = 0.0,
    @SerializedName("ventasTransferencia") val ventasTransferencia: Double = 0.0,
    @SerializedName("ticketsEmitidos") val ticketsEmitidos: Int = 0,
    @SerializedName("ticketPromedio") val ticketPromedio: Double = 0.0,
    @SerializedName("topProductos") val topProductos: List<ProductoTop> = emptyList()
)

data class ProductoTop(
    @SerializedName("id") val id: String = "",
    @SerializedName("nombre") val nombre: String = "",
    @SerializedName("unidadesVendidas") val unidadesVendidas: Int = 0,
    @SerializedName("totalGenerado") val totalGenerado: Double = 0.0
)

data class OrdenBackend(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("mesa") val mesa: Mesa? = null,
    @SerializedName("notas") val notas: String? = "",
    @SerializedName("estado") val estado: String = "PENDIENTE",
    @SerializedName("total") val total: Double = 0.0,
    @SerializedName("metodoPago") val metodoPago: String? = "Efectivo"
)

data class OrdenPayload(
    val notas: String,
    val total: Double,
    val detalles: List<DetallePayload>
)

data class DetallePayload(
    val productoId: Long,
    val cantidad: Int,
    val precioUnitario: Double
)