package com.lecheagriaelternero.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
    var notas by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Confirmar Pedido", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        // Ícono de flecha actualizado para evitar la advertencia
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(16.dp).fillMaxSize()) {

            if (productosEnCarrito.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("El carrito está vacío", color = Color.Gray)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(productosEnCarrito) { producto ->
                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = producto.nombre,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 16.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "C$ ${producto.precio}",
                                    color = Color(0xFF1B6D24),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
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