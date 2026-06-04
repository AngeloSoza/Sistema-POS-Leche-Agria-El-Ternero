package com.lecheagriaelternero.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.lecheagriaelternero.model.Producto
import com.lecheagriaelternero.viewmodel.MenuViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaTomaOrden(navController: NavController, viewModel: MenuViewModel) {
    val menuReal by viewModel.menu.collectAsStateWithLifecycle()
    val carritoActual by viewModel.carritoActual.collectAsStateWithLifecycle()

    val categorias = listOf("Todos") + menuReal.map { it.categoria }.distinct()
    var categoriaSeleccionada by remember { mutableStateOf("Todos") }
    var productoACustomizar by remember { mutableStateOf<Producto?>(null) }

    val productosMostrados = menuReal.filter { producto ->
        val coincideCategoria = categoriaSeleccionada == "Todos" || producto.categoria == categoriaSeleccionada
        coincideCategoria && producto.disponible
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tomar Orden") },
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
            onConfirm = { productoModificado ->
                viewModel.agregarAlCarrito(productoModificado)
                productoACustomizar = null
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
    onConfirm: (Producto) -> Unit
) {
    var notas by remember { mutableStateOf("") }
    var itemAQuitar by remember { mutableStateOf<Producto?>(null) }
    var itemAAgregar by remember { mutableStateOf<Producto?>(null) }
    var expandedQuitar by remember { mutableStateOf(false) }
    var expandedAgregar by remember { mutableStateOf(false) }

    val opcionesExtras = menuReal.filter { it.precio < 60.0 && it.id != producto.id }

    val precioQuitar = itemAQuitar?.precio ?: 0.0
    val precioAgregar = itemAAgregar?.precio ?: 0.0
    val precioFinal = producto.precio - precioQuitar + precioAgregar

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configurar: ${producto.nombre}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Precio Base: C$ ${producto.precio}", fontWeight = FontWeight.Medium)

                ExposedDropdownMenuBox(expanded = expandedQuitar, onExpandedChange = { expandedQuitar = !expandedQuitar }) {
                    OutlinedTextField(
                        value = itemAQuitar?.nombre ?: "Sin cambios",
                        onValueChange = {}, readOnly = true,
                        label = { Text("Quitar ingrediente (- precio)") },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedQuitar) }
                    )
                    ExposedDropdownMenu(expanded = expandedQuitar, onDismissRequest = { expandedQuitar = false }) {
                        DropdownMenuItem(text = { Text("Ninguno") }, onClick = { itemAQuitar = null; expandedQuitar = false })
                        opcionesExtras.forEach { p ->
                            DropdownMenuItem(
                                text = { Text("${p.nombre} (-C$${p.precio})") },
                                onClick = { itemAQuitar = p; expandedQuitar = false }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(expanded = expandedAgregar, onExpandedChange = { expandedAgregar = !expandedAgregar }) {
                    OutlinedTextField(
                        value = itemAAgregar?.nombre ?: "Sin extra",
                        onValueChange = {}, readOnly = true,
                        label = { Text("Agregar extra (+ precio)") },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAgregar) }
                    )
                    ExposedDropdownMenu(expanded = expandedAgregar, onDismissRequest = { expandedAgregar = false }) {
                        DropdownMenuItem(text = { Text("Ninguno") }, onClick = { itemAAgregar = null; expandedAgregar = false })
                        opcionesExtras.forEach { p ->
                            DropdownMenuItem(
                                text = { Text("${p.nombre} (+C$${p.precio})") },
                                onClick = { itemAAgregar = p; expandedAgregar = false }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = notas, onValueChange = { notas = it },
                    label = { Text("Especificaciones adicionales") }, modifier = Modifier.fillMaxWidth()
                )

                Text("Total Recalculado: C$ $precioFinal", fontWeight = FontWeight.Black, color = Color(0xFF1B6D24), fontSize = 18.sp)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val notaFinal = buildString {
                        if (itemAQuitar != null) append("Sin ${itemAQuitar!!.nombre}. ")
                        if (itemAAgregar != null) append("Cambio por ${itemAAgregar!!.nombre}. ")
                        if (notas.isNotBlank()) append(notas)
                    }
                    val productoModificado = producto.copy(
                        id = java.util.UUID.randomUUID().toString(),
                        nombre = "${producto.nombre} (Config)",
                        descripcion = notaFinal.ifEmpty { producto.descripcion },
                        precio = precioFinal
                    )
                    onConfirm(productoModificado)
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