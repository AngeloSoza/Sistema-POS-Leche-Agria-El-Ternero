package com.lecheagriaelternero.network

import com.lecheagriaelternero.model.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface ApiService {
    @GET("api/mesas")
    suspend fun getMesas(): List<Mesa>

    @GET("api/menu")
    suspend fun getMenu(): List<Producto>

    @GET("api/ordenes")
    suspend fun getOrdenes(): List<OrdenBackend>

    @PATCH("api/ordenes/{id}/estado")
    suspend fun actualizarEstadoOrden(
        @Path("id") id: String,
        @Body estado: Map<String, String>
    )

    @POST("api/ordenes/{mesaId}") // Ruta corregida
    suspend fun enviarPedido(
        @Path("mesaId") mesaId: String,
        @Body payload: OrdenPayload // Tipo de dato corregido al nuevo DTO
    )

    @POST("api/ordenes/editar-pedido/{id}") // El endpoint que faltaba en tu API
    suspend fun editarManual(
        @Path("id") id: String,
        @Body payload: @JvmSuppressWildcards Map<String, Any>
    )

    @PUT("api/menu/{id}")
    suspend fun actualizarProducto(
        @Path("id") id: String,
        @Body producto: Producto
    )

    @POST("api/caja/pagar/{mesaId}")
    suspend fun registrarPagoMesa(
        @Path("mesaId") mesaId: String,
        @Body metodoPago: Map<String, String>
    )

    @GET("api/estadisticas/hoy")
    suspend fun getEstadisticasDelDia(): EstadisticasDia

    @POST("api/caja/abrir")
    suspend fun abrirCaja(@Body sesionCaja: SesionCaja): retrofit2.Response<SesionCaja>

    @POST("api/caja/pagar")
    suspend fun registrarPago(@Body pago: Pago): retrofit2.Response<Pago>
}

object RetrofitClient {
    // CAMBIA ESTA IP SI TU PC CAMBIÓ DE RED
    private const val BASE_URL = "http://192.168.0.8:8080/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
