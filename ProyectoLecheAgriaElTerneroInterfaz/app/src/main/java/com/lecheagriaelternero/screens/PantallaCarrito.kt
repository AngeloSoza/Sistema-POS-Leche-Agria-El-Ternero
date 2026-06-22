package com.lecheagriaelternero.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.lecheagriaelternero.viewmodel.MenuViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCarrito(navController: NavController, viewModel: MenuViewModel) {
    val productosEnCarrito by viewModel.carritoActual.collectAsStateWithLifecycle()
    val ordenPrevia by viewModel.ordenActivaMesa.collectAsStateWithLifecycle()
    val menuReal by viewModel.menu.collectAsStateWithLifecycle()

    var notas by remember { mutableStateOf("") }

    var mostrarEditorOrden by remember { mutableStateOf(false) }
    var notasEdicion by remember { mutableStateOf("") }
    var totalEdicion by remember { mutableStateOf("") }

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
                // SECCIÓN 1: LO QUE YA ESTÁ PEDIDO EN LA BASE DE DATOS
                ordenPrevia?.let { orden ->
                    item {
                        Surface(
                            color = Color(0xFFFFF9C4),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("PEDIDO ACTUAL EN MESA", fontWeight = FontWeight.Black, fontSize = 12.sp, color = Color(0xFF827717))
                                    Button(
                                        onClick = {
                                            notasEdicion = orden.notas ?: ""
                                            totalEdicion = orden.total.toString()
                                            mostrarEditorOrden = true
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B6D24)),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Gestionar", fontSize = 12.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(orden.notas ?: "Sin detalles", fontSize = 13.sp, color = Color.DarkGray)
                                Text("Consumo acumulado: C$ ${orden.total}", fontWeight = FontWeight.Bold, color = Color(0xFF1B6D24), modifier = Modifier.padding(top = 8.dp))
                            }
                        }

                        if (productosEnCarrito.isNotEmpty()) {
                            Text("AÑADIR A LA ORDEN:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                // SECCIÓN 2: PRODUCTOS NUEVOS EN EL CARRITO LOCAL
                if (productosEnCarrito.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) {
                            Text("No has añadido productos nuevos", color = Color.LightGray)
                        }
                    }
                } else {
                    items(productosEnCarrito) { producto ->
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
                label = { Text("Notas Generales para la Cocina (Nuevas)") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Ej: Mesa completa, apurar bebidas...") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            val totalNuevos = productosEnCarrito.sumOf { it.precio }

            Button(
                onClick = {
                    viewModel.enviarOrden(notas)
                    navController.popBackStack()
                },
                enabled = productosEnCarrito.isNotEmpty(),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E1E))
            ) {
                Text("Enviar ${if (ordenPrevia != null) "Actualización" else "Nueva Orden"} a Cocina (+C$ $totalNuevos)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    // CORRECCIÓN DEL SMART CAST: Congelamos el valor en una variable inmutable
    val ordenActualSegura = ordenPrevia

    if (mostrarEditorOrden && ordenActualSegura != null) {
        val notasLimpias = remember(notasEdicion) {
            notasEdicion.replace(Regex("(?i)⚠️ NOTAS: Persona \\d+:"), "⚠️ NOTAS:")
                .replace(Regex("(?i)NOTAS: Persona \\d+:"), "")
                .trim()
        }

        val lineasEditables = remember(notasLimpias) {
            notasLimpias.split("\n").filter { it.isNotBlank() && !it.contains("---") }
        }
        var expandedExtraEdit by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { mostrarEditorOrden = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier.fillMaxWidth(0.95f),
            title = { Text("Editor de Orden Especializada") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Elimina items directamente o ajusta las notas y el total manualmente. Se guardará en la base de datos al instante.", fontSize = 12.sp, color = Color.Gray)

                    Text("Items Detectados (Toca para eliminar):", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    Box(modifier = Modifier.weight(1f, fill = false).heightIn(max = 200.dp)) {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(lineasEditables) { linea ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                                    onClick = {
                                        val nuevasLineas = lineasEditables.toMutableList().apply { remove(linea) }
                                        notasEdicion = nuevasLineas.joinToString("\n")
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(linea, fontSize = 12.sp, modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider()

                    Text("Añadir Extra a esta Orden:", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    @Suppress("DEPRECATION") // Silencia el aviso del menuAnchor
                    ExposedDropdownMenuBox(expanded = expandedExtraEdit, onExpandedChange = { expandedExtraEdit = !expandedExtraEdit }) {
                        OutlinedTextField(
                            value = "Toca para añadir un extra...",
                            onValueChange = {}, readOnly = true,
                            label = { Text("Añadir Extra (+ precio)") },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedExtraEdit) }
                        )
                        ExposedDropdownMenu(expanded = expandedExtraEdit, onDismissRequest = { expandedExtraEdit = false }) {
                            val ingredientesDisponiblesLocal = menuReal.filter { it.categoria != "Combos" }
                            ingredientesDisponiblesLocal.forEach { p ->
                                DropdownMenuItem(
                                    text = { Text("${p.nombre} (+C$ ${p.precio})") },
                                    onClick = {
                                        notasEdicion = if (notasEdicion.isBlank()) "- 1x ${p.nombre}" else "$notasEdicion\n- 1x ${p.nombre}"
                                        val currentTotal = totalEdicion.toDoubleOrNull() ?: 0.0
                                        totalEdicion = (currentTotal + p.precio).toString()
                                        expandedExtraEdit = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = notasEdicion,
                        onValueChange = { notasEdicion = it },
                        label = { Text("Notas y Detalle Manual") },
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                    )

                    OutlinedTextField(
                        value = totalEdicion,
                        onValueChange = { totalEdicion = it },
                        label = { Text("Total Final (C$)") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Text("C$", fontWeight = FontWeight.Bold) }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // USAMOS LA VARIABLE CONGELADA PARA QUE KOTLIN NO FALLE
                        val nuevoTotal = totalEdicion.toDoubleOrNull() ?: ordenActualSegura.total
                        viewModel.actualizarOrdenManual(ordenActualSegura.id, notasEdicion, nuevoTotal)
                        mostrarEditorOrden = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B6D24))
                ) {
                    Text("Guardar Cambios")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarEditorOrden = false }) { Text("Cancelar") }
            }
        )
    }
}