package com.lecheagriaelternero.network

import com.lecheagriaelternero.model.*
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface ApiService {

    @GET("/api/mesas")
    suspend fun getMesas(): List<Mesa>

    @GET("/api/estadisticas/hoy")
    suspend fun getEstadisticasDelDia(): EstadisticasDia

    @GET("/api/ordenes")
    suspend fun getOrdenes(): List<OrdenBackend>

    @POST("/api/ordenes/enviar-pedido/{mesaId}")
    suspend fun enviarPedido(@Path("mesaId") mesaId: String, @Body payload: OrdenPayload): OrdenBackend

    @PATCH("/api/ordenes/{id}/estado")
    suspend fun actualizarEstadoOrden(@Path("id") id: String, @Body estado: Map<String, String>): OrdenBackend

    @POST("/api/ordenes/editar-pedido/{id}")
    suspend fun editarManual(@Path("id") id: String, @Body payload: Map<String, @JvmSuppressWildcards Any>): OrdenBackend

    @GET("/api/menu")
    suspend fun getMenu(): List<Producto>

    @POST("/api/menu")
    suspend fun crearProducto(@Body producto: Producto): Producto

    @PUT("/api/menu/{id}")
    suspend fun actualizarProducto(@Path("id") id: String, @Body producto: Producto): Producto

    @DELETE("/api/menu/{id}")
    suspend fun eliminarProducto(@Path("id") id: String): Response<Unit>
}


object RetrofitClient {

    var currentIp: String = "192.168.0.18"
    
    private var _apiService: ApiService? = null

    val apiService: ApiService
        get() {
            if (_apiService == null) {
                _apiService = createRetrofit("http://$currentIp:8080")
            }
            return _apiService!!
        }

    fun updateIp(newIp: String) {
        currentIp = newIp.trim()
        _apiService = createRetrofit("http://$currentIp:8080")
    }

    private fun createRetrofit(baseUrl: String): ApiService {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}