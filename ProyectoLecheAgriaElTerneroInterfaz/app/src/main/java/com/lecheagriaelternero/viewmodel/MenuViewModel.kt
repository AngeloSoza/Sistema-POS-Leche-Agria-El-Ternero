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
            viewModelScope.launch(Dispatchers.IO) {
                cargarOrdenes()
                val ordenEncontrada = _ordenesActivas.value.find {
                    it.mesa?.id?.toString() == id && it.estado != "PAGADO"
                }
                _ordenActivaMesa.value = ordenEncontrada
            }
        }
    }

    fun cargarMesas() {
        viewModelScope.launch(Dispatchers.IO) {
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
        viewModelScope.launch(Dispatchers.IO) {
            try { _menu.value = RetrofitClient.apiService.getMenu() } catch (e: Exception) { }
        }
    }

    fun cargarEstadisticas() {
        viewModelScope.launch(Dispatchers.IO) {
            try { _estadisticas.value = RetrofitClient.apiService.getEstadisticasDelDia() } catch (e: Exception) { }
        }
    }

    fun cargarOrdenes() {
        viewModelScope.launch(Dispatchers.IO) {
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
                val itemsLimpios = _carritoActual.value.map {
                    it.copy(
                        nombre = it.nombre.replace(" (Config)", "").trim(),
                        descripcion = it.descripcion.trim()
                    )
                }

                val itemsAgrupados = itemsLimpios.groupBy { "${it.nombre.lowercase()}|${it.descripcion.lowercase()}" }

                // 1. Los items nuevos del carrito se marcan como NUEVOS
                val detalleNuevos = itemsAgrupados.entries.joinToString("\n") { (key, lista) ->
                    val cantidad = lista.size
                    val originalItem = lista.first()
                    val nombre = originalItem.nombre
                    val desc = originalItem.descripcion

                    if (desc.isNotBlank() && desc != "null") {
                        "🔴 NUEVO: ${cantidad}x $nombre\n   $desc"
                    } else {
                        "🔴 NUEVO: ${cantidad}x $nombre"
                    }
                }

                // 2. Extraemos el pasado
                val ordenPrevia = _ordenActivaMesa.value
                var notasBase = ordenPrevia?.notas ?: ""
                var notasGenViejas = ""

                if (notasBase.contains("📝 NOTAS GENERALES:")) {
                    val partes = notasBase.split("📝 NOTAS GENERALES:")
                    notasBase = partes[0].trim()
                    notasGenViejas = partes.getOrNull(1)?.trim() ?: ""
                }

                // 🛡️ LÓGICA DE RÁFAGAS: Todo el pasado se convierte en "YA PEDIDO"
                notasBase = notasBase.replace("🔴 NUEVO:", "✅ YA PEDIDO:")
                    .replace(Regex("^- ", RegexOption.MULTILINE), "✅ YA PEDIDO: ")

                // 3. Concatenamos respetando la historia
                val notaFinal = buildString {
                    if (notasBase.isNotBlank()) {
                        append(notasBase)
                        append("\n")
                    }
                    append(detalleNuevos)

                    val notasGenCombinadas = listOf(notasGenViejas, notas).filter { it.isNotBlank() }.joinToString(" | ")
                    if (notasGenCombinadas.isNotBlank()) {
                        append("\n\n📝 NOTAS GENERALES: $notasGenCombinadas")
                    }
                }

                val payload = OrdenPayload(
                    notas = notaFinal.trim(),
                    total = totalCarrito,
                    detalles = emptyList() // 🛡️ Vaciado para evitar crasheos de integridad referencial en Spring Boot
                )

                RetrofitClient.apiService.enviarPedido(mesaId, payload)
                vaciarCarrito()

                // 4. Refresco ultra-rápido para la pantalla local
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
        viewModelScope.launch(Dispatchers.IO) {
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
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val payload = mapOf("notas" to notas, "total" to total)
                RetrofitClient.apiService.editarManual(ordenId.toString(), payload)

                // Refresco instantáneo tras edición manual
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
        viewModelScope.launch(Dispatchers.IO) {
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
        viewModelScope.launch(Dispatchers.IO) {
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