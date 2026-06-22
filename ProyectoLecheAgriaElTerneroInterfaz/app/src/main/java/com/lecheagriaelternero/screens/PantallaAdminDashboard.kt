package com.lecheagriaelternero.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.lecheagriaelternero.model.OrdenBackend
import com.lecheagriaelternero.model.Producto
import com.lecheagriaelternero.viewmodel.MenuViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAdminDashboard(navController: NavController, viewModel: MenuViewModel) {
    var tabSeleccionado by remember { mutableIntStateOf(0) }

    val ordenesActivas by viewModel.ordenesActivas.collectAsStateWithLifecycle()
    val fondoInicial by viewModel.fondoInicial.collectAsStateWithLifecycle()
    val ventasPedidosYa by viewModel.ventasPedidosYa.collectAsStateWithLifecycle()
    val gastosDelDia by viewModel.gastosDelDia.collectAsStateWithLifecycle()
    val menuReal by viewModel.menu.collectAsStateWithLifecycle()

    var transferenciasDia by remember { mutableDoubleStateOf(0.0) }

    val ordenesPagadasHoy = ordenesActivas.filter { it.estado == "PAGADO" }
    val ventasLocal = ordenesPagadasHoy.sumOf { it.total }
    val totalGastos = gastosDelDia.sumOf { it.second }

    // 🧮 FÓRMULA EXACTA DE ARQUEO:
    val ventasTotalesDia = ventasLocal + ventasPedidosYa
    val sumaBase = ventasTotalesDia + fondoInicial
    val efectivoEsperadoEnCaja = sumaBase - ventasPedidosYa - transferenciasDia - totalGastos

    val fechaActual = SimpleDateFormat("EEEE, dd 'de' MMMM", Locale.forLanguageTag("es-NI")).format(Date()).replaceFirstChar { it.uppercase() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Administración Central", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF5F5F5))
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).background(Color(0xFFFAFAFA))) {

            PrimaryTabRow(
                selectedTabIndex = tabSeleccionado,
                containerColor = Color(0xFF1E1E1E),
                contentColor = Color.White
            ) {
                Tab(selected = tabSeleccionado == 0, onClick = { tabSeleccionado = 0 }) { Text("Arqueo Caja", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold) }
                Tab(selected = tabSeleccionado == 1, onClick = { tabSeleccionado = 1 }) { Text("Analíticas", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold) }
                Tab(selected = tabSeleccionado == 2, onClick = { tabSeleccionado = 2 }) { Text("Gestión Menú", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold) }
            }

            when (tabSeleccionado) {
                0 -> VistaArqueoCaja(
                    fechaActual, ventasLocal, fondoInicial, ventasPedidosYa, ventasTotalesDia, transferenciasDia, gastosDelDia, efectivoEsperadoEnCaja, viewModel,
                    onTransferenciasChange = { transferenciasDia = it }
                )
                1 -> VistaAnaliticasProfesional(fechaActual, ventasLocal, ventasPedidosYa, ordenesPagadasHoy, menuReal)
                2 -> VistaGestorInventario(menuReal, viewModel)
            }
        }
    }
}

@Composable
fun VistaArqueoCaja(
    fecha: String,
    ventasLocal: Double,
    fondo: Double,
    pedidosYa: Double,
    ventasTotales: Double,
    transferencias: Double,
    gastos: List<Pair<String, Double>>,
    esperado: Double,
    viewModel: MenuViewModel,
    onTransferenciasChange: (Double) -> Unit
) {
    var mostrarDialogoGasto by remember { mutableStateOf(false) }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text(fecha, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Gray) }

        item {
            Card(elevation = CardDefaults.cardElevation(2.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9))) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("1. SUMA DE INGRESOS", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color(0xFF1B6D24))
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFC8E6C9))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Ventas Restaurante:", fontWeight = FontWeight.Medium)
                        Text("C$ $ventasLocal", fontWeight = FontWeight.Black, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Ventas PeYa:", fontWeight = FontWeight.Medium)
                        OutlinedTextField(
                            value = if(pedidosYa == 0.0) "" else pedidosYa.toString(),
                            onValueChange = { viewModel.setVentasPedidosYa(it.toDoubleOrNull() ?: 0.0) },
                            modifier = Modifier.width(130.dp).height(50.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            prefix = { Text("C$") }
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Caja Inicial:", fontWeight = FontWeight.Medium)
                        OutlinedTextField(
                            value = if(fondo == 0.0) "" else fondo.toString(),
                            onValueChange = { viewModel.setFondoInicial(it.toDoubleOrNull() ?: 0.0) },
                            modifier = Modifier.width(130.dp).height(50.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            prefix = { Text("C$") }
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFC8E6C9))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("TOTAL ACUMULADO:", fontWeight = FontWeight.Bold, color = Color(0xFF1B6D24))
                        Text("C$ ${ventasTotales + fondo}", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color(0xFF1B6D24))
                    }
                }
            }
        }

        item {
            Card(elevation = CardDefaults.cardElevation(2.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("2. DESCUENTOS Y COMPRAS", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color(0xFFD32F2F))
                        IconButton(onClick = { mostrarDialogoGasto = true }, modifier = Modifier.background(Color.White, RoundedCornerShape(8.dp)).size(32.dp)) {
                            Icon(Icons.Default.Add, contentDescription = "Añadir", tint = Color(0xFFD32F2F), modifier = Modifier.size(20.dp))
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFFFCDD2))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("- PeYa:", color = Color(0xFFD32F2F), fontWeight = FontWeight.Medium)
                        Text("C$ $pedidosYa", fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("- Transferencias:", color = Color(0xFFD32F2F), fontWeight = FontWeight.Medium)
                        OutlinedTextField(
                            value = if(transferencias == 0.0) "" else transferencias.toString(),
                            onValueChange = { onTransferenciasChange(it.toDoubleOrNull() ?: 0.0) },
                            modifier = Modifier.width(130.dp).height(50.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            prefix = { Text("C$") }
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("- Compras:", color = Color(0xFFD32F2F), fontWeight = FontWeight.Medium)
                    if (gastos.isEmpty()) {
                        Text("  (Ninguna compra registrada)", color = Color.Gray, fontSize = 12.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                    } else {
                        gastos.forEachIndexed { index, gasto ->
                            // 🛡️ SOLUCIÓN APLICADA: Separación de los paddings
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).padding(start = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(gasto.first, modifier = Modifier.weight(1f), fontSize = 14.sp)
                                Text("C$ ${gasto.second}", fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F), fontSize = 14.sp)
                                IconButton(onClick = { viewModel.eliminarGasto(index) }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)), elevation = CardDefaults.cardElevation(4.dp)) {
                Column(modifier = Modifier.padding(24.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("CUADRE: EFECTIVO FÍSICO ESPERADO", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("C$ $esperado", color = Color(0xFF4CAF50), fontSize = 40.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Debe coincidir exactamente con los billetes de la caja.", color = Color.Gray, fontSize = 11.sp, textAlign = TextAlign.Center)
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    if (mostrarDialogoGasto) {
        var desc by remember { mutableStateOf("") }
        var monto by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { mostrarDialogoGasto = false },
            title = { Text("Registrar Compra", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Descripción (ej. Leche Agria, Tortillas)") }, singleLine = true)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = monto, onValueChange = { monto = it }, label = { Text("Monto en C$") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (desc.isNotBlank() && monto.toDoubleOrNull() != null) {
                        viewModel.agregarGasto(desc, monto.toDouble())
                        mostrarDialogoGasto = false
                    }
                }) { Text("Guardar") }
            },
            dismissButton = { TextButton(onClick = { mostrarDialogoGasto = false }) { Text("Cancelar") } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VistaAnaliticasProfesional(fecha: String, ventasLocal: Double, ventasPedidosYa: Double, ordenesPagadas: List<OrdenBackend>, menuReal: List<Producto>) {
    val totalGlobal = ventasLocal + ventasPedidosYa
    var verTicketsDialog by remember { mutableStateOf(false) }
    var ordenDetalle by remember { mutableStateOf<OrdenBackend?>(null) }
    val context = LocalContext.current

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Text("Rendimiento: $fecha", fontSize = 18.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(24.dp))

            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                if (totalGlobal > 0) {
                    val angleLocal = (ventasLocal / totalGlobal).toFloat() * 360f
                    val anglePY = (ventasPedidosYa / totalGlobal).toFloat() * 360f
                    Canvas(modifier = Modifier.size(160.dp)) {
                        drawArc(color = Color(0xFF1B6D24), startAngle = -90f, sweepAngle = angleLocal, useCenter = false, style = Stroke(width = 50f, cap = StrokeCap.Butt))
                        drawArc(color = Color(0xFFD32F2F), startAngle = -90f + angleLocal, sweepAngle = anglePY, useCenter = false, style = Stroke(width = 50f, cap = StrokeCap.Butt))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("TOTAL", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text("C$ $totalGlobal", fontSize = 22.sp, fontWeight = FontWeight.Black)
                    }
                } else {
                    Text("Sin datos de ventas", color = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(12.dp).background(Color(0xFF1B6D24), CircleShape))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Restaurante", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("C$ $ventasLocal", fontSize = 20.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 8.dp))
                    }
                }
                Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(12.dp).background(Color(0xFFD32F2F), CircleShape))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("PedidosYa", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("C$ $ventasPedidosYa", fontSize = 20.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth().clickable { verTicketsDialog = true },
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(modifier = Modifier.padding(20.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Tickets del Día", fontSize = 14.sp, color = Color(0xFFE65100), fontWeight = FontWeight.Bold)
                        Text("${ordenesPagadas.size} comprobantes", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color(0xFFE65100))
                    }
                    Button(onClick = { verTicketsDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100))) {
                        Text("Ver Recibos")
                    }
                }
            }
        }
    }

    if (verTicketsDialog) {
        Dialog(onDismissRequest = { verTicketsDialog = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFF5F5F5)) {
                Column {
                    TopAppBar(
                        title = { Text("Recibos - $fecha", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                        navigationIcon = { IconButton(onClick = { verTicketsDialog = false }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cerrar") } }
                    )
                    if (ordenesPagadas.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No hay recibos hoy.", color = Color.Gray) }
                    } else {
                        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(ordenesPagadas) { orden ->
                                Card(elevation = CardDefaults.cardElevation(2.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Column {
                                            Text("Ticket #${orden.id}", fontWeight = FontWeight.Bold)
                                            Text("Mesa ${orden.mesa?.numero}", fontSize = 12.sp, color = Color.Gray)
                                        }
                                        Text("C$ ${orden.total}", fontWeight = FontWeight.Black, color = Color(0xFF1B6D24))
                                        Row {
                                            IconButton(onClick = { ordenDetalle = orden }) { Icon(Icons.Default.Add, contentDescription = "Ver") }
                                            IconButton(onClick = { generarReciboPDF(context, orden, menuReal, "Auditoría Admin") }) { Icon(Icons.Default.Edit, contentDescription = "PDF") }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (ordenDetalle != null) {
        DialogoVerRecibo(
            orden = ordenDetalle!!,
            menuReal = menuReal,
            metodoUsado = "Auditoría Interna",
            onDismiss = { ordenDetalle = null },
            onImprimir = { generarReciboPDF(context, ordenDetalle!!, menuReal, "Auditoría Admin") }
        )
    }
}

@Composable
fun VistaGestorInventario(menu: List<Producto>, viewModel: MenuViewModel) {
    val menuOrdenado = menu.sortedBy { it.id }

    var mostrarFormulario by remember { mutableStateOf(false) }
    var productoAEditar by remember { mutableStateOf<Producto?>(null) }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Catálogo de Productos", fontSize = 20.sp, fontWeight = FontWeight.Black)
                Button(onClick = { mostrarFormulario = true; productoAEditar = null }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E1E))) {
                    Text("+ Nuevo")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        items(menuOrdenado, key = { it.id }) { prod ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(containerColor = if (prod.disponible) Color.White else Color(0xFFEEEEEE)),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(prod.nombre, fontWeight = FontWeight.Bold, color = if (prod.disponible) Color.Black else Color.Gray)
                        Text("C$ ${prod.precio}", fontSize = 14.sp, color = Color(0xFF1B6D24), fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = { productoAEditar = prod; mostrarFormulario = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.Gray)
                    }
                    Switch(
                        checked = prod.disponible,
                        onCheckedChange = { nuevoEstado -> viewModel.modificarProductoEnBD(prod.copy(disponible = nuevoEstado)) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF1B6D24), checkedTrackColor = Color(0xFFA5D6A7))
                    )
                }
            }
        }
    }

    if (mostrarFormulario) {
        DialogoFormularioProducto(
            productoActual = productoAEditar,
            onDismiss = { mostrarFormulario = false },
            onGuardar = { prod ->
                if (productoAEditar == null) viewModel.crearProducto(prod) else viewModel.modificarProductoEnBD(prod)
                mostrarFormulario = false
            },
            onEliminar = { id ->
                viewModel.eliminarProducto(id)
                mostrarFormulario = false
            }
        )
    }
}

@Composable
fun DialogoFormularioProducto(productoActual: Producto?, onDismiss: () -> Unit, onGuardar: (Producto) -> Unit, onEliminar: (String) -> Unit) {
    var nombre by remember { mutableStateOf(productoActual?.nombre ?: "") }
    var precio by remember { mutableStateOf(productoActual?.precio?.toString() ?: "") }
    var descripcion by remember { mutableStateOf(productoActual?.descripcion ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (productoActual == null) "Crear Nuevo Producto" else "Editar Producto", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") }, singleLine = true)
                OutlinedTextField(value = precio, onValueChange = { precio = it }, label = { Text("Precio (C$)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                OutlinedTextField(value = descripcion, onValueChange = { descripcion = it }, label = { Text("Descripción") }, maxLines = 3)
            }
        },
        confirmButton = {
            Button(onClick = {
                val p = precio.toDoubleOrNull() ?: 0.0
                if (nombre.isNotBlank()) {
                    val nuevo = Producto(
                        id = productoActual?.id ?: "",
                        nombre = nombre,
                        precio = p,
                        descripcion = descripcion,
                        disponible = productoActual?.disponible ?: true
                    )
                    onGuardar(nuevo)
                }
            }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B6D24))) { Text("Guardar") }
        },
        dismissButton = {
            Row {
                if (productoActual != null) {
                    IconButton(onClick = { onEliminar(productoActual.id) }) { Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red) }
                }
                TextButton(onClick = onDismiss) { Text("Cancelar") }
            }
        }
    )
}