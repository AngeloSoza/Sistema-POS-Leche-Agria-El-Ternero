package com.lecheagriaelternero.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lecheagriaelternero.model.*
import com.lecheagriaelternero.network.RetrofitClient
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

    fun setMesaSeleccionada(id: String) {
        _mesaSeleccionadaId.value = id
        vaciarCarrito()
    }

    fun cargarMesas() {
        viewModelScope.launch {
            try { _mesas.value = RetrofitClient.apiService.getMesas() } catch (e: Exception) { }
        }
    }

    fun cargarMenu() {
        viewModelScope.launch {
            try { _menu.value = RetrofitClient.apiService.getMenu() } catch (e: Exception) { }
        }
    }

    fun cargarEstadisticas() {
        viewModelScope.launch {
            try { _estadisticas.value = RetrofitClient.apiService.getEstadisticasDelDia() } catch (e: Exception) { }
        }
    }

    fun cargarOrdenes() {
        viewModelScope.launch {
            try { _ordenesActivas.value = RetrofitClient.apiService.getOrdenes() } catch (e: Exception) { }
        }
    }

    fun agregarAlCarrito(producto: Producto) {
        val lista = _carritoActual.value.toMutableList()
        lista.add(producto)
        _carritoActual.value = lista
    }

    fun vaciarCarrito() {
        _carritoActual.value = mutableListOf()
    }

    fun enviarOrden(notas: String) {
        viewModelScope.launch {
            try {
                val mesaId = _mesaSeleccionadaId.value
                if (mesaId.isEmpty() || _carritoActual.value.isEmpty()) return@launch

                val totalCarrito = _carritoActual.value.sumOf { it.precio }

                val detalleItems = _carritoActual.value.joinToString("\n") { "- 1x ${it.nombre}" }
                val notaFinal = "$detalleItems\n\n⚠️ NOTAS: Persona ${_personaActual.value}: $notas"

                val payload = mapOf(
                    "notas" to notaFinal,
                    "total" to totalCarrito.toDouble()
                )
                RetrofitClient.apiService.enviarOrdenCocina(mesaId, payload)

                vaciarCarrito()
                cargarMesas()
                cargarOrdenes()
            } catch (e: Exception) {
                _errorState.value = "Error conectando con la cocina: ${e.message}"
            }
        }
    }

    fun cambiarEstadoOrden(ordenId: Long, nuevoEstado: String) {
        viewModelScope.launch {
            try {
                RetrofitClient.apiService.actualizarEstadoOrden(ordenId.toString(), mapOf("estado" to nuevoEstado))
                cargarOrdenes()
                cargarMesas()
            } catch (e: Exception) {
                _errorState.value = "Error actualizando orden: ${e.message}"
            }
        }
    }

    fun cobrarMesa(mesaId: String, metodoPago: String) {
        viewModelScope.launch {
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
        viewModelScope.launch {
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