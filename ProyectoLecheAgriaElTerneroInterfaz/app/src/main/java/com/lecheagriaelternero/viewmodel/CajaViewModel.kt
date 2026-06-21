package com.lecheagriaelternero.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lecheagriaelternero.model.SesionCaja
import com.lecheagriaelternero.network.RetrofitClient
import kotlinx.coroutines.launch

class CajaViewModel : ViewModel() {
    // Variables de estado que la pantalla observará
    var isLoading by mutableStateOf(false)
    var mensajeExito by mutableStateOf("")
    var mensajeError by mutableStateOf("")

    fun abrirCaja(montoInicial: Double) {
        viewModelScope.launch {
            isLoading = true
            mensajeExito = ""
            mensajeError = ""

            try {
                // Creamos el objeto con el monto ingresado
                val sesion = SesionCaja(montoInicial = montoInicial)

                // Llamamos a la API que configuraste
                val response = RetrofitClient.apiService.abrirCaja(sesion)

                if (response.isSuccessful) {
                    mensajeExito = "¡Caja abierta exitosamente con C$ $montoInicial!"
                } else {
                    mensajeError = "Error: Es posible que la caja ya esté abierta."
                }
            } catch (e: Exception) {
                mensajeError = "Error de conexión con el servidor: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}