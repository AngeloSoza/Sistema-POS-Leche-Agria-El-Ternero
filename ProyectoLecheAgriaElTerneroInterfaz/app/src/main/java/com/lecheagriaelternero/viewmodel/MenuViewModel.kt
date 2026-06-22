package com.lecheagriaelternero.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lecheagriaelternero.model.*
import com.lecheagriaelternero.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MenuViewModel : ViewModel() {

    private val _mesas = MutableStateFlow<List<Mesa>>(emptyList())
    val mesas: StateFlow<List<Mesa>> = _mesas

    private val _menu = MutableStateFlow<List<Producto>>(emptyList())
    val menu: StateFlow<List<Producto>> = _menu

    private val _carritoActual = MutableStateFlow<MutableList<Producto>>(mutableListOf())
    val carritoActual: StateFlow<List<Producto>> = _carritoActual

    private val _estadisticas = MutableStateFlow(EstadisticasDia())
    val estadisticas: StateFlow<EstadisticasDia> = _estadisticas

    private val _ordenesActivas = MutableStateFlow<List<OrdenBackend>>(emptyList())
    val ordenesActivas: StateFlow<List<OrdenBackend>> = _ordenesActivas

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState

    private val _mesaSeleccionadaId = MutableStateFlow("")
    val mesaSeleccionadaId: StateFlow<String> = _mesaSeleccionadaId

    private val _personaActual = MutableStateFlow(1)
    val personaActual: StateFlow<Int> = _personaActual

    init {
        cargarMesas()
        cargarMenu()
        cargarEstadisticas()
        cargarOrdenes()
    }

    private val _ordenActivaMesa = MutableStateFlow<OrdenBackend?>(null)
    val ordenActivaMesa: StateFlow<OrdenBackend?> = _ordenActivaMesa

    fun setMesaSeleccionada(id: String) {
        if (_mesaSeleccionadaId.value != id) {
            _mesaSeleccionadaId.value = id
            vaciarCarrito() // Limpiar items nuevos de la sesión anterior

            // Cargar automáticamente la orden de esta mesa si existe
            viewModelScope.launch(Dispatchers.IO) { // INYECTADO: Ejecución en segundo plano
                cargarOrdenes()
                val ordenEncontrada = _ordenesActivas.value.find {
                    // CORRECCIÓN: .toString() para evitar choque de tipos (Long vs String)
                    it.mesa?.id?.toString() == id && it.estado != "PAGADO"
                }
                _ordenActivaMesa.value = ordenEncontrada
            }
        }
    }

    fun cargarMesas() {
        viewModelScope.launch(Dispatchers.IO) { // INYECTADO: Ejecución en segundo plano
            try {
                val lista = RetrofitClient.apiService.getMesas()
                // Ordenar por número de mesa para que no se muevan
                _mesas.value = lista.sortedBy {
                    it.numero.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0
                }
            } catch (e: Exception) { }
        }
    }

    fun cargarMenu() {
        viewModelScope.launch(Dispatchers.IO) { // INYECTADO: Ejecución en segundo plano
            try { _menu.value = RetrofitClient.apiService.getMenu() } catch (e: Exception) { }
        }
    }

    fun cargarEstadisticas() {
        viewModelScope.launch(Dispatchers.IO) { // INYECTADO: Ejecución en segundo plano
            try { _estadisticas.value = RetrofitClient.apiService.getEstadisticasDelDia() } catch (e: Exception) { }
        }
    }

    fun cargarOrdenes() {
        viewModelScope.launch(Dispatchers.IO) { // INYECTADO: Ejecución en segundo plano
            try { _ordenesActivas.value = RetrofitClient.apiService.getOrdenes() } catch (e: Exception) { }
        }
    }

    fun agregarAlCarrito(producto: Producto) {
        val lista = _carritoActual.value.toMutableList()
        lista.add(producto)
        _carritoActual.value = lista
    }

    fun eliminarDelCarrito(producto: Producto) {
        val lista = _carritoActual.value.toMutableList()
        lista.remove(producto)
        _carritoActual.value = lista
    }

    fun vaciarCarrito() {
        _carritoActual.value = mutableListOf()
    }

    fun enviarOrden(notas: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val mesaId = _mesaSeleccionadaId.value
                if (mesaId.isEmpty() || _carritoActual.value.isEmpty()) return@launch

                val totalCarrito = _carritoActual.value.sumOf { it.precio }

                // AGRUPACIÓN INTELIGENTE DE ITEMS
                val itemsLimpios = _carritoActual.value.map {
                    it.copy(
                        nombre = it.nombre.replace(" (Config)", "").trim(),
                        descripcion = it.descripcion.trim()
                    )
                }

                val itemsAgrupados = itemsLimpios.groupBy { "${it.nombre.lowercase()}|${it.descripcion.lowercase()}" }

                // 1. Preparamos el bloque de texto de los productos NUEVOS
                val detalleNuevos = itemsAgrupados.entries.joinToString("\n") { (key, lista) ->
                    val cantidad = lista.size
                    val originalItem = lista.first()
                    val nombre = originalItem.nombre
                    val desc = originalItem.descripcion

                    if (desc.isNotBlank() && desc != "null") {
                        "- ${cantidad}x $nombre\n   $desc"
                    } else {
                        "- ${cantidad}x $nombre"
                    }
                }

                val notasNuevas = if (notas.isBlank()) detalleNuevos else "$detalleNuevos\n\n📝 NOTAS GENERALES: $notas"

                // 🛡️ CORRECCIÓN: Rescatamos el texto de la orden vieja y le pegamos lo nuevo abajo
                val ordenPrevia = _ordenActivaMesa.value
                val textoPrevio = if (ordenPrevia != null && !ordenPrevia.notas.isNullOrBlank()) {
                    "${ordenPrevia.notas}\n"
                } else {
                    ""
                }

                // Unimos el pasado con el presente
                val notaFinal = textoPrevio + notasNuevas

                // 2. Preparamos la estructura JSON para la tabla "detalle_ordenes"
                val detallesPayload = itemsAgrupados.entries.map { (_, lista) ->
                    val originalItem = lista.first()
                    DetallePayload(
                        productoId = originalItem.id.toLongOrNull() ?: 0L,
                        cantidad = lista.size,
                        precioUnitario = originalItem.precio
                    )
                }

                // 3. Enviamos el objeto estructurado
                val payload = OrdenPayload(
                    notas = notaFinal,
                    total = totalCarrito,
                    detalles = detallesPayload
                )

                RetrofitClient.apiService.enviarPedido(mesaId, payload)

                vaciarCarrito()

                // Refresco instantáneo para la vista local
                val ordenesActualizadas = RetrofitClient.apiService.getOrdenes()
                _ordenesActivas.value = ordenesActualizadas
                _ordenActivaMesa.value = ordenesActualizadas.find { it.mesa?.id?.toString() == mesaId && it.estado != "PAGADO" }

                val mesasActualizadas = RetrofitClient.apiService.getMesas()
                _mesas.value = mesasActualizadas.sortedBy { it.numero.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0 }

            } catch (e: Exception) {
                _errorState.value = "Error conectando con la cocina: ${e.message}"
            }
        }
    }

    fun cambiarEstadoOrden(ordenId: Long, nuevoEstado: String) {
        viewModelScope.launch(Dispatchers.IO) { // INYECTADO: Ejecución en segundo plano
            try {
                RetrofitClient.apiService.actualizarEstadoOrden(ordenId.toString(), mapOf("estado" to nuevoEstado))
                cargarOrdenes()
                cargarMesas()
            } catch (e: Exception) {
                _errorState.value = "Error actualizando orden: ${e.message}"
            }
        }
    }

    fun actualizarOrdenManual(ordenId: Long, notas: String, total: Double) {
        viewModelScope.launch(Dispatchers.IO) { // INYECTADO: Ejecución en segundo plano
            try {
                val payload = mapOf("notas" to notas, "total" to total)
                RetrofitClient.apiService.editarManual(ordenId.toString(), payload)

                // Refresco instantáneo tras edición
                val ordenesActualizadas = RetrofitClient.apiService.getOrdenes()
                _ordenesActivas.value = ordenesActualizadas
                val mesaIdLocal = _mesaSeleccionadaId.value
                _ordenActivaMesa.value = ordenesActualizadas.find { it.mesa?.id?.toString() == mesaIdLocal && it.estado != "PAGADO" }
                cargarMesas()

            } catch (e: Exception) {
                _errorState.value = "Error editando orden: ${e.message}"
            }
        }
    }

    fun cobrarMesa(mesaId: String, metodoPago: String) {
        viewModelScope.launch(Dispatchers.IO) { // INYECTADO: Ejecución en segundo plano
            try {
                val payload = mapOf("metodo" to metodoPago)
                RetrofitClient.apiService.registrarPagoMesa(mesaId, payload)
                cargarMesas()
                cargarEstadisticas()
            } catch (e: Exception) {
                _errorState.value = "Error cobrando la mesa."
            }
        }
    }

    fun modificarProductoEnBD(productoEditado: Producto) {
        viewModelScope.launch(Dispatchers.IO) { // INYECTADO: Ejecución en segundo plano
            try {
                RetrofitClient.apiService.actualizarProducto(productoEditado.id, productoEditado)
                cargarMenu()
            } catch (e: Exception) {
                _errorState.value = "Error editando producto."
            }
        }
    }

    fun descartarError() { _errorState.value = null }
}