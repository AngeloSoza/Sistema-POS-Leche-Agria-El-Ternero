package com.lecheagriaelternero.screens

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.lecheagriaelternero.R
import com.lecheagriaelternero.model.OrdenBackend
import com.lecheagriaelternero.model.Producto
import com.lecheagriaelternero.viewmodel.MenuViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

data class ItemCajaParseado(
    val idUnico: String,
    val cantidad: Int,
    val nombre: String,
    val precioCalculado: Double,
    val bloqueTextoOriginal: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCaja(navController: NavController, viewModel: MenuViewModel) {
    val ordenesActivas by viewModel.ordenesActivas.collectAsStateWithLifecycle()
    val menuReal by viewModel.menu.collectAsStateWithLifecycle()
    val errorState by viewModel.errorState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(errorState) {
        errorState?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.descartarError()
        }
    }

    var tabSeleccionado by remember { mutableIntStateOf(0) }
    var textoBusqueda by remember { mutableStateOf("") }

    val ordenesPendientes = ordenesActivas.filter { it.estado != "PAGADO" }.sortedBy { it.id }
    val ordenesPagadas = ordenesActivas.filter { it.estado == "PAGADO" }
        .filter {
            it.mesa?.numero?.contains(textoBusqueda, ignoreCase = true) == true ||
                    it.id.toString().contains(textoBusqueda)
        }
        .sortedByDescending { it.id }

    var ordenAEditar by remember { mutableStateOf<OrdenBackend?>(null) }
    var ordenAVerDetalle by remember { mutableStateOf<OrdenBackend?>(null) }

    val metodosPagoMesas = remember { mutableStateMapOf<Long, String>() }
    var isProcessing by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Caja Principal", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF5F5F5))
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            TabRow(
                selectedTabIndex = tabSeleccionado,
                containerColor = Color(0xFF1B6D24),
                contentColor = Color.White
            ) {
                Tab(selected = tabSeleccionado == 0, onClick = { tabSeleccionado = 0 }) {
                    Text("Cuentas Pendientes", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
                }
                Tab(selected = tabSeleccionado == 1, onClick = { tabSeleccionado = 1 }) {
                    Text("Cuentas Pagadas", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
                }
            }

            if (tabSeleccionado == 0) {
                if (ordenesPendientes.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay cuentas por cobrar", color = Color.Gray)
                    }
                } else {
                    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(ordenesPendientes, key = { it.id }) { orden ->
                            val metodoActual = metodosPagoMesas[orden.id] ?: "Efectivo"
                            CardCajaPendiente(
                                orden = orden,
                                isProcessing = isProcessing,
                                onCobrar = {
                                    if (!isProcessing) {
                                        isProcessing = true
                                        // 🛡️ SOLUCIÓN: Pasamos el ID de la ORDEN (que es seguro y existe)
                                        viewModel.cobrarMesa(orden.id, metodoActual)
                                        Toast.makeText(context, "Procesando cobro: ${metodoActual}", Toast.LENGTH_SHORT).show()

                                        // Bloqueamos el botón por 1.5s para evitar doble clic mientras el servidor responde
                                        coroutineScope.launch {
                                            delay(1500)
                                            isProcessing = false
                                        }
                                    }
                                },
                                onEditar = { ordenAEditar = orden },
                                onMetodoCambio = { metodosPagoMesas[orden.id] = it },
                                metodoActual = metodoActual
                            )
                        }
                    }
                }
            } else {
                OutlinedTextField(
                    value = textoBusqueda,
                    onValueChange = { textoBusqueda = it },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Buscar por Mesa o Ticket ID...") },
                    leadingIcon = { Text("🔍", modifier = Modifier.padding(start = 8.dp)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1B6D24),
                        unfocusedBorderColor = Color.LightGray
                    )
                )

                if (ordenesPagadas.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay cuentas pagadas hoy", color = Color.Gray)
                    }
                } else {
                    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(ordenesPagadas, key = { it.id }) { orden ->
                            val metodoUsado = metodosPagoMesas[orden.id] ?: "Efectivo"
                            CardReciboHistorial(
                                orden = orden,
                                onVerDetalle = { ordenAVerDetalle = orden },
                                onImprimir = { generarReciboPDF(context, orden, menuReal, metodoUsado) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (ordenAEditar != null) {
        DialogoEditorCaja(
            orden = ordenAEditar!!,
            menuReal = menuReal,
            onDismiss = { ordenAEditar = null },
            onGuardar = { id, nuevasNotas, nuevoTotal ->
                viewModel.actualizarOrdenManual(id, nuevasNotas, nuevoTotal)
                ordenAEditar = null
            }
        )
    }

    if (ordenAVerDetalle != null) {
        val metodoUsado = metodosPagoMesas[ordenAVerDetalle!!.id] ?: "Efectivo"
        DialogoVerRecibo(
            orden = ordenAVerDetalle!!,
            menuReal = menuReal,
            metodoUsado = metodoUsado,
            onDismiss = { ordenAVerDetalle = null },
            onImprimir = { generarReciboPDF(context, ordenAVerDetalle!!, menuReal, metodoUsado) }
        )
    }
}

@Composable
fun CardCajaPendiente(
    orden: OrdenBackend,
    isProcessing: Boolean,
    onCobrar: () -> Unit,
    onEditar: () -> Unit,
    onMetodoCambio: (String) -> Unit,
    metodoActual: String
) {
    var expandedBancos by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("MESA ${orden.mesa?.numero ?: "?"}", fontWeight = FontWeight.Black, fontSize = 20.sp, color = Color(0xFF1E1E1E))
                Text("Ticket #${orden.id}", color = Color.Gray, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(color = Color(0xFFF1F8E9), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Total a Pagar:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("C$ ${orden.total}", fontWeight = FontWeight.Black, fontSize = 24.sp, color = Color(0xFF1B6D24))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Canal de Pago Autorizado:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = metodoActual == "Efectivo", onClick = { onMetodoCambio("Efectivo") }, enabled = !isProcessing)
                    Text("Efectivo", fontSize = 14.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = metodoActual.startsWith("Transf."),
                        onClick = {
                            onMetodoCambio("Transf. BAC")
                            expandedBancos = true
                        },
                        enabled = !isProcessing
                    )
                    Text(if (metodoActual.startsWith("Transf.")) metodoActual else "Transferencia", fontSize = 14.sp)
                }
            }

            if (metodoActual.startsWith("Transf.")) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    ElevatedFilterChip(selected = metodoActual == "Transf. BAC", onClick = { onMetodoCambio("Transf. BAC") }, label = { Text("BAC") })
                    ElevatedFilterChip(selected = metodoActual == "Transf. BANPRO", onClick = { onMetodoCambio("Transf. BANPRO") }, label = { Text("BANPRO") })
                    ElevatedFilterChip(selected = metodoActual == "Transf. LAFISE", onClick = { onMetodoCambio("Transf. LAFISE") }, label = { Text("LAFISE") })
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onEditar, modifier = Modifier.weight(1f), enabled = !isProcessing) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Editar")
                }
                Button(
                    onClick = onCobrar,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B6D24)),
                    enabled = !isProcessing
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Cobrar Cuenta", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CardReciboHistorial(orden: OrdenBackend, onVerDetalle: () -> Unit, onImprimir: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA))
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Recibo Mesa ${orden.mesa?.numero}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Ticket #${orden.id} - PAGADO", color = Color(0xFF388E3C), fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
            Text("C$ ${orden.total}", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color(0xFF1E1E1E))
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = onVerDetalle, modifier = Modifier.weight(1f)) { Text("Ver Detalle") }
            TextButton(onClick = onImprimir, modifier = Modifier.weight(1f)) { Text("Generar PDF") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoEditorCaja(
    orden: OrdenBackend,
    menuReal: List<Producto>,
    onDismiss: () -> Unit,
    onGuardar: (Long, String, Double) -> Unit
) {
    var itemsParseados by remember { mutableStateOf<List<ItemCajaParseado>>(emptyList()) }
    var notasGeneralesEdicion by remember { mutableStateOf("") }
    var totalCalculado by remember { mutableDoubleStateOf(0.0) }
    var expandedExtraEdit by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val rawNotas = orden.notas?.replace("\r", "") ?: ""
        var itemsPart = rawNotas
        var genPart = ""

        if (rawNotas.contains("📝 NOTAS GENERALES:")) {
            val parts = rawNotas.split("📝 NOTAS GENERALES:")
            itemsPart = parts[0].trim()
            genPart = parts.getOrNull(1)?.trim() ?: ""
        }

        val lines = itemsPart.split("\n")
        val list = mutableListOf<ItemCajaParseado>()

        for (line in lines) {
            if (line.contains(Regex("\\d+x\\s+"))) {
                val itemsDesglosados = parsearBloqueCajaAislado(line, menuReal)
                list.addAll(itemsDesglosados)
            } else if (line.isNotBlank() && list.isNotEmpty()) {
                val ultimo = list.last()
                list[list.size - 1] = ultimo.copy(bloqueTextoOriginal = ultimo.bloqueTextoOriginal + "\n" + line)
            }
        }

        itemsParseados = list
        notasGeneralesEdicion = genPart
        totalCalculado = orden.total
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.fillMaxWidth(0.95f),
        title = { Text("Ajuste de Comprobante - Mesa ${orden.mesa?.numero}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Elimina unidades específicas de manera individual sin alterar el resto.", fontSize = 12.sp, color = Color.Gray)

                Box(modifier = Modifier.weight(1f, fill = false).heightIn(max = 240.dp)) {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(itemsParseados, key = { it.idUnico }) { item ->
                            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))) {
                                Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("${item.cantidad}x ${item.nombre}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("C$ ${item.precioCalculado}", color = Color(0xFF1B6D24), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    }
                                    IconButton(onClick = {
                                        itemsParseados = itemsParseados.filter { it.idUnico != item.idUnico }
                                        totalCalculado = (totalCalculado - item.precioCalculado).coerceAtLeast(0.0)
                                    }) { Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red) }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider()

                Text("Inyectar Ítem Extraordinario:", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                @Suppress("DEPRECATION")
                ExposedDropdownMenuBox(expanded = expandedExtraEdit, onExpandedChange = { expandedExtraEdit = !expandedExtraEdit }) {
                    OutlinedTextField(
                        value = "Toca para añadir producto...",
                        onValueChange = {}, readOnly = true,
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedExtraEdit) }
                    )
                    ExposedDropdownMenu(expanded = expandedExtraEdit, onDismissRequest = { expandedExtraEdit = false }) {
                        menuReal.forEach { p ->
                            DropdownMenuItem(
                                text = { Text("${p.nombre} (+C$ ${p.precio})") },
                                onClick = {
                                    val newItem = ItemCajaParseado(
                                        idUnico = UUID.randomUUID().toString(),
                                        cantidad = 1,
                                        nombre = p.nombre,
                                        precioCalculado = p.precio,
                                        bloqueTextoOriginal = "✅ YA PEDIDO: 1x ${p.nombre}"
                                    )
                                    itemsParseados = itemsParseados + newItem
                                    totalCalculado += p.precio
                                    expandedExtraEdit = false
                                }
                            )
                        }
                    }
                }

                Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("TOTAL RECALCULADO:", fontWeight = FontWeight.Bold)
                        Text("C$ $totalCalculado", fontWeight = FontWeight.Black, color = Color(0xFF1B6D24), fontSize = 18.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val nuevoTextoNotas = buildString {
                        append(itemsParseados.joinToString("\n") { it.bloqueTextoOriginal })
                        if (notasGeneralesEdicion.isNotBlank()) {
                            append("\n\n📝 NOTAS GENERALES: $notasGeneralesEdicion")
                        }
                    }
                    onGuardar(orden.id, nuevoTextoNotas.trim(), totalCalculado)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B6D24))
            ) { Text("Guardar Ajustes") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun DialogoVerRecibo(orden: OrdenBackend, menuReal: List<Producto>, metodoUsado: String, onDismiss: () -> Unit, onImprimir: () -> Unit) {
    var itemsParseados by remember { mutableStateOf<List<ItemCajaParseado>>(emptyList()) }

    LaunchedEffect(Unit) {
        val rawNotas = orden.notas?.replace("\r", "") ?: ""
        var itemsPart = rawNotas
        if (rawNotas.contains("📝 NOTAS GENERALES:")) { itemsPart = rawNotas.split("📝 NOTAS GENERALES:")[0].trim() }

        val lines = itemsPart.split("\n")
        val list = mutableListOf<ItemCajaParseado>()

        for (line in lines) {
            if (line.contains(Regex("\\d+x\\s+"))) {
                list.addAll(parsearBloqueCajaAislado(line, menuReal))
            }
        }
        itemsParseados = list
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Consulta de Comprobante", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                LazyColumn(modifier = Modifier.heightIn(max = 280.dp)) {
                    items(itemsParseados) { item ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${item.cantidad}x ${item.nombre}", fontSize = 14.sp, modifier = Modifier.weight(1f))
                            Text("C$ ${item.precioCalculado}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        HorizontalDivider(color = Color(0xFFEEEEEE))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("Forma de pago registrada: $metodoUsado", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("TOTAL LIQUIDADO:", fontWeight = FontWeight.Black)
                    Text("C$ ${orden.total}", fontWeight = FontWeight.Black, color = Color(0xFF1B6D24), fontSize = 18.sp)
                }
            }
        },
        confirmButton = {
            Button(onClick = onImprimir, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E1E))) {
                Text("Bajar PDF Oficial")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } }
    )
}

fun parsearBloqueCajaAislado(line: String, menuReal: List<Producto>): List<ItemCajaParseado> {
    val regex = Regex(".*?(\\d+)x\\s+(.*)")
    val match = regex.find(line)
    val desglosados = mutableListOf<ItemCajaParseado>()

    if (match != null) {
        val totalCantidad = match.groupValues[1].toIntOrNull() ?: 1
        val nombre = match.groupValues[2].trim()
        val prod = menuReal.find { it.nombre.equals(nombre, ignoreCase = true) }
        val precioUnitario = prod?.precio ?: 0.0

        for (i in 1..totalCantidad) {
            desglosados.add(
                ItemCajaParseado(
                    idUnico = UUID.randomUUID().toString(),
                    cantidad = 1,
                    nombre = nombre,
                    precioCalculado = precioUnitario,
                    bloqueTextoOriginal = "✅ YA PEDIDO: 1x $nombre"
                )
            )
        }
    }
    return desglosados
}

fun generarReciboPDF(context: Context, orden: OrdenBackend, menuReal: List<Producto>, metodoPago: String) {
    try {
        val pdfDocument = PdfDocument()
        val anchoPagina = 300

        val rawNotas = orden.notas?.replace("\r", "") ?: ""
        var itemsPart = rawNotas
        if (rawNotas.contains("📝 NOTAS GENERALES:")) { itemsPart = rawNotas.split("📝 NOTAS GENERALES:")[0].trim() }

        val lines = itemsPart.split("\n")
        val itemsList = mutableListOf<ItemCajaParseado>()
        for (line in lines) {
            if (line.contains(Regex("\\d+x\\s+"))) {
                itemsList.addAll(parsearBloqueCajaAislado(line, menuReal))
            }
        }

        val altoPagina = 440 + (itemsList.size * 25)
        val pageInfo = PdfDocument.PageInfo.Builder(anchoPagina, altoPagina, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        val logoOriginal = BitmapFactory.decodeResource(context.resources, R.drawable.logo_ternero)
        val logoScaled = android.graphics.Bitmap.createScaledBitmap(logoOriginal, 75, 75, false)
        canvas.drawBitmap(logoScaled, 112f, 20f, paint)

        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 13f
        canvas.drawText("LECHE AGRIA EL TERNERO", anchoPagina / 2f, 120f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 9f
        canvas.drawText("Sabor que enamora...", anchoPagina / 2f, 135f, paint)
        canvas.drawText("RUC: J0310000012345", anchoPagina / 2f, 148f, paint)
        canvas.drawText("Managua, Nicaragua", anchoPagina / 2f, 160f, paint)

        paint.strokeWidth = 1f
        canvas.drawLine(10f, 172f, anchoPagina - 10f, 172f, paint)

        paint.textAlign = Paint.Align.LEFT
        val dateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
        val fechaHora = dateFormat.format(Date())

        canvas.drawText("Emisión: $fechaHora", 12f, 190f, paint)
        canvas.drawText("Ticket ID: #${orden.id}", 12f, 204f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Mesa Asignada: ${orden.mesa?.numero}", 12f, 218f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawLine(10f, 230f, anchoPagina - 10f, 230f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("CT", 12f, 248f, paint)
        canvas.drawText("DETALLE", 45f, 248f, paint)
        canvas.drawText("TOTAL", 242f, 248f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        var yPos = 268f

        for (item in itemsList) {
            canvas.drawText("${item.cantidad}", 14f, yPos, paint)
            var cleanName = item.nombre
            if (cleanName.length > 24) cleanName = cleanName.substring(0, 22) + ".."
            canvas.drawText(cleanName, 45f, yPos, paint)
            canvas.drawText("C$ ${item.precioCalculado}", 242f, yPos, paint)
            yPos += 22f
        }

        canvas.drawLine(10f, yPos + 4f, anchoPagina - 10f, yPos + 4f, paint)

        yPos += 28f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 12f
        canvas.drawText("TOTAL A PAGAR:", 45f, yPos, paint)
        canvas.drawText("C$ ${orden.total}", 215f, yPos, paint)

        yPos += 24f
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("MÉTODO DE PAGO: ${metodoPago.uppercase()}", 45f, yPos, paint)

        yPos += 35f
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 9f
        canvas.drawText("Reg. Simplificado de Cuota Fija", anchoPagina / 2f, yPos, paint)
        canvas.drawText("¡Muchas gracias por elegirnos!", anchoPagina / 2f, yPos + 14f, paint)

        pdfDocument.finishPage(page)

        val nombreArchivo = "Factura_Mesa_${orden.mesa?.numero}_Ticket_${orden.id}.pdf"

        // 🛡️ COMPATIBILIDAD UNIVERSAL: Verifica la versión de Android del celular
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            // Para Android 10+ (Como tu S25 Ultra)
            val resolver = context.contentResolver
            val contentValues = android.content.ContentValues().apply {
                put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, nombreArchivo)
                put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

            if (uri != null) {
                resolver.openOutputStream(uri)?.use { outputStream ->
                    pdfDocument.writeTo(outputStream)
                }
                Toast.makeText(context, "Factura en Descargas: $nombreArchivo", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Error: El sistema rechazó guardar el archivo", Toast.LENGTH_LONG).show()
            }
        } else {
            // Para Android 9 o inferior (Tablets o celulares antiguos)
            val carpetaDescargas = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val archivoFinal = File(carpetaDescargas, nombreArchivo)
            pdfDocument.writeTo(FileOutputStream(archivoFinal))
            Toast.makeText(context, "Factura en Descargas: $nombreArchivo", Toast.LENGTH_LONG).show()
        }

        pdfDocument.close()

    } catch (e: Exception) {
        Toast.makeText(context, "Fallo al procesar PDF: ${e.message}", Toast.LENGTH_LONG).show()
    }
}