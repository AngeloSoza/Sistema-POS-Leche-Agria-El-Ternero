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
    private const val BASE_URL = "http://192.168.0.18:8080"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}