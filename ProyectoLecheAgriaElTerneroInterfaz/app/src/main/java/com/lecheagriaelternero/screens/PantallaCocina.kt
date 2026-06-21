package com.lecheagriaelternero.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.lecheagriaelternero.model.OrdenBackend
import com.lecheagriaelternero.viewmodel.MenuViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCocina(navController: NavController, viewModel: MenuViewModel) {
    // FORZAR RECARGA AL ENTRAR
    LaunchedEffect(Unit) {
        viewModel.cargarOrdenes()
    }

    val ordenes by viewModel.ordenesActivas.collectAsStateWithLifecycle()

    val ordenesCocina = ordenes.filter { it.estado == "PENDIENTE" || it.estado == "LISTO" }
        .sortedWith(compareBy({ it.estado != "PENDIENTE" }, { -it.id }))

    // SISTEMA DE REFRESCADO AUTOMÁTICO CADA 5 SEGUNDOS
    LaunchedEffect(Unit) {
        while(true) {
            viewModel.cargarOrdenes()
            kotlinx.coroutines.delay(5000)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Monitor de Cocina (KDS)", fontWeight = FontWeight.ExtraBold)
                        Text("Sincronizado en tiempo real", fontSize = 12.sp, color = Color.Gray)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E1E), titleContentColor = Color.White),
                actions = {
                    IconButton(onClick = { viewModel.cargarOrdenes() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refrescar", tint = Color.White)
                    }
                }
            )
        },
        containerColor = Color(0xFF121212) // Fondo oscuro profesional
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            val pendientes = ordenesCocina.count { it.estado == "PENDIENTE" }
            
            Row(
                modifier = Modifier.fillMaxWidth().background(Color(0xFF2D2D2D)).padding(16.dp), 
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("ÓRDENES ACTIVAS", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                Surface(
                    color = if (pendientes > 0) Color(0xFFD32F2F) else Color(0xFF1B6D24), 
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "$pendientes POR PREPARAR", 
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), 
                        color = Color.White, 
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (ordenesCocina.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No hay órdenes pendientes", color = Color.Gray, fontWeight = FontWeight.Medium)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(ordenesCocina) { orden ->
                        val esListo = orden.estado == "LISTO"
                        KitchenTicketCard(
                            orden = orden, 
                            colorFondo = if (esListo) Color(0xFF1B5E20) else Color(0xFFFFFFFF),
                            onAction = {
                                if (!esListo) {
                                    viewModel.cambiarEstadoOrden(orden.id, "LISTO")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun KitchenTicketCard(orden: OrdenBackend, colorFondo: Color = Color.White, onAction: () -> Unit) {
    val esListo = orden.estado == "LISTO"
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colorFondo),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(), 
                horizontalArrangement = Arrangement.SpaceBetween, 
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        "MESA ${orden.mesa?.numero ?: "N/A"}", 
                        fontSize = 28.sp, 
                        fontWeight = FontWeight.Black,
                        color = if (esListo) Color.White else Color.Black
                    )
                    Text(
                        "Ticket #${orden.id}", 
                        fontSize = 14.sp, 
                        color = if (esListo) Color.White.copy(alpha = 0.7f) else Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                if (esListo) {
                    Surface(color = Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(12.dp)) {
                        Text(
                            "ENTREGADO", 
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), 
                            color = Color.White, 
                            fontSize = 14.sp, 
                            fontWeight = FontWeight.Black
                        )
                    }
                } else {
                    val esActualizacion = orden.notas?.contains("--- ACTUALIZACIÓN ---") == true
                    Surface(
                        color = if (esActualizacion) Color(0xFFD32F2F) else Color(0xFFFFF3E0), 
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            if (esActualizacion) "ACTUALIZADA" else "NUEVO", 
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), 
                            color = if (esActualizacion) Color.White else Color(0xFFE65100),
                            fontSize = 14.sp, 
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = if (esListo) Color.White.copy(alpha = 0.2f) else Color.LightGray)
            Spacer(modifier = Modifier.height(16.dp))

            // Desglose de productos con mejor legibilidad
            val lineas = orden.notas?.split("\n") ?: emptyList()
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                lineas.forEach { linea ->
                    if (linea.isNotBlank()) {
                        Row(verticalAlignment = Alignment.Top) {
                            if (linea.startsWith("-")) {
                                Icon(
                                    Icons.Default.CheckCircle, 
                                    contentDescription = null, 
                                    modifier = Modifier.size(18.dp).padding(top = 2.dp),
                                    tint = if (esListo) Color.White else Color(0xFF1B6D24)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                            }
                            Text(
                                linea, 
                                fontSize = 18.sp, 
                                fontWeight = if (linea.startsWith("-")) FontWeight.Bold else FontWeight.Normal,
                                color = if (esListo) Color.White else Color.Black,
                                lineHeight = 24.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (!esListo) {
                Button(
                    onClick = onAction,
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E1E)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("ORDEN LISTA PARA ENTREGAR ✅", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                }
            }
        }
    }
}
