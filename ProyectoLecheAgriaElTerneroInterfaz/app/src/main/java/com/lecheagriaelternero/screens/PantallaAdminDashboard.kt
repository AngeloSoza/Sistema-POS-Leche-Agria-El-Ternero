package com.lecheagriaelternero.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
    val estadisticas by viewModel.estadisticas.collectAsStateWithLifecycle()

    val transferenciasDia = estadisticas.ventasTransferencia

    val ordenesPagadasHoy = ordenesActivas.filter { it.estado == "PAGADO" }
    val ventasLocal = ordenesPagadasHoy.sumOf { it.total }
    val totalGastos = gastosDelDia.sumOf { it.second }

    val ventasTotalesDia = ventasLocal + ventasPedidosYa
    val sumaBase = ventasTotalesDia + fondoInicial
    val efectivoEsperadoEnCaja = sumaBase - ventasPedidosYa - transferenciasDia - totalGastos

    val fechaActual = remember { SimpleDateFormat("EEEE, dd 'de' MMMM", Locale.forLanguageTag("es-NI")).format(Date()).replaceFirstChar { it.uppercase() } }

    val bgApp = remember { Color(0xFFF8F9FA) }
    val textPrimary = remember { Color(0xFF1A1C1E) }

    Scaffold(
        containerColor = bgApp,
        topBar = {
            TopAppBar(
                title = { Text("Administración Central", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = textPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgApp)
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            TabRow(
                selectedTabIndex = tabSeleccionado,
                containerColor = bgApp,
                contentColor = textPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[tabSeleccionado]),
                        color = textPrimary,
                        height = 3.dp
                    )
                },
                divider = { HorizontalDivider(color = Color(0xFFE5E7EB)) }
            ) {
                Tab(selected = tabSeleccionado == 0, onClick = { tabSeleccionado = 0 }) {
                    Text("Arqueo Caja", modifier = Modifier.padding(16.dp), fontWeight = if(tabSeleccionado == 0) FontWeight.Black else FontWeight.Medium)
                }
                Tab(selected = tabSeleccionado == 1, onClick = { tabSeleccionado = 1 }) {
                    Text("Analíticas", modifier = Modifier.padding(16.dp), fontWeight = if(tabSeleccionado == 1) FontWeight.Black else FontWeight.Medium)
                }
                Tab(selected = tabSeleccionado == 2, onClick = { tabSeleccionado = 2 }) {
                    Text("Gestión Menú", modifier = Modifier.padding(16.dp), fontWeight = if(tabSeleccionado == 2) FontWeight.Black else FontWeight.Medium)
                }
            }

            when (tabSeleccionado) {
                0 -> VistaArqueoCaja(
                    fechaActual, ventasLocal, fondoInicial, ventasPedidosYa, ventasTotalesDia, transferenciasDia, gastosDelDia, efectivoEsperadoEnCaja, viewModel
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
    viewModel: MenuViewModel
) {
    var mostrarDialogoGasto by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Text(fecha, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6C757D)) }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFC8E6C9)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("1. SUMA DE INGRESOS", fontWeight = FontWeight.Black, fontSize = 15.sp, color = Color(0xFF217128), letterSpacing = 1.sp)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFE8F5E9))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Ventas Restaurante:", fontWeight = FontWeight.Medium, color = Color(0xFF495057))
                        Text("C$ $ventasLocal", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color(0xFF1A1C1E))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Ventas PeYa:", fontWeight = FontWeight.Medium, color = Color(0xFF495057))
                        OutlinedTextField(
                            value = if(pedidosYa == 0.0) "" else pedidosYa.toString(),
                            onValueChange = { viewModel.setVentasPedidosYa(it.toDoubleOrNull() ?: 0.0) },
                            modifier = Modifier.width(140.dp).height(52.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            prefix = { Text("C$ ", color = Color.Gray) },
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0xFFE0E0E0))
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Caja Inicial:", fontWeight = FontWeight.Medium, color = Color(0xFF495057))
                        OutlinedTextField(
                            value = if(fondo == 0.0) "" else fondo.toString(),
                            onValueChange = { viewModel.setFondoInicial(it.toDoubleOrNull() ?: 0.0) },
                            modifier = Modifier.width(140.dp).height(52.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            prefix = { Text("C$ ", color = Color.Gray) },
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0xFFE0E0E0))
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFE8F5E9))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("TOTAL ACUMULADO:", fontWeight = FontWeight.ExtraBold, color = Color(0xFF217128), fontSize = 14.sp)
                        Text("C$ ${ventasTotales + fondo}", fontWeight = FontWeight.Black, fontSize = 20.sp, color = Color(0xFF217128))
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFFFCDD2)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("2. DESCUENTOS Y COMPRAS", fontWeight = FontWeight.Black, fontSize = 15.sp, color = Color(0xFFD32F2F), letterSpacing = 1.sp)
                        IconButton(
                            onClick = { mostrarDialogoGasto = true },
                            modifier = Modifier.background(Color(0xFFFFF0F2), RoundedCornerShape(8.dp)).size(32.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Añadir", tint = Color(0xFFD32F2F), modifier = Modifier.size(20.dp))
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFFFF0F2))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("- Retiro PeYa:", color = Color(0xFFD32F2F), fontWeight = FontWeight.Medium)
                        Text("C$ $pedidosYa", fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F), fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("- Transferencias:", color = Color(0xFFD32F2F), fontWeight = FontWeight.Medium)
                        OutlinedTextField(
                            value = if(transferencias == 0.0) "0.0" else transferencias.toString(),
                            onValueChange = { },
                            readOnly = true,
                            modifier = Modifier.width(140.dp).height(52.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            prefix = { Text("C$ ", color = Color.Gray) },
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color(0xFFFFCDD2),
                                focusedBorderColor = Color(0xFFD32F2F)
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("- Compras Locales:", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (gastos.isEmpty()) {
                        Text("No se han registrado compras.", color = Color.Gray, fontSize = 13.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            gastos.forEachIndexed { index, gasto ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().background(Color(0xFFF8F9FA), RoundedCornerShape(8.dp)).padding(start = 12.dp, top = 4.dp, bottom = 4.dp, end = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(gasto.first, modifier = Modifier.weight(1f), fontSize = 14.sp, color = Color(0xFF495057))
                                    Text("C$ ${gasto.second}", fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F), fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(onClick = { viewModel.eliminarGasto(index) }, modifier = Modifier.size(28.dp)) {
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Gray, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1C1E)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("CUADRE FINAL", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("C$ $esperado", color = Color(0xFFA0F399), fontSize = 42.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Efectivo físico que debe haber en caja.", color = Color(0xFF6C757D), fontSize = 13.sp, textAlign = TextAlign.Center)
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
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp),
            title = { Text("Registrar Compra", fontWeight = FontWeight.Black, fontSize = 20.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = desc, onValueChange = { desc = it },
                        label = { Text("Descripción (ej. Insumos)") },
                        singleLine = true, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = monto, onValueChange = { monto = it },
                        label = { Text("Monto (C$)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (desc.isNotBlank() && monto.toDoubleOrNull() != null) {
                            viewModel.agregarGasto(desc, monto.toDouble())
                            mostrarDialogoGasto = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF217128)),
                    shape = RoundedCornerShape(50)
                ) { Text("Guardar", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp)) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoGasto = false }) { Text("Cancelar", color = Color.Gray) }
            }
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
    val textPrimary = Color(0xFF1A1C1E)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Text("Resumen del $fecha", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                color = Color.White,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.fillMaxWidth().height(220.dp).padding(16.dp), contentAlignment = Alignment.Center) {
                    if (totalGlobal > 0) {
                        val angleLocal = (ventasLocal / totalGlobal).toFloat() * 360f
                        val anglePY = (ventasPedidosYa / totalGlobal).toFloat() * 360f
                        Canvas(modifier = Modifier.size(150.dp)) {
                            drawArc(color = Color(0xFF217128), startAngle = -90f, sweepAngle = angleLocal, useCenter = false, style = Stroke(width = 45f, cap = StrokeCap.Round))
                            drawArc(color = Color(0xFFD32F2F), startAngle = -90f + angleLocal, sweepAngle = anglePY, useCenter = false, style = Stroke(width = 45f, cap = StrokeCap.Round))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("INGRESOS", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            Text("C$ $totalGlobal", fontSize = 22.sp, fontWeight = FontWeight.Black, color = textPrimary)
                        }
                    } else {
                        Text("No hay datos suficientes.", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).background(Color(0xFF217128), CircleShape))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Local", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        }
                        Text("C$ $ventasLocal", fontSize = 22.sp, fontWeight = FontWeight.Black, color = textPrimary, modifier = Modifier.padding(top = 8.dp))
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).background(Color(0xFFD32F2F), CircleShape))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("PeYa", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        }
                        Text("C$ $ventasPedidosYa", fontSize = 22.sp, fontWeight = FontWeight.Black, color = textPrimary, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable { verTicketsDialog = true },
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                border = BorderStroke(1.dp, Color(0xFFFFE0B2)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(modifier = Modifier.padding(20.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Historial de Tickets", fontSize = 14.sp, color = Color(0xFFE65100), fontWeight = FontWeight.Bold)
                        Text("${ordenesPagadas.size} emitidos", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color(0xFFE65100))
                    }
                    Box(modifier = Modifier.background(Color(0xFFE65100), CircleShape).padding(12.dp)) {
                        Icon(Icons.Rounded.ReceiptLong, contentDescription = "Ver", tint = Color.White)
                    }
                }
            }
        }
    }

    if (verTicketsDialog) {
        Dialog(onDismissRequest = { verTicketsDialog = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Scaffold(
                containerColor = Color(0xFFF8F9FA),
                topBar = {
                    TopAppBar(
                        title = { Text("Recibos Emitidos", fontSize = 18.sp, fontWeight = FontWeight.Black) },
                        navigationIcon = { IconButton(onClick = { verTicketsDialog = false }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cerrar") } },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8F9FA))
                    )
                }
            ) { pd ->
                if (ordenesPagadas.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(pd), contentAlignment = Alignment.Center) { Text("No hay recibos hoy.", color = Color.Gray) }
                } else {
                    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(pd)) {
                        items(ordenesPagadas) { orden ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Column {
                                        Text("Ticket #${orden.id}", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                                        Text("Mesa ${orden.mesa?.numero ?: "N/A"}", fontSize = 13.sp, color = Color.Gray)
                                    }
                                    Text("C$ ${orden.total}", fontWeight = FontWeight.Black, color = Color(0xFF217128), fontSize = 16.sp)
                                    Row {
                                        IconButton(onClick = { ordenDetalle = orden }) { Icon(Icons.Default.Add, contentDescription = "Ver", tint = textPrimary) }
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

    val categorias = listOf("Combos", "Extras", "Bebidas")
    val menuAgrupado = remember(menu) {
        menu.groupBy { it.categoria }
    }

    var mostrarFormulario by remember { mutableStateOf(false) }
    var productoAEditar by remember { mutableStateOf<Producto?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Menú y Precios", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color(0xFF1A1C1E))
                Button(
                    onClick = { mostrarFormulario = true; productoAEditar = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF217128)),
                    shape = RoundedCornerShape(50)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Nuevo", fontWeight = FontWeight.Bold)
                }
            }
        }

        categorias.forEach { nombreCat ->
            val productosDeCat = menuAgrupado[nombreCat] ?: emptyList()
            if (productosDeCat.isNotEmpty()) {
                item {
                    Text(
                        text = nombreCat.uppercase(),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                        letterSpacing = 1.sp
                    )
                }
                items(productosDeCat, key = { it.id }) { prod ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = if (prod.disponible) Color.White else Color(0xFFF8F9FA)),
                        border = BorderStroke(1.dp, if (prod.disponible) Color(0xFFE5E7EB) else Color(0xFFEEEEEE)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(prod.nombre, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = if (prod.disponible) Color(0xFF1A1C1E) else Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("C$ ${prod.precio}", fontSize = 14.sp, color = if(prod.disponible) Color(0xFF217128) else Color.Gray, fontWeight = FontWeight.Bold)
                            }
                            IconButton(onClick = { productoAEditar = prod; mostrarFormulario = true }, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color(0xFF6C757D), modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = prod.disponible,
                                onCheckedChange = { nuevoEstado -> viewModel.modificarProductoEnBD(prod.copy(disponible = nuevoEstado)) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF217128),
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Color(0xFFD1D5DB),
                                    uncheckedBorderColor = Color.Transparent
                                )
                            )
                        }
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(40.dp)) }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoFormularioProducto(productoActual: Producto?, onDismiss: () -> Unit, onGuardar: (Producto) -> Unit, onEliminar: (String) -> Unit) {
    var nombre by remember { mutableStateOf(productoActual?.nombre ?: "") }
    var precio by remember { mutableStateOf(productoActual?.precio?.toString() ?: "") }
    var descripcion by remember { mutableStateOf(productoActual?.descripcion ?: "") }

    val listaCategorias = listOf(
        com.lecheagriaelternero.model.CategoriaBackend(1, "Combos"),
        com.lecheagriaelternero.model.CategoriaBackend(2, "Extras"),
        com.lecheagriaelternero.model.CategoriaBackend(3, "Bebidas")
    )
    var categoriaSeleccionada by remember { 
        mutableStateOf(productoActual?.categoriaObj ?: listaCategorias[1])
    }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp),
        title = { Text(if (productoActual == null) "Nuevo Producto" else "Editar Producto", fontWeight = FontWeight.Black, fontSize = 20.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = categoriaSeleccionada.nombre,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoría") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        listaCategorias.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.nombre) },
                                onClick = {
                                    categoriaSeleccionada = cat
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = nombre, onValueChange = { nombre = it },
                    label = { Text("Nombre del Producto") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = precio, onValueChange = { precio = it },
                    label = { Text("Precio (C$)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = descripcion, onValueChange = { descripcion = it },
                    label = { Text("Descripción") }, maxLines = 3, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = precio.toDoubleOrNull() ?: 0.0
                    if (nombre.isNotBlank()) {
                        val nuevo = Producto(
                            id = productoActual?.id ?: "",
                            nombre = nombre,
                            precio = p,
                            descripcion = descripcion,
                            disponible = productoActual?.disponible ?: true,
                            categoriaObj = categoriaSeleccionada
                        )
                        onGuardar(nuevo)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1C1E)),
                shape = RoundedCornerShape(50)
            ) { Text("Guardar", modifier = Modifier.padding(horizontal = 12.dp), fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (productoActual != null) {
                    IconButton(onClick = { onEliminar(productoActual.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFD32F2F))
                    }
                }
                TextButton(onClick = onDismiss) { Text("Cancelar", color = Color.Gray, fontWeight = FontWeight.Bold) }
            }
        }
    )
}