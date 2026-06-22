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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.lecheagriaelternero.R
import com.lecheagriaelternero.model.OrdenBackend
import com.lecheagriaelternero.model.Producto
import com.lecheagriaelternero.viewmodel.MenuViewModel
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
    val context = LocalContext.current

    var tabSeleccionado by remember { mutableIntStateOf(0) } // 0: Pendientes, 1: Historial

    val ordenesPendientes = ordenesActivas.filter { it.estado != "PAGADO" }.sortedBy { it.id }
    val ordenesPagadas = ordenesActivas.filter { it.estado == "PAGADO" }.sortedByDescending { it.id }

    var ordenAEditar by remember { mutableStateOf<OrdenBackend?>(null) }
    var ordenAVerDetalle by remember { mutableStateOf<OrdenBackend?>(null) }
    var metodoPagoSeleccionado by remember { mutableStateOf("Efectivo") }

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
                    Text("Historial de Recibos", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
                }
            }

            // CONTENIDO PENDIENTES
            if (tabSeleccionado == 0) {
                if (ordenesPendientes.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay cuentas por cobrar", color = Color.Gray)
                    }
                } else {
                    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(ordenesPendientes) { orden ->
                            CardCajaPendiente(
                                orden = orden,
                                onCobrar = {
                                    viewModel.cobrarMesa(orden.mesa?.id.toString(), metodoPagoSeleccionado)
                                    Toast.makeText(context, "Mesa cobrada con éxito", Toast.LENGTH_SHORT).show()
                                },
                                onEditar = { ordenAEditar = orden },
                                onMetodoCambio = { metodoPagoSeleccionado = it },
                                metodoActual = metodoPagoSeleccionado
                            )
                        }
                    }
                }
            }
            // CONTENIDO HISTORIAL
            else {
                if (ordenesPagadas.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay recibos generados hoy", color = Color.Gray)
                    }
                } else {
                    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(ordenesPagadas) { orden ->
                            CardReciboHistorial(
                                orden = orden,
                                onVerDetalle = { ordenAVerDetalle = orden },
                                onImprimir = { generarReciboPDF(context, orden, menuReal) }
                            )
                        }
                    }
                }
            }
        }
    }

    // DIÁLOGO DE EDICIÓN PROFUNDA (CAJA)
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

    // DIÁLOGO DE VER DETALLE HISTORIAL
    if (ordenAVerDetalle != null) {
        DialogoVerRecibo(
            orden = ordenAVerDetalle!!,
            menuReal = menuReal,
            onDismiss = { ordenAVerDetalle = null },
            onImprimir = { generarReciboPDF(context, ordenAVerDetalle!!, menuReal) }
        )
    }
}

@Composable
fun CardCajaPendiente(
    orden: OrdenBackend,
    onCobrar: () -> Unit,
    onEditar: () -> Unit,
    onMetodoCambio: (String) -> Unit,
    metodoActual: String
) {
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

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = metodoActual == "Efectivo", onClick = { onMetodoCambio("Efectivo") })
                    Text("Efectivo")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = metodoActual == "Tarjeta", onClick = { onMetodoCambio("Tarjeta") })
                    Text("Tarjeta")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onEditar, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Editar")
                }
                Button(onClick = onCobrar, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B6D24))) {
                    Text("Cobrar", fontWeight = FontWeight.Bold)
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
        var currentBlock = mutableListOf<String>()

        for (line in lines) {
            if (line.contains(Regex("\\d+x\\s+"))) {
                if (currentBlock.isNotEmpty()) {
                    parsearBloqueCaja(currentBlock, menuReal)?.let { list.add(it) }
                }
                currentBlock = mutableListOf(line)
            } else if (line.isNotBlank()) {
                currentBlock.add(line)
            }
        }
        if (currentBlock.isNotEmpty()) {
            parsearBloqueCaja(currentBlock, menuReal)?.let { list.add(it) }
        }

        itemsParseados = list
        notasGeneralesEdicion = genPart
        totalCalculado = orden.total
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.fillMaxWidth(0.95f),
        title = { Text("Modificar Cuenta - Mesa ${orden.mesa?.numero}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.weight(1f, fill = false).heightIn(max = 200.dp)) {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(itemsParseados) { item ->
                            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))) {
                                Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("${item.cantidad}x ${item.nombre}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("C$ ${item.precioCalculado}", color = Color(0xFF1B6D24), fontSize = 12.sp)
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

                Text("Añadir Producto Olvidado:", fontWeight = FontWeight.Bold, fontSize = 14.sp)

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
fun DialogoVerRecibo(orden: OrdenBackend, menuReal: List<Producto>, onDismiss: () -> Unit, onImprimir: () -> Unit) {
    var itemsParseados by remember { mutableStateOf<List<ItemCajaParseado>>(emptyList()) }

    LaunchedEffect(Unit) {
        val rawNotas = orden.notas?.replace("\r", "") ?: ""
        var itemsPart = rawNotas
        if (rawNotas.contains("📝 NOTAS GENERALES:")) { itemsPart = rawNotas.split("📝 NOTAS GENERALES:")[0].trim() }

        val lines = itemsPart.split("\n")
        val list = mutableListOf<ItemCajaParseado>()
        var currentBlock = mutableListOf<String>()

        for (line in lines) {
            if (line.contains(Regex("\\d+x\\s+"))) {
                if (currentBlock.isNotEmpty()) { parsearBloqueCaja(currentBlock, menuReal)?.let { list.add(it) } }
                currentBlock = mutableListOf(line)
            } else if (line.isNotBlank()) { currentBlock.add(line) }
        }
        if (currentBlock.isNotEmpty()) { parsearBloqueCaja(currentBlock, menuReal)?.let { list.add(it) } }
        itemsParseados = list
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Detalle de Cuenta", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                    items(itemsParseados) { item ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${item.cantidad}x ${item.nombre}", fontSize = 14.sp, modifier = Modifier.weight(1f))
                            Text("C$ ${item.precioCalculado}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        HorizontalDivider(color = Color(0xFFEEEEEE))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("TOTAL COBRADO:", fontWeight = FontWeight.Black)
                    Text("C$ ${orden.total}", fontWeight = FontWeight.Black, color = Color(0xFF1B6D24), fontSize = 18.sp)
                }
            }
        },
        confirmButton = {
            Button(onClick = { onImprimir(); onDismiss() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E1E))) {
                Text("Descargar PDF")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } }
    )
}

fun parsearBloqueCaja(lines: List<String>, menuReal: List<Producto>): ItemCajaParseado? {
    val firstLine = lines.first()
    val regex = Regex(".*?(\\d+)x\\s+(.*)")
    val match = regex.find(firstLine)

    if (match != null) {
        val cant = match.groupValues[1].toIntOrNull() ?: 1
        val nombre = match.groupValues[2].trim()
        val prod = menuReal.find { it.nombre.equals(nombre, ignoreCase = true) }
        val precioItem = if (prod != null) prod.precio * cant else 0.0

        return ItemCajaParseado(
            idUnico = UUID.randomUUID().toString(),
            cantidad = cant,
            nombre = nombre,
            precioCalculado = precioItem,
            bloqueTextoOriginal = lines.joinToString("\n")
        )
    }
    return null
}

// 🖨️ MOTOR GENERADOR DE PDF PROFESIONAL TIPO TICKET THERMAL
fun generarReciboPDF(context: Context, orden: OrdenBackend, menuReal: List<Producto>) {
    try {
        val pdfDocument = PdfDocument()
        val anchoPagina = 300 // Formato ticket 80mm

        // Parseo rápido para calcular altura dinámica del ticket
        val rawNotas = orden.notas?.replace("\r", "") ?: ""
        var itemsPart = rawNotas
        if (rawNotas.contains("📝 NOTAS GENERALES:")) { itemsPart = rawNotas.split("📝 NOTAS GENERALES:")[0].trim() }
        val lines = itemsPart.split("\n")
        val itemsList = mutableListOf<ItemCajaParseado>()
        var currentBlock = mutableListOf<String>()
        for (line in lines) {
            if (line.contains(Regex("\\d+x\\s+"))) {
                if (currentBlock.isNotEmpty()) { parsearBloqueCaja(currentBlock, menuReal)?.let { itemsList.add(it) } }
                currentBlock = mutableListOf(line)
            } else if (line.isNotBlank()) { currentBlock.add(line) }
        }
        if (currentBlock.isNotEmpty()) { parsearBloqueCaja(currentBlock, menuReal)?.let { itemsList.add(it) } }

        val altoPagina = 400 + (itemsList.size * 30) // Altura dinámica según items
        val pageInfo = PdfDocument.PageInfo.Builder(anchoPagina, altoPagina, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        // Dibujar Logo
        val logoOriginal = BitmapFactory.decodeResource(context.resources, R.drawable.logo_ternero)
        val logoScaled = android.graphics.Bitmap.createScaledBitmap(logoOriginal, 80, 80, false)
        canvas.drawBitmap(logoScaled, 110f, 20f, paint)

        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 14f
        canvas.drawText("LECHE AGRIA EL TERNERO", anchoPagina / 2f, 130f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 10f
        canvas.drawText("Sabor que enamora...", anchoPagina / 2f, 145f, paint)
        canvas.drawText("Managua, Nicaragua", anchoPagina / 2f, 160f, paint)

        // Trazador de líneas
        paint.strokeWidth = 1f
        canvas.drawLine(10f, 175f, anchoPagina - 10f, 175f, paint)

        // Metadatos
        paint.textAlign = Paint.Align.LEFT
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val fechaHora = dateFormat.format(Date())
        canvas.drawText("Fecha: $fechaHora", 10f, 195f, paint)
        canvas.drawText("Ticket #: ${orden.id}   Mesa: ${orden.mesa?.numero}", 10f, 210f, paint)

        canvas.drawLine(10f, 225f, anchoPagina - 10f, 225f, paint)

        // Encabezados de tabla
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("CANT", 10f, 245f, paint)
        canvas.drawText("DESCRIPCIÓN", 50f, 245f, paint)
        canvas.drawText("IMPORTE", 240f, 245f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        var yPosition = 265f

        // Dibujar Items
        for (item in itemsList) {
            canvas.drawText("${item.cantidad}", 15f, yPosition, paint)

            // Recorte de nombre si es muy largo
            var nombreCorto = item.nombre
            if (nombreCorto.length > 25) nombreCorto = nombreCorto.substring(0, 23) + ".."
            canvas.drawText(nombreCorto, 50f, yPosition, paint)

            canvas.drawText("C$ ${item.precioCalculado}", 240f, yPosition, paint)
            yPosition += 20f
        }

        canvas.drawLine(10f, yPosition + 10f, anchoPagina - 10f, yPosition + 10f, paint)

        // Totales
        yPosition += 35f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 14f
        canvas.drawText("TOTAL:", 100f, yPosition, paint)
        canvas.drawText("C$ ${orden.total}", 200f, yPosition, paint)

        // Footer
        yPosition += 40f
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 10f
        canvas.drawText("¡Gracias por su preferencia!", anchoPagina / 2f, yPosition, paint)
        canvas.drawText("Vuelva pronto.", anchoPagina / 2f, yPosition + 15f, paint)

        pdfDocument.finishPage(page)

        // Guardado directo en Descargas (Compatible con API 29+)
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, "Recibo_Mesa_${orden.mesa?.numero}_Ticket_${orden.id}.pdf")

        pdfDocument.writeTo(FileOutputStream(file))
        pdfDocument.close()

        Toast.makeText(context, "PDF Guardado en Descargas", Toast.LENGTH_LONG).show()

    } catch (e: Exception) {
        Toast.makeText(context, "Error generando PDF: ${e.message}", Toast.LENGTH_LONG).show()
    }
}