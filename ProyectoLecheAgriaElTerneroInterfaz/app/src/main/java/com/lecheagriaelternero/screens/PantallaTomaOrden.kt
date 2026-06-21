package com.lecheagriaelternero.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.lecheagriaelternero.model.Producto
import com.lecheagriaelternero.viewmodel.MenuViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaTomaOrden(navController: NavController, viewModel: MenuViewModel) {
    val menuReal by viewModel.menu.collectAsStateWithLifecycle()
    val carritoActual by viewModel.carritoActual.collectAsStateWithLifecycle()
    val ordenes by viewModel.ordenesActivas.collectAsStateWithLifecycle()
    val mesaSeleccionadaId by viewModel.mesaSeleccionadaId.collectAsStateWithLifecycle()

    val ordenPrevia = ordenes.find { it.mesa?.id == mesaSeleccionadaId && it.estado != "PAGADO" }

    val categorias = listOf("Todos") + menuReal.map { it.categoria }.distinct()
    var categoriaSeleccionada by remember { mutableStateOf("Todos") }
    var productoACustomizar by remember { mutableStateOf<Producto?>(null) }
    var mostrarResumenPrevio by remember { mutableStateOf(false) }
    var mostrarEditorOrden by remember { mutableStateOf(false) }
    var notasEdicion by remember { mutableStateOf("") }
    var totalEdicion by remember { mutableStateOf("") }

    val productosMostrados = menuReal.filter { producto ->
        val coincideCategoria = categoriaSeleccionada == "Todos" || producto.categoria == categoriaSeleccionada
        coincideCategoria && producto.disponible
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Tomar Orden", fontSize = 18.sp)
                        if (ordenPrevia != null) {
                            Text(
                                "Mesa con pedido activo (C$ ${ordenPrevia.total})", 
                                fontSize = 12.sp, 
                                color = Color(0xFFD32F2F),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable { mostrarResumenPrevio = true }
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    BadgedBox(
                        badge = {
                            if (carritoActual.isNotEmpty()) {
                                Badge { Text(carritoActual.size.toString()) }
                            }
                        },
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        IconButton(onClick = { navController.navigate("carrito") }) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Ver Carrito")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Banner informativo si hay orden previa
            if (ordenPrevia != null) {
                Surface(
                    color = Color(0xFFFFF9C4),
                    modifier = Modifier.fillMaxWidth().clickable { mostrarResumenPrevio = true }
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "⚠️ Esta mesa ya tiene un pedido en curso. Toca para ver detalles.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            SecondaryScrollableTabRow(
                selectedTabIndex = categorias.indexOf(categoriaSeleccionada),
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                categorias.forEachIndexed { index, categoria ->
                    Tab(
                        selected = categorias.indexOf(categoriaSeleccionada) == index,
                        onClick = { categoriaSeleccionada = categoria },
                        text = { Text(categoria) }
                    )
                }
            }

            if (productosMostrados.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay productos disponibles.")
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(productosMostrados) { producto ->
                        ProductoItemCard(
                            producto = producto,
                            onAgregar = { productoACustomizar = producto }
                        )
                    }
                }
            }
        }
    }

    productoACustomizar?.let { producto ->
        DialogoPersonalizar(
            producto = producto,
            menuReal = menuReal,
            onDismiss = { productoACustomizar = null },
            onConfirm = { productoModificado, cant ->
                repeat(cant) {
                    viewModel.agregarAlCarrito(productoModificado)
                }
                productoACustomizar = null
            }
        )
    }

    if (mostrarResumenPrevio && ordenPrevia != null) {
        AlertDialog(
            onDismissRequest = { mostrarResumenPrevio = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color(0xFF1B6D24))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Resumen Mesa ${ordenPrevia.mesa?.numero ?: ""}")
                }
            },
            text = {
                Column {
                    Surface(
                        color = Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Consumo: C$ ${ordenPrevia.total}", 
                            fontWeight = FontWeight.Black, 
                            color = Color(0xFF1B6D24),
                            modifier = Modifier.padding(12.dp),
                            fontSize = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Detalle del pedido:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Parseo básico de líneas para mostrar mejor
                    val lineas = ordenPrevia.notas?.split("\n") ?: emptyList()
                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        items(lineas) { linea ->
                            if (linea.isNotBlank()) {
                                Text(
                                    linea, 
                                    fontSize = 13.sp, 
                                    color = if (linea.startsWith("-")) Color.DarkGray else Color.Gray,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { 
                        notasEdicion = ordenPrevia.notas ?: ""
                        totalEdicion = ordenPrevia.total.toString()
                        mostrarEditorOrden = true
                        mostrarResumenPrevio = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B6D24))
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gestionar Pedido")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarResumenPrevio = false }) { Text("Cerrar") }
            }
        )
    }

    if (mostrarEditorOrden && ordenPrevia != null) {
        val lineasEditables = remember(notasEdicion) { 
            notasEdicion.split("\n").filter { it.isNotBlank() && !it.contains("---") } 
        }

        AlertDialog(
            onDismissRequest = { mostrarEditorOrden = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier.fillMaxWidth(0.95f),
            title = { Text("Editor de Orden Especializada") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Elimina items directamente o ajusta las notas y el total manualmente.", fontSize = 12.sp, color = Color.Gray)
                    
                    Text("Items Detectados (Toca para eliminar):", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    
                    Box(modifier = Modifier.weight(1f, fill = false).heightIn(max = 250.dp)) {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(lineasEditables) { linea ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                                    onClick = {
                                        // Eliminar línea específica
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

                    OutlinedTextField(
                        value = notasEdicion,
                        onValueChange = { notasEdicion = it },
                        label = { Text("Notas y Detalle Manual") },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
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
                        val nuevoTotal = totalEdicion.toDoubleOrNull() ?: ordenPrevia.total
                        viewModel.actualizarOrden(ordenPrevia.id, notasEdicion, nuevoTotal)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoPersonalizar(
    producto: Producto,
    menuReal: List<Producto>,
    onDismiss: () -> Unit,
    onConfirm: (Producto, Int) -> Unit
) {
    var notasAdicionales by remember { mutableStateOf("") }
    var cantidad by remember { mutableIntStateOf(1) }
    
    // Identificar ingredientes del combo basados en la descripción
    val ingredientesDisponibles = menuReal.filter { it.categoria != "Combos" }
    
    // Lógica para detectar qué extras están mencionados en la descripción del combo
    val ingredientesCombo = remember(producto.descripcion, menuReal) {
        val descLow = producto.descripcion.lowercase()
            .replace("un ", "1 ")
            .replace("uno ", "1 ")
            .replace("dos ", "2 ")
            .replace("tres ", "3 ")
            .replace(".", "")
            .replace(",", "")
            .replace("(", "")
            .replace(")", "")
            .replace("  ", " ")

        val matches = ingredientesDisponibles.filter { item ->
            val itemNameLow = item.nombre.lowercase()
                .replace(".", "")
                .replace(",", "")
                .replace("(", "")
                .replace(")", "")
                .replace("  ", " ")
            
            descLow.contains(itemNameLow) || 
            (itemNameLow == "1 huevo entero" && descLow.contains("un huevo entero")) ||
            (itemNameLow == "2 huevos enteros" && descLow.contains("dos huevos enteros")) ||
            (itemNameLow == "café negro" && descLow.contains("café"))
        }.sortedByDescending { it.nombre.length }

        val finalIngredientes = mutableListOf<Producto>()
        for (match in matches) {
            val matchNameLow = match.nombre.lowercase()
                .replace(".", "").replace(",", "").replace("(", "").replace(")", "")
            
            val alreadyCovered = finalIngredientes.any { 
                it.nombre.lowercase()
                    .replace(".", "").replace(",", "").replace("(", "").replace(")", "")
                    .contains(matchNameLow)
            }
            if (!alreadyCovered) {
                finalIngredientes.add(match)
            }
        }
        finalIngredientes.sortedBy { it.nombre }
    }

    val ingredientesSeleccionados = remember { 
        mutableStateListOf<Producto>().apply { addAll(ingredientesCombo) } 
    }
    
    var extraAnadido by remember { mutableStateOf<Producto?>(null) }
    var expandedExtra by remember { mutableStateOf(false) }

    // Cálculo dinámico de precio
    val costoIngredientesOmitidos = ingredientesCombo.filter { it !in ingredientesSeleccionados }.sumOf { it.precio }
    val costoExtra = extraAnadido?.precio ?: 0.0
    val precioFinal = (producto.precio - costoIngredientesOmitidos + costoExtra).coerceAtLeast(0.0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configurar: ${producto.nombre}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Precio Base: C$ ${producto.precio}", fontWeight = FontWeight.Medium)

                if (ingredientesCombo.isNotEmpty()) {
                    Text("Ingredientes Incluidos (Desmarca para quitar):", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Column {
                        ingredientesCombo.forEach { ingrediente ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().clickable {
                                    if (ingredientesSeleccionados.contains(ingrediente)) {
                                        ingredientesSeleccionados.remove(ingrediente)
                                    } else {
                                        ingredientesSeleccionados.add(ingrediente)
                                    }
                                }
                            ) {
                                Checkbox(
                                    checked = ingredientesSeleccionados.contains(ingrediente),
                                    onCheckedChange = null // Manejado por el Row clickable
                                )
                                Text("${ingrediente.nombre} (-C$ ${ingrediente.precio})", fontSize = 14.sp)
                            }
                        }
                    }
                }

                HorizontalDivider()

                Text("Añadir Extra:", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                ExposedDropdownMenuBox(expanded = expandedExtra, onExpandedChange = { expandedExtra = !expandedExtra }) {
                    OutlinedTextField(
                        value = extraAnadido?.nombre ?: "Sin extra",
                        onValueChange = {}, readOnly = true,
                        label = { Text("Seleccionar Extra (+ precio)") },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedExtra) }
                    )
                    ExposedDropdownMenu(expanded = expandedExtra, onDismissRequest = { expandedExtra = false }) {
                        DropdownMenuItem(text = { Text("Ninguno") }, onClick = { extraAnadido = null; expandedExtra = false })
                        ingredientesDisponibles.forEach { p ->
                            DropdownMenuItem(
                                text = { Text("${p.nombre} (+C$ ${p.precio})") },
                                onClick = { extraAnadido = p; expandedExtra = false }
                            )
                        }
                    }
                }

                HorizontalDivider()

                // SELECTOR DE CANTIDAD
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Cantidad:", fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FilledIconButton(
                            onClick = { if (cantidad > 1) cantidad-- },
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.LightGray)
                        ) { Text("-", fontWeight = FontWeight.Bold, fontSize = 20.sp) }
                        
                        Text(
                            cantidad.toString(), 
                            modifier = Modifier.padding(horizontal = 16.dp),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                        
                        FilledIconButton(
                            onClick = { cantidad++ },
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFF1B6D24))
                        ) { Text("+", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 20.sp) }
                    }
                }

                OutlinedTextField(
                    value = notasAdicionales, onValueChange = { notasAdicionales = it },
                    label = { Text("Especificaciones adicionales") }, modifier = Modifier.fillMaxWidth()
                )

                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Total Recalculado: C$ $precioFinal", 
                        fontWeight = FontWeight.Black, 
                        color = Color(0xFF1B6D24), 
                        fontSize = 18.sp,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val notaFinal = buildString {
                        val omitidos = ingredientesCombo.filter { it !in ingredientesSeleccionados }
                        if (omitidos.isNotEmpty()) {
                            append("SIN: ${omitidos.joinToString(", ") { it.nombre }}. ")
                        }
                        if (extraAnadido != null) {
                            append("EXTRA: ${extraAnadido!!.nombre}. ")
                        }
                        if (notasAdicionales.isNotBlank()) {
                            append(notasAdicionales)
                        }
                    }
                    val productoModificado = producto.copy(
                        id = java.util.UUID.randomUUID().toString(),
                        nombre = "${producto.nombre} (Config)",
                        descripcion = notaFinal.ifEmpty { producto.descripcion },
                        precio = precioFinal
                    )
                    onConfirm(productoModificado, cantidad)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E1E))
            ) { Text("Agregar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun ProductoItemCard(producto: Producto, onAgregar: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth().clickable { onAgregar() }
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(producto.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(4.dp))
            Text(producto.descripcion, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(8.dp))
            Text("C$ ${producto.precio}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onAgregar, modifier = Modifier.fillMaxWidth()) { Text("Configurar") }
        }
    }
}