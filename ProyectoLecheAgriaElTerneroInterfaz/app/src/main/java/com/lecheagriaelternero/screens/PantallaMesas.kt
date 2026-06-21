package com.lecheagriaelternero.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
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
import com.lecheagriaelternero.viewmodel.MenuViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaMesas(navController: NavController, viewModel: MenuViewModel) {
    val mesasReales by viewModel.mesas.collectAsStateWithLifecycle()
    val errorMsg by viewModel.errorState.collectAsStateWithLifecycle()

    errorMsg?.let { mensaje ->
        AlertDialog(
            onDismissRequest = { viewModel.descartarError() },
            confirmButton = { TextButton(onClick = { viewModel.descartarError() }) { Text("Aceptar") } },
            title = { Text("⚠️ Aviso") },
            text = { Text(mensaje) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mapa de Mesas", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF5F5F5))
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.cargarMesas() },
                containerColor = Color(0xFF1E1E1E),
                contentColor = Color.White
            ) { Text("↻") }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            if (mesasReales.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay mesas en la base de datos.", color = Color.Gray)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 150.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(mesasReales) { mesa ->
                        val esLibre = mesa.estado.uppercase() == "LIBRE" || mesa.estado.uppercase() == "DISPONIBLE"
                        val colorFondo = if (esLibre) Color(0xFF1B6D24) else Color(0xFFD32F2F)
                        val textoEstado = if (esLibre) "DISPONIBLE" else "OCUPADA"

                        Card(
                            onClick = {
                                if (mesa.id != viewModel.mesaSeleccionadaId.value) {
                                    viewModel.vaciarCarrito() // Solo vaciar si cambiamos de mesa físicamente
                                }
                                viewModel.setMesaSeleccionada(mesa.id)
                                navController.navigate("toma_orden")
                            },
                            modifier = Modifier.height(130.dp), // Altura fija para uniformidad total
                            colors = CardDefaults.cardColors(containerColor = colorFondo),
                            border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center // Centra todo el contenido verticalmente
                            ) {
                                Text(
                                    text = "Mesa ${mesa.numero}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = textoEstado,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 14.sp
                                )
                                
                                // El consumo se muestra o se reserva el espacio para mantener el tamaño
                                if (!esLibre && mesa.total > 0) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        color = Color.Black.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "CONSUMO: C$ ${mesa.total}",
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFFFFEB3B),
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                } else {
                                    // Espaciador invisible para mantener la misma altura que las ocupadas
                                    Spacer(modifier = Modifier.height(32.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}