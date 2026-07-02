package com.lecheagriaelternero.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lecheagriaelternero.model.*
import com.lecheagriaelternero.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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

    private val _estaCargando = MutableStateFlow(true)
    val estaCargando: StateFlow<Boolean> = _estaCargando

    private val _ordenActivaMesa = MutableStateFlow<OrdenBackend?>(null)
    val ordenActivaMesa: StateFlow<OrdenBackend?> = _ordenActivaMesa

    // ==========================================
    // MÓDULO ADMINISTRATIVO (FINANZAS)
    // ==========================================
    private val _fondoInicial = MutableStateFlow(0.0)
    val fondoInicial: StateFlow<Double> = _fondoInicial

    private val _ventasPedidosYa = MutableStateFlow(0.0)
    val ventasPedidosYa: StateFlow<Double> = _ventasPedidosYa

    private val _gastosDelDia = MutableStateFlow<List<Pair<String, Double>>>(emptyList())
    val gastosDelDia: StateFlow<List<Pair<String, Double>>> = _gastosDelDia

    fun setFondoInicial(monto: Double) { _fondoInicial.value = monto }
    fun setVentasPedidosYa(monto: Double) { _ventasPedidosYa.value = monto }

    fun agregarGasto(descripcion: String, monto: Double) {
        val nuevaLista = _gastosDelDia.value.toMutableList()
        nuevaLista.add(Pair(descripcion, monto))
        _gastosDelDia.value = nuevaLista
    }

    fun eliminarGasto(index: Int) {
        val nuevaLista = _gastosDelDia.value.toMutableList()
        if (index in nuevaLista.indices) {
            nuevaLista.removeAt(index)
            _gastosDelDia.value = nuevaLista
        }
    }

    init {
        inicializarBaseDeDatosCompleta()
    }

    fun inicializarBaseDeDatosCompleta() {
        viewModelScope.launch(Dispatchers.IO) {
            _estaCargando.value = true

            // 🛡️ BLINDAJE ANTI-HTTP 404: Descargamos cada módulo de forma independiente.
            // Si algo falla, el sistema sigue de pie y carga el resto.

            // 1. Cargar Mesas
            try {
                val lista = RetrofitClient.apiService.getMesas()
                _mesas.value = lista.sortedBy { "${it.numero}".replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0 }
            } catch (e: Exception) {
                // Error silencioso en consola, no rompemos la app
            }


            // 2. Cargar Menú
            try {
                val menuCrudo = RetrofitClient.apiService.getMenu()
                _menu.value = menuCrudo.sortedBy { it.id }
            } catch (e: Exception) { }

            // 3. Cargar Órdenes Activas
            try {
                _ordenesActivas.value = RetrofitClient.apiService.getOrdenes()
            } catch (e: Exception) { }

            // 4. Cargar Estadísticas (El causante original del 404)
            try {
                _estadisticas.value = RetrofitClient.apiService.getEstadisticasDelDia()
            } catch (e: Exception) {
                // Si el backend no tiene esta ruta aún, usamos datos vacíos sin lanzar alertas
                _estadisticas.value = EstadisticasDia()
            }

            _estaCargando.value = false
        }

    }

    fun setMesaSeleccionada(id: String) {
        _mesaSeleccionadaId.value = id
        vaciarCarrito()
        viewModelScope.launch(Dispatchers.IO) {
            cargarOrdenes()
            // 🛡️ SOLUCIÓN: Interpolación de strings para evitar "Redundant call of conversion"
            val ordenEncontrada = _ordenesActivas.value.find {
                "${it.mesa?.id}" == id && it.estado != "PAGADO"
            }
            _ordenActivaMesa.value = ordenEncontrada
        }
    }

    fun cargarMesas() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val lista = RetrofitClient.apiService.getMesas()
                _mesas.value = lista.sortedBy { "${it.numero}".replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0 }
            } catch (_: Exception) { }
        }
    }

    fun cargarMenu() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val menuCrudo = RetrofitClient.apiService.getMenu()
                _menu.value = menuCrudo.sortedBy { it.id }
            } catch (_: Exception) { }
        }
    }

    fun cargarEstadisticas() {
        viewModelScope.launch(Dispatchers.IO) {
            try { _estadisticas.value = RetrofitClient.apiService.getEstadisticasDelDia() } catch (_: Exception) { }
        }
    }

    fun cargarEstadisticasPorFecha(fecha: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _estadisticas.value = RetrofitClient.apiService.getEstadisticasPorFecha(fecha)
            } catch (_: Exception) {
                _estadisticas.value = EstadisticasDia()
            }
        }
    }

    fun cargarOrdenes() {
        viewModelScope.launch(Dispatchers.IO) {
            try { _ordenesActivas.value = RetrofitClient.apiService.getOrdenes() } catch (_: Exception) { }
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
                val detalleNuevos = itemsAgrupados.entries.joinToString("\n") { (_, lista) ->
                    val cantidad = lista.size
                    val originalItem = lista.first()
                    if (originalItem.descripcion.isNotBlank() && originalItem.descripcion != "null") {
                        "⭐ [NUEVO] ${cantidad}x ${originalItem.nombre}\n   ${originalItem.descripcion}"
                    } else {
                        "⭐ [NUEVO] ${cantidad}x ${originalItem.nombre}"
                    }
                }

                val notaFinal = buildString {
                    append(detalleNuevos)
                    if (notas.isNotBlank()) {
                        append("\n\n📝 NOTAS GENERALES: $notas")
                    }
                }

                val payload = OrdenPayload(notas = notaFinal.trim(), total = totalCarrito, detalles = emptyList())
                RetrofitClient.apiService.enviarPedido(mesaId, payload)
                vaciarCarrito()

                // 🛡️ REFRESCO GARANTIZADO
                cargarOrdenes()
                cargarMesas()

                val ordenesActualizadas = RetrofitClient.apiService.getOrdenes()
                _ordenesActivas.value = ordenesActualizadas
                _ordenActivaMesa.value = ordenesActualizadas.find { "${it.mesa?.id}" == mesaId && it.estado != "PAGADO" }

            } catch (e: Exception) {
                _errorState.value = "🔴 ERROR COCINA (TIMEOUT): Verifica que la PC y el celular estén en el mismo Wi-Fi (IP: 192.168.0.18)"
            }
        }
    }

    fun cambiarEstadoOrden(ordenId: Long, nuevoEstado: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                RetrofitClient.apiService.actualizarEstadoOrden("$ordenId", mapOf("estado" to nuevoEstado))
                delay(500)
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
                RetrofitClient.apiService.editarManual("$ordenId", payload)
                val ordenesActualizadas = RetrofitClient.apiService.getOrdenes()
                _ordenesActivas.value = ordenesActualizadas
                val mesaIdLocal = _mesaSeleccionadaId.value
                _ordenActivaMesa.value = ordenesActualizadas.find { "${it.mesa?.id}" == mesaIdLocal && it.estado != "PAGADO" }
                cargarMesas()
            } catch (e: Exception) {
                _errorState.value = "Error editando orden: ${e.message}"
            }
        }
    }

    fun cobrarMesa(ordenId: Long, metodoPago: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                RetrofitClient.apiService.actualizarEstadoOrden(
                    "$ordenId", 
                    mapOf("estado" to "PAGADO", "metodoPago" to metodoPago)
                )
                // 🛡️ REFRESCO TOTAL POST-COBRO
                delay(500)
                cargarOrdenes()
                cargarMesas()
                cargarEstadisticas()
            } catch (e: Exception) {
                _errorState.value = "🔴 ERROR CAJA: No se pudo registrar el pago. ${e.message}"
            }
        }
    }

    // ==========================================
    // CRUD DE MENÚ OPTIMIZADO
    // ==========================================
    fun modificarProductoEnBD(productoEditado: Producto) {
        val listaActual = _menu.value.toMutableList()
        val index = listaActual.indexOfFirst { it.id == productoEditado.id }
        if (index != -1) {
            listaActual[index] = productoEditado
            _menu.value = listaActual
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                RetrofitClient.apiService.actualizarProducto("${productoEditado.id}", productoEditado)
            } catch (e: Exception) {
                cargarMenu()
                _errorState.value = "Error editando producto: ${e.message}"
            }
        }
    }

    fun crearProducto(nuevoProducto: Producto) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 🛡️ SOLUCIÓN: Para productos nuevos, el ID debe ser nulo para que la DB lo genere (SERIAL)
                // Si mandamos "" o "0", el backend podría intentar insertarlo literalmente y fallar.
                val productoParaEnviar = nuevoProducto.copy(id = "")
                RetrofitClient.apiService.crearProducto(productoParaEnviar)
                cargarMenu()
            } catch (e: Exception) {
                _errorState.value = "Error creando producto: ${e.message}"
            }
        }
    }

    fun eliminarProducto(id: String) {
        _menu.value = _menu.value.filter { "${it.id}" != id }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                RetrofitClient.apiService.eliminarProducto(id)
            } catch (e: Exception) {
                cargarMenu()
                _errorState.value = "Error eliminando producto: ${e.message}"
            }
        }
    }

    fun descartarError() { _errorState.value = null }
}