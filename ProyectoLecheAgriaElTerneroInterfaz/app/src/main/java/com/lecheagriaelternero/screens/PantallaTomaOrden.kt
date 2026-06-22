package com.lecheagriaelternero.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
                title = { Text("Tomar Orden", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    val ordenActivaMesa by viewModel.ordenActivaMesa.collectAsStateWithLifecycle()

                    BadgedBox(
                        badge = {
                            if (carritoActual.isNotEmpty()) {
                                Badge { Text(carritoActual.size.toString()) }
                            }
                        },
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        IconButton(onClick = { navController.navigate("carrito") }) {
                            if (ordenActivaMesa != null) {
                                Surface(
                                    color = Color(0xFFFFEB3B),
                                    shape = androidx.compose.foundation.shape.CircleShape,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        Icons.Default.ShoppingCart,
                                        contentDescription = "Ver Carrito",
                                        tint = Color.Black,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            } else {
                                Icon(Icons.Default.ShoppingCart, contentDescription = "Ver Carrito")
                            }
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
            onConfirm = { productoModificado, cant ->
                repeat(cant) {
                    viewModel.agregarAlCarrito(productoModificado)
                }
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
    onConfirm: (Producto, Int) -> Unit
) {
    var notasAdicionales by remember { mutableStateOf("") }
    var cantidad by remember { mutableIntStateOf(1) }

    var terminoHuevo by remember { mutableStateOf("") }
    val nombreLow = producto.nombre.lowercase()
    val descLowProd = producto.descripcion.lowercase()

    val esHuevoEntero = nombreLow.contains("huevo") &&
            (nombreLow.contains("entero") || nombreLow.contains("ranchero") ||
                    descLowProd.contains("entero") || descLowProd.contains("ranchero"))

    val ingredientesDisponibles = menuReal.filter { it.categoria != "Combos" }

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

    val extrasSeleccionados = remember { mutableStateListOf<Producto>() }
    var expandedExtra by remember { mutableStateOf(false) }

    val costoIngredientesOmitidos = ingredientesCombo.filter { it !in ingredientesSeleccionados }.sumOf { it.precio }
    val costoExtras = extrasSeleccionados.sumOf { it.precio }
    val precioFinal = (producto.precio - costoIngredientesOmitidos + costoExtras).coerceAtLeast(0.0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configurar: ${producto.nombre}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
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
                                    onCheckedChange = null
                                )
                                Text("${ingrediente.nombre} (-C$ ${ingrediente.precio})", fontSize = 14.sp)
                            }
                        }
                    }
                }

                HorizontalDivider()

                if (esHuevoEntero) {
                    Text("Término de la Yema:", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    val opciones = listOf("Suaves", "Medios", "Duros")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        opciones.forEach { opcion ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { terminoHuevo = opcion }) {
                                RadioButton(selected = terminoHuevo == opcion, onClick = { terminoHuevo = opcion })
                                Text(opcion, fontSize = 12.sp)
                            }
                        }
                    }
                    HorizontalDivider()
                }

                Text("Añadir Extras (Puedes añadir varios):", fontSize = 14.sp, fontWeight = FontWeight.Bold)

                if (extrasSeleccionados.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        extrasSeleccionados.forEach { extra ->
                            Surface(
                                color = Color(0xFFF1F8E9),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("${extra.nombre} (+C$ ${extra.precio})", fontSize = 12.sp)
                                    IconButton(onClick = { extrasSeleccionados.remove(extra) }, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                ExposedDropdownMenuBox(expanded = expandedExtra, onExpandedChange = { expandedExtra = !expandedExtra }) {
                    OutlinedTextField(
                        value = "Toca para añadir un extra...",
                        onValueChange = {}, readOnly = true,
                        label = { Text("Añadir otro extra (+ precio)") },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedExtra) }
                    )
                    ExposedDropdownMenu(expanded = expandedExtra, onDismissRequest = { expandedExtra = false }) {
                        ingredientesDisponibles.forEach { p ->
                            DropdownMenuItem(
                                text = { Text("${p.nombre} (+C$ ${p.precio})") },
                                onClick = {
                                    extrasSeleccionados.add(p)
                                    expandedExtra = false
                                }
                            )
                        }
                    }
                }

                HorizontalDivider()

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
                            append("SIN: ${omitidos.joinToString(", ") { it.nombre.uppercase() }}. ")
                        }
                        if (terminoHuevo.isNotBlank()) {
                            append("TÉRMINO: $terminoHuevo. ")
                        }
                        if (extrasSeleccionados.isNotEmpty()) {
                            append("\nEXTRAS:\n")
                            extrasSeleccionados.forEach { append("   + ${it.nombre}\n") }
                        }
                        if (notasAdicionales.isNotBlank()) {
                            append("NOTAS: $notasAdicionales")
                        }
                    }
                    val productoModificado = producto.copy(
                        nombre = producto.nombre,
                        descripcion = notaFinal.trim().ifEmpty { producto.descripcion },
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