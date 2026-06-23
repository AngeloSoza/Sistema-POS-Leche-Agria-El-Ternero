package com.lecheagriaelternero.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

    val categorias = remember(menuReal) { listOf("Todos") + menuReal.map { it.categoria }.distinct() }
    var categoriaSeleccionada by remember { mutableStateOf("Todos") }
    var productoACustomizar by remember { mutableStateOf<Producto?>(null) }

    val productosMostrados = remember(menuReal, categoriaSeleccionada) {
        menuReal.filter { producto ->
            val coincideCategoria = categoriaSeleccionada == "Todos" || producto.categoria == categoriaSeleccionada
            coincideCategoria && producto.disponible
        }
    }

    val bgApp = remember { Color(0xFFF9FAFB) }
    val textPrimary = remember { Color(0xFF111827) }

    Scaffold(
        containerColor = bgApp,
        topBar = {
            TopAppBar(
                title = { Text("Tomar Orden", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgApp),
                actions = {
                    val ordenActivaMesa by viewModel.ordenActivaMesa.collectAsStateWithLifecycle()

                    BadgedBox(
                        badge = {
                            if (carritoActual.isNotEmpty()) {
                                Badge(containerColor = Color(0xFFDC2626)) { Text(carritoActual.size.toString()) }
                            }
                        },
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        IconButton(onClick = { navController.navigate("carrito") }) {
                            if (ordenActivaMesa != null) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color(0xFFFBBF24), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.ShoppingCart, contentDescription = "Carrito", tint = textPrimary, modifier = Modifier.size(18.dp))
                                }
                            } else {
                                Icon(Icons.Default.ShoppingCart, contentDescription = "Carrito", tint = textPrimary)
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
                modifier = Modifier.fillMaxWidth(),
                containerColor = bgApp,
                divider = {}
            ) {
                categorias.forEachIndexed { index, categoria ->
                    val isSelected = categoriaSeleccionada == categoria
                    Tab(
                        selected = isSelected,
                        onClick = { categoriaSeleccionada = categoria },
                        text = { Text(categoria, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, fontSize = 14.sp) }
                    )
                }
            }

            if (productosMostrados.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay productos disponibles.", color = Color.Gray)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 150.dp),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Agregamos un KEY único por producto para optimizar drásticamente el reciclaje de celdas
                    items(productosMostrados, key = { it.nombre }) { producto ->
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

    val textPrimary = remember { Color(0xFF111827) }
    val nombreLow = remember(producto.nombre) { producto.nombre.lowercase() }
    val descLowProd = remember(producto.descripcion) { producto.descripcion.lowercase() }

    val esHuevoEntero = nombreLow.contains("huevo") &&
            (nombreLow.contains("entero") || nombreLow.contains("ranchero") ||
                    descLowProd.contains("entero") || descLowProd.contains("ranchero"))

    val ingredientesDisponibles = remember(menuReal) { menuReal.filter { it.categoria != "Combos" } }

    val ingredientesCombo = remember(producto.descripcion, menuReal) {
        val descLow = producto.descripcion.lowercase()
            .replace("un ", "1 ").replace("uno ", "1 ").replace("dos ", "2 ").replace("tres ", "3 ")
            .replace(".", "").replace(",", "").replace("(", "").replace(")", "").replace("  ", " ")

        val matches = ingredientesDisponibles.filter { item ->
            val itemNameLow = item.nombre.lowercase().replace(".", "").replace(",", "").replace("(", "").replace(")", "").replace("  ", " ")
            descLow.contains(itemNameLow) ||
                    (itemNameLow == "1 huevo entero" && descLow.contains("un huevo entero")) ||
                    (itemNameLow == "2 huevos enteros" && descLow.contains("dos huevos enteros")) ||
                    (itemNameLow == "café negro" && descLow.contains("café"))
        }.sortedByDescending { it.nombre.length }

        val finalIngredientes = mutableListOf<Producto>()
        for (match in matches) {
            val matchNameLow = match.nombre.lowercase().replace(".", "").replace(",", "").replace("(", "").replace(")", "")
            val alreadyCovered = finalIngredientes.any { it.nombre.lowercase().replace(".", "").replace(",", "").replace("(", "").replace(")", "").contains(matchNameLow) }
            if (!alreadyCovered) finalIngredientes.add(match)
        }
        finalIngredientes.sortedBy { it.nombre }
    }

    val ingredientesSeleccionados = remember { mutableStateListOf<Producto>().apply { addAll(ingredientesCombo) } }
    val extrasSeleccionados = remember { mutableStateListOf<Producto>() }
    var expandedExtra by remember { mutableStateOf(false) }

    val costoIngredientesOmitidos = ingredientesCombo.filter { it !in ingredientesSeleccionados }.sumOf { it.precio }
    val costoExtras = extrasSeleccionados.sumOf { it.precio }
    val precioFinal = (producto.precio - costoIngredientesOmitidos + costoExtras).coerceAtLeast(0.0)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp),
        title = { Text(producto.nombre, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {

                Box(modifier = Modifier.background(Color(0xFFF3F4F6), RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text("Precio Base: C$ ${producto.precio}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = textPrimary)
                }

                if (ingredientesCombo.isNotEmpty()) {
                    Text("Ingredientes incluidos:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Column {
                        ingredientesCombo.forEach { ingrediente ->
                            val checked = ingredientesSeleccionados.contains(ingrediente)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (checked) ingredientesSeleccionados.remove(ingrediente)
                                        else ingredientesSeleccionados.add(ingrediente)
                                    }
                                    .padding(vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = if (checked) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (checked) Color(0xFF16A34A) else Color.LightGray,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("${ingrediente.nombre} (-C$ ${ingrediente.precio})", fontSize = 14.sp, color = textPrimary)
                            }
                        }
                    }
                    HorizontalDivider(color = Color(0xFFF3F4F6))
                }

                if (esHuevoEntero) {
                    Text("Término de la Yema:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Suaves", "Medios", "Duros").forEach { opcion ->
                            val isSelected = terminoHuevo == opcion
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (isSelected) Color(0xFFDCFCE7) else Color.White, RoundedCornerShape(8.dp))
                                    .clickable { terminoHuevo = opcion }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(opcion, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color(0xFF16A34A) else textPrimary)
                            }
                        }
                    }
                    HorizontalDivider(color = Color(0xFFF3F4F6))
                }

                Text("Extras Añadidos:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                if (extrasSeleccionados.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        extrasSeleccionados.forEach { extra ->
                            Row(
                                modifier = Modifier.fillMaxWidth().background(Color(0xFFF3F4F6), RoundedCornerShape(6.dp)).padding(start = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${extra.nombre} (+C$ ${extra.precio})", fontSize = 13.sp)
                                IconButton(onClick = { extrasSeleccionados.remove(extra) }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                ExposedDropdownMenuBox(expanded = expandedExtra, onExpandedChange = { expandedExtra = !expandedExtra }) {
                    OutlinedTextField(
                        value = "", onValueChange = {}, readOnly = true,
                        placeholder = { Text("Añadir extra...", fontSize = 14.sp) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(8.dp),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedExtra) }
                    )
                    ExposedDropdownMenu(expanded = expandedExtra, onDismissRequest = { expandedExtra = false }, containerColor = Color.White) {
                        ingredientesDisponibles.forEach { p ->
                            DropdownMenuItem(
                                text = { Text("${p.nombre} (+C$ ${p.precio})", fontSize = 14.sp) },
                                onClick = {
                                    extrasSeleccionados.add(p)
                                    expandedExtra = false
                                }
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFF3F4F6))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Cantidad:", fontWeight = FontWeight.Bold, color = textPrimary)
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.background(Color(0xFFF3F4F6), RoundedCornerShape(50))) {
                        IconButton(onClick = { if (cantidad > 1) cantidad-- }, modifier = Modifier.size(36.dp)) { Icon(Icons.Default.Remove, null) }
                        Text(cantidad.toString(), modifier = Modifier.padding(horizontal = 8.dp), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { cantidad++ }, modifier = Modifier.size(36.dp)) { Icon(Icons.Default.Add, null) }
                    }
                }

                OutlinedTextField(
                    value = notasAdicionales, onValueChange = { notasAdicionales = it },
                    placeholder = { Text("Instrucciones de cocina...") }, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                // Totalizador Sólido Ligero
                Box(
                    modifier = Modifier.fillMaxWidth().background(Color(0xFF1E2022), RoundedCornerShape(10.dp)).padding(12.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Recalculado:", color = Color.White, fontSize = 14.sp)
                        Text("C$ $precioFinal", fontWeight = FontWeight.Black, color = Color(0xFF4ADE80), fontSize = 16.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val notaFinal = buildString {
                        val omitidos = ingredientesCombo.filter { it !in ingredientesSeleccionados }
                        if (omitidos.isNotEmpty()) append("SIN: ${omitidos.joinToString(", ") { it.nombre.uppercase() }}. ")
                        if (terminoHuevo.isNotBlank()) append("TÉRMINO: $terminoHuevo. ")
                        if (extrasSeleccionados.isNotEmpty()) {
                            append("\nEXTRAS:\n")
                            extrasSeleccionados.forEach { append("   + ${it.nombre}\n") }
                        }
                        if (notasAdicionales.isNotBlank()) append("NOTAS: $notasAdicionales")
                    }
                    onConfirm(producto.copy(descripcion = notaFinal.trim().ifEmpty { producto.descripcion }, precio = precioFinal), cantidad)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                shape = RoundedCornerShape(8.dp)
            ) { Text("Confirmar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = Color.Gray) } }
    )
}

@Composable
fun ProductoItemCard(producto: Producto, onAgregar: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        modifier = Modifier.fillMaxWidth().clickable { onAgregar() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(producto.nombre, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF111827), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(2.dp))
            Text(producto.descripcion, fontSize = 12.sp, color = Color(0xFF6B7280), maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.height(32.dp))
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("C$ ${producto.precio}", fontSize = 16.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.Bold)
                Box(modifier = Modifier.size(28.dp).background(Color(0xFF1E2022), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}