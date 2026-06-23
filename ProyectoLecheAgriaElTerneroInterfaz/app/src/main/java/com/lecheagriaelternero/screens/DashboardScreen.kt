package com.lecheagriaelternero.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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

    // Caché de colores para evitar re-asignaciones en memoria durante recomposiciones
    val bgApp = remember { Color(0xFFF9FAFB) }
    val darkCard = remember { Color(0xFF1E2022) } // Carbón plano sólido (más ligero que un degradado)
    val textPrimary = remember { Color(0xFF111827) }
    val textSecondary = remember { Color(0xFF6B7280) }
    val oliveText = remember { Color(0xFF374151) }

    Scaffold(
        containerColor = bgApp,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Analytics,
                            contentDescription = null,
                            tint = textPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Panel Administrativo", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textPrimary)
                            Text("Leche Agria El Ternero", fontSize = 12.sp, color = textSecondary)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Regresar", tint = textPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("inventario") }) {
                        Icon(Icons.Rounded.Inventory2, contentDescription = "Inventario", tint = textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgApp)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(modifier = Modifier.height(2.dp))

            // Tarjeta Principal de Ventas (Sólida, sin sombras pesadas)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = darkCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Box(
                        modifier = Modifier
                            .background(Color(0.2f, 0.2f, 0.2f, 0.4f), RoundedCornerShape(50))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "RENDIMIENTO HOY",
                            color = Color(0xFF9CA3AF),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Total de Ventas del Día", color = Color(0xFFD1D5DB), fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "C$ ${stats.totalVentas}",
                        color = Color.White,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Bloque de Métricas Secundarias
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Tickets
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("TICKETS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = textSecondary)
                        Text("${stats.ticketsEmitidos}", fontSize = 24.sp, fontWeight = FontWeight.Black, color = textPrimary, modifier = Modifier.padding(vertical = 2.dp))
                        Text("Prom: C$ ${"%.2f".format(stats.ticketPromedio)}", fontSize = 12.sp, color = oliveText)
                    }
                }

                // Producto Estrella
                val productoEstrella = stats.topProductos.firstOrNull()
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("ESTRELLA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = textSecondary)
                        Text(
                            text = productoEstrella?.nombre ?: "N/A",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary,
                            modifier = Modifier.padding(vertical = 2.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text("${productoEstrella?.unidadesVendidas ?: 0} uds", fontSize = 12.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Contenedor Lista Top Productos
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Star, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Top Productos", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    stats.topProductos.forEachIndexed { index, producto ->
                        ItemTopProducto(
                            posicion = (index + 1).toString(),
                            nombre = producto.nombre,
                            ventas = "${producto.unidadesVendidas} unidades",
                            precio = "C$ ${producto.totalGenerado}",
                            esPrimero = index == 0
                        )
                        if (index < stats.topProductos.lastIndex) {
                            HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ItemTopProducto(posicion: String, nombre: String, ventas: String, precio: String, esPrimero: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Indicador numérico simplificado sin anidación excesiva
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    color = if (esPrimero) Color(0xFF1E2022) else Color(0xFFF3F4F6),
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = posicion,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = if (esPrimero) Color.White else Color(0xFF4B5563)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(nombre, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF111827), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(ventas, fontSize = 12.sp, color = Color(0xFF6B7280))
        }
        Text(precio, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF111827))
    }
}