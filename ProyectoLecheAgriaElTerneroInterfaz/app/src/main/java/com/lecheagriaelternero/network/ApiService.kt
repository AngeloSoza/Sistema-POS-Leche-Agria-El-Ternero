package com.lecheagriaelternero.network

import com.lecheagriaelternero.model.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface ApiService {
    @GET("api/mesas")
    suspend fun getMesas(): List<Mesa>

    @GET("api/menu")
    suspend fun getMenu(): List<Producto>

    @GET("api/ordenes")
    suspend fun getOrdenes(): List<OrdenBackend>

    @POST("api/ordenes/{mesaId}")
    suspend fun enviarOrdenCocina(
        @Path("mesaId") mesaId: String,
        @Body orden: @JvmSuppressWildcards Map<String, Any>
    )

    @PATCH("api/ordenes/{id}/estado")
    suspend fun actualizarEstadoOrden(
        @Path("id") id: String,
        @Body estado: Map<String, String>
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
}

object RetrofitClient {

    private const val BASE_URL = "http://10.209.82.72:8080/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
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