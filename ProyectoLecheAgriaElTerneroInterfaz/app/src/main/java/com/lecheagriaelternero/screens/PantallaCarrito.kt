package com.lecheagriaelternero.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
fun PantallaCarrito(navController: NavController, viewModel: MenuViewModel) {
    val productosEnCarrito by viewModel.carritoActual.collectAsStateWithLifecycle()
    val ordenPrevia by viewModel.ordenActivaMesa.collectAsStateWithLifecycle()
    var notas by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Confirmar / Editar Pedido", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.vaciarCarrito() }) {
                        Icon(Icons.Default.Delete, contentDescription = "Vaciar", tint = Color.Red)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(16.dp).fillMaxSize()) {

            LazyColumn(modifier = Modifier.weight(1f)) {
                // SECCIÓN 1: LO QUE YA ESTÁ PEDIDO (Si existe)
                ordenPrevia?.let { orden ->
                    item {
                        Surface(
                            color = Color(0xFFFFF9C4),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("PEDIDO ACTUAL EN MESA", fontWeight = FontWeight.Black, fontSize = 12.sp, color = Color(0xFF827717))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(orden.notas ?: "Sin detalles", fontSize = 13.sp, color = Color.DarkGray)
                                Text("Consumo acumulado: C$ ${orden.total}", fontWeight = FontWeight.Bold, color = Color(0xFF1B6D24), modifier = Modifier.padding(top = 8.dp))
                            }
                        }
                        Text("AÑADIR A LA ORDEN:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                if (productosEnCarrito.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) {
                            Text("No has añadido productos nuevos", color = Color.LightGray)
                        }
                    }
                } else {
                    items(productosEnCarrito) { producto ->
                        // ... (resto del diseño del item del carrito que ya teníamos)
                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = producto.nombre,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = "C$ ${producto.precio}",
                                        color = Color(0xFF1B6D24),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                                IconButton(onClick = { viewModel.eliminarDelCarrito(producto) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
                                }
                            }

                            if (producto.descripcion.isNotBlank()) {
                                Text(
                                    text = "Nota: ${producto.descripcion}",
                                    color = Color(0xFFD32F2F),
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                            HorizontalDivider(color = Color(0xFFEEEEEE), modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                }
            }

            OutlinedTextField(
                value = notas,
                onValueChange = { notas = it },
                label = { Text("Notas Generales para la Cocina") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Ej: Mesa completa, apurar bebidas...") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            val total = productosEnCarrito.sumOf { it.precio }

            Button(
                onClick = {
                    viewModel.enviarOrden(notas)
                    navController.popBackStack()
                },
                enabled = productosEnCarrito.isNotEmpty(),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B6D24))
            ) {
                Text("Enviar a Cocina (C$ $total)", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}