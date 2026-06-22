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
import com.lecheagriaelternero.model.Producto
import com.lecheagriaelternero.viewmodel.MenuViewModel
import java.util.UUID

data class ItemOrdenParseado(
    val idUnico: String,
    val cantidad: Int,
    val nombre: String,
    val precioCalculado: Double,
    val bloqueTextoOriginal: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCarrito(navController: NavController, viewModel: MenuViewModel) {
    val productosEnCarrito by viewModel.carritoActual.collectAsStateWithLifecycle()
    val ordenPrevia by viewModel.ordenActivaMesa.collectAsStateWithLifecycle()
    val menuReal by viewModel.menu.collectAsStateWithLifecycle()

    var notas by remember { mutableStateOf("") }
    var mostrarEditorOrden by remember { mutableStateOf(false) }

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
                                        onClick = { mostrarEditorOrden = true },
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
                                    Text(producto.nombre, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                                    Text("C$ ${producto.precio}", color = Color(0xFF1B6D24), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                                IconButton(onClick = { viewModel.eliminarDelCarrito(producto) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
                                }
                            }

                            if (producto.descripcion.isNotBlank()) {
                                Text("Nota: ${producto.descripcion}", color = Color(0xFFD32F2F), fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
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
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B6D24))
            ) {
                Text("Enviar ${if (ordenPrevia != null) "Actualización" else "Nueva Orden"} a Cocina (+C$ $totalNuevos)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    val ordenActualSegura = ordenPrevia

    if (mostrarEditorOrden && ordenActualSegura != null) {
        var itemsParseados by remember { mutableStateOf<List<ItemOrdenParseado>>(emptyList()) }
        var notasGeneralesEdicion by remember { mutableStateOf("") }
        var totalCalculado by remember { mutableDoubleStateOf(0.0) }

        LaunchedEffect(mostrarEditorOrden) {
            val rawNotas = ordenActualSegura.notas ?: ""
            var itemsPart = rawNotas
            var genPart = ""

            if (rawNotas.contains("📝 NOTAS GENERALES:")) {
                val parts = rawNotas.split("📝 NOTAS GENERALES:")
                itemsPart = parts[0].trim()
                genPart = parts.getOrNull(1)?.trim() ?: ""
            }

            val lines = itemsPart.split("\n")
            val list = mutableListOf<ItemOrdenParseado>()
            var currentBlock = mutableListOf<String>()

            // Analizador Léxico Inmune a fallos
            for (line in lines) {
                if (line.matches(Regex(".*?\\d+x\\s+.*"))) {
                    if (currentBlock.isNotEmpty()) {
                        val parsed = parsearBloque(currentBlock, menuReal)
                        if (parsed != null) list.add(parsed)
                    }
                    currentBlock = mutableListOf(line)
                } else if (line.isNotBlank()) {
                    currentBlock.add(line)
                }
            }
            if (currentBlock.isNotEmpty()) {
                val parsed = parsearBloque(currentBlock, menuReal)
                if (parsed != null) list.add(parsed)
            }

            itemsParseados = list
            notasGeneralesEdicion = genPart
            totalCalculado = ordenActualSegura.total
        }

        AlertDialog(
            onDismissRequest = { mostrarEditorOrden = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier.fillMaxWidth(0.95f),
            title = { Text("Gestión de Cuenta y Pedidos", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Los productos eliminados se restarán automáticamente de la cuenta.", fontSize = 12.sp, color = Color.Gray)

                    Box(modifier = Modifier.weight(1f, fill = false).heightIn(max = 250.dp)) {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(itemsParseados) { item ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            // 🛡️ SOLUCIÓN: Solo mostramos nombre y precio
                                            Text("${item.cantidad}x ${item.nombre}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text("C$ ${item.precioCalculado}", color = Color(0xFF1B6D24), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                        }
                                        IconButton(
                                            onClick = {
                                                itemsParseados = itemsParseados.filter { it.idUnico != item.idUnico }
                                                totalCalculado = (totalCalculado - item.precioCalculado).coerceAtLeast(0.0)
                                            }
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider()

                    OutlinedTextField(
                        value = notasGeneralesEdicion,
                        onValueChange = { notasGeneralesEdicion = it },
                        label = { Text("Notas Generales") },
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                    )

                    // 🛡️ SOLUCIÓN: Texto fijo e inmutable
                    Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("TOTAL FINAL DE LA MESA:", fontWeight = FontWeight.Bold)
                            Text("C$ $totalCalculado", fontWeight = FontWeight.Black, color = Color(0xFF1B6D24), fontSize = 18.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val nuevoTextoNotas = buildString {
                            val itemsText = itemsParseados.joinToString("\n") { it.bloqueTextoOriginal }
                            append(itemsText)
                            if (notasGeneralesEdicion.isNotBlank()) {
                                append("\n\n📝 NOTAS GENERALES: $notasGeneralesEdicion")
                            }
                        }
                        viewModel.actualizarOrdenManual(ordenActualSegura.id, nuevoTextoNotas.trim(), totalCalculado)
                        mostrarEditorOrden = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B6D24))
                ) {
                    Text("Guardar Cambios")
                }
            },
            dismissButton = { TextButton(onClick = { mostrarEditorOrden = false }) { Text("Cancelar") } }
        )
    }
}

// Analizador Lexico Inmune a fallos de formato
fun parsearBloque(lines: List<String>, menuReal: List<Producto>): ItemOrdenParseado? {
    val firstLine = lines.first()
    val regex = Regex(".*?(\\d+)x\\s+(.*)")
    val match = regex.find(firstLine)

    if (match != null) {
        val cant = match.groupValues[1].toIntOrNull() ?: 1
        val nombre = match.groupValues[2].trim()

        val prod = menuReal.find { it.nombre.equals(nombre, ignoreCase = true) }
        val precioItem = if (prod != null) prod.precio * cant else 0.0

        return ItemOrdenParseado(
            idUnico = UUID.randomUUID().toString(),
            cantidad = cant,
            nombre = nombre,
            precioCalculado = precioItem,
            bloqueTextoOriginal = lines.joinToString("\n")
        )
    }
    return null
}