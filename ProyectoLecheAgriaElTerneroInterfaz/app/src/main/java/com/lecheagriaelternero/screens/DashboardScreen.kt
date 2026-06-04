package com.lecheagriaelternero.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController, viewModel: com.lecheagriaelternero.viewmodel.MenuViewModel) {
    val stats by viewModel.estadisticas.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📊", fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Panel Administrativo", fontWeight = FontWeight.Black, fontSize = 20.sp, color = Color(0xFF1E1E1E))
                            Text("Leche Agria El Ternero", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Text("⬅️", fontSize = 20.sp)
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("inventario") }) {
                        Text("📦", fontSize = 24.sp) // Icono para ir a Inventario
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFDF8F8))
            )
        },
        containerColor = Color(0xFFFDF8F8)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF040505)), // Negro Principal
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Surface(color = Color(0xFFA0F399), shape = RoundedCornerShape(50)) {
                        Text(
                            text = "PERFORMANCE TODAY",
                            color = Color(0xFF217128),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Total de Ventas del Día", color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("C$ ${stats.totalVentas}", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.width(12.dp))
                        // Text("↗ +12%", color = Color(0xFFA0F399), fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
                    }
                }
            }


            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Tickets Emitidos
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1EDEC)),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("TICKETS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Text("${stats.ticketsEmitidos}", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color(0xFF1E1E1E), modifier = Modifier.padding(vertical = 4.dp))
                        Text("Prom: C$ ${"%.2f".format(stats.ticketPromedio)}", fontSize = 12.sp, color = Color.DarkGray)
                    }
                }

                val productoEstrella = stats.topProductos.firstOrNull()
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFC4C7C7)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("ESTRELLA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Text(productoEstrella?.nombre ?: "N/A", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFF1E1E1E), modifier = Modifier.padding(vertical = 4.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("${productoEstrella?.unidadesVendidas ?: 0} unidades", fontSize = 12.sp, color = Color(0xFF1B6D24), fontWeight = FontWeight.Bold)
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFC4C7C7)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⭐", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Top Productos", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E1E))
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    stats.topProductos.forEachIndexed { index, producto ->
                        ItemTopProducto(
                            posicion = (index + 1).toString(),
                            nombre = producto.nombre,
                            ventas = "${producto.unidadesVendidas} unidades",
                            precio = "C$ ${producto.totalGenerado}",
                            esPrimero = index == 0
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ItemTopProducto(posicion: String, nombre: String, ventas: String, precio: String, esPrimero: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .background(Color(0xFFF7F3F2), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = if (esPrimero) Color(0xFF1E1E1E) else Color.White,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Color(0xFFE5E2E1)),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(posicion, fontWeight = FontWeight.Black, color = if (esPrimero) Color.White else Color(0xFF1E1E1E))
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(nombre, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E1E1E), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(ventas, fontSize = 12.sp, color = Color.Gray)
        }
        Text(precio, fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color(0xFF1E1E1E))
    }
}