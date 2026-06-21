package com.lecheagriaelternero.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
    val ordenes by viewModel.ordenesActivas.collectAsStateWithLifecycle()

    val ordenesCocina = ordenes.filter { it.estado == "PENDIENTE" || it.estado == "LISTO" }
        .sortedByDescending { it.id } // Mostrar las más nuevas arriba

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
                title = { Text("Monitor de Cocina (KDS)", fontWeight = FontWeight.Black) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E1E), titleContentColor = Color.White)
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth().background(Color(0xFF2D2D2D)).padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Órdenes Recibidas", color = Color.White, fontWeight = FontWeight.Bold)
                Surface(color = Color(0xFF444444), shape = RoundedCornerShape(12.dp)) {
                    Text("${ordenesCocina.size} TICKETS", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), color = Color.White, fontSize = 10.sp)
                }
            }

            if (ordenesCocina.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay órdenes pendientes en este momento.", color = Color.Gray)
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
                            colorFondo = if (esListo) Color(0xFFE8F5E9) else Color.White,
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorFondo),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Mesa ${orden.mesa?.numero ?: "N/A"}", fontSize = 24.sp, fontWeight = FontWeight.Black)
                if (orden.estado == "LISTO") {
                    Surface(color = Color(0xFF1B6D24), shape = RoundedCornerShape(8.dp)) {
                        Text("LISTA", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Text("Ticket #${orden.id}", fontSize = 12.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(16.dp))

            Text(orden.notas ?: "Sin detalles", fontSize = 18.sp, fontWeight = FontWeight.Medium, lineHeight = 24.sp)

            Spacer(modifier = Modifier.height(20.dp))

            if (orden.estado == "PENDIENTE") {
                Button(
                    onClick = onAction,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E1E)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("ORDEN LISTA ✅", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
