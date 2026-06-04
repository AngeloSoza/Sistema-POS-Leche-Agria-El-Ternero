package com.lecheagriaelternero.screens

import android.content.ContentValues
import android.content.Context
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.lecheagriaelternero.model.OrdenBackend
import com.lecheagriaelternero.viewmodel.MenuViewModel
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCaja(navController: NavController, viewModel: MenuViewModel) {
    val ordenesActivas by viewModel.ordenesActivas.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.cargarOrdenes()
    }

    var tabSeleccionado by remember { mutableIntStateOf(0) }

    val ordenesCaja = ordenesActivas.filter { it.estado != "PAGADO" }
    val ordenesPagadas = ordenesActivas.filter { it.estado == "PAGADO" }

    var ordenSeleccionada by remember { mutableStateOf<OrdenBackend?>(null) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Caja Principal", fontWeight = FontWeight.Black) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E1E), titleContentColor = Color.White)
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            TabRow(
                selectedTabIndex = tabSeleccionado,
                containerColor = Color.White,
                contentColor = Color(0xFF1B6D24)
            ) {
                Tab(
                    selected = tabSeleccionado == 0,
                    onClick = { tabSeleccionado = 0; ordenSeleccionada = null },
                    text = { Text("Pendientes de Cobro", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = tabSeleccionado == 1,
                    onClick = { tabSeleccionado = 1; ordenSeleccionada = null },
                    text = { Text("Historial de Recibos", fontWeight = FontWeight.Bold) }
                )
            }

            if (tabSeleccionado == 0) {
                if (ordenesCaja.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No hay cuentas pendientes por cobrar.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(ordenesCaja) { orden ->
                            MesaCobroItem(orden, seleccionada = ordenSeleccionada?.id == orden.id) {
                                ordenSeleccionada = orden
                            }
                        }
                    }
                }

                ordenSeleccionada?.let { orden ->
                    PanelCobro(orden = orden, viewModel = viewModel, context = context) {
                        ordenSeleccionada = null
                    }
                }
            } else {
                if (ordenesPagadas.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No hay órdenes pagadas en la base de datos.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(ordenesPagadas.reversed()) { orden ->
                            ReciboItem(orden = orden, context = context)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MesaCobroItem(orden: OrdenBackend, seleccionada: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = if (seleccionada) Color(0xFFF1F8F1) else Color.White),
        border = BorderStroke(1.dp, if (seleccionada) Color(0xFF1B6D24) else Color(0xFFEEEEEE)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(color = Color(0xFF1E1E1E), shape = RoundedCornerShape(8.dp), modifier = Modifier.size(48.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(orden.mesa?.numero?.toString() ?: "-", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Mesa ${orden.mesa?.numero?.toString() ?: "N/A"}", fontWeight = FontWeight.Bold)
                Text(orden.estado, fontSize = 12.sp, color = Color.Gray)
            }
            Text("C$ ${orden.total}", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color(0xFF1B6D24))
        }
    }
}

@Composable
fun ReciboItem(orden: OrdenBackend, context: Context) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(8.dp), modifier = Modifier.size(48.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Receipt, contentDescription = "Recibo", tint = Color(0xFF1B6D24))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Ticket #${orden.id} - Mesa ${orden.mesa?.numero ?: "N/A"}", fontWeight = FontWeight.Bold)
                Text("Total pagado: C$ ${orden.total}", fontSize = 14.sp, color = Color(0xFF1B6D24), fontWeight = FontWeight.Black)
            }
            IconButton(onClick = {
                generarPDF(context, orden)
                Toast.makeText(context, "PDF guardado en la carpeta de Descargas de tu teléfono", Toast.LENGTH_SHORT).show()
            }) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = "Descargar PDF", tint = Color(0xFFD32F2F))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanelCobro(orden: OrdenBackend, viewModel: MenuViewModel, context: Context, onCobroCompletado: () -> Unit) {
    var metodoPago by remember { mutableStateOf("Efectivo") }
    var banco by remember { mutableStateOf("") }
    var expandedBanco by remember { mutableStateOf(false) }
    val bancos = listOf("BAC", "Banpro", "Ficohsa", "Lafise")

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        shadowElevation = 16.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Total a Cobrar", color = Color.Gray, fontWeight = FontWeight.Medium)
                Text("C$ ${orden.total}", fontSize = 32.sp, fontWeight = FontWeight.Black, color = Color(0xFF1E1E1E))
            }
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { metodoPago = "Efectivo" }, colors = ButtonDefaults.buttonColors(containerColor = if (metodoPago == "Efectivo") Color.Black else Color.LightGray), modifier = Modifier.weight(1f)) { Text("Efectivo") }
                Button(onClick = { metodoPago = "Transferencia" }, colors = ButtonDefaults.buttonColors(containerColor = if (metodoPago == "Transferencia") Color.Black else Color.LightGray), modifier = Modifier.weight(1f)) { Text("Transferencia") }
            }

            if (metodoPago == "Transferencia") {
                Spacer(modifier = Modifier.height(12.dp))
                ExposedDropdownMenuBox(expanded = expandedBanco, onExpandedChange = { expandedBanco = !expandedBanco }) {
                    OutlinedTextField(
                        value = banco, onValueChange = {}, readOnly = true, label = { Text("Banco Destino (Obligatorio)") },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expandedBanco, onDismissRequest = { expandedBanco = false }) {
                        bancos.forEach { b ->
                            DropdownMenuItem(text = { Text(b) }, onClick = { banco = b; expandedBanco = false })
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = {
                    if (metodoPago == "Transferencia" && banco.isEmpty()) {
                        Toast.makeText(context, "Debe seleccionar un banco", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    generarPDF(context, orden)
                    viewModel.cambiarEstadoOrden(orden.id, "PAGADO")

                    Toast.makeText(context, "Cuenta cobrada. Factura generada con éxito.", Toast.LENGTH_LONG).show()
                    onCobroCompletado()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B6D24))
            ) { Text("COBRAR E IMPRIMIR CUENTA", fontWeight = FontWeight.Black) }
        }
    }

}

fun generarPDF(context: Context, orden: OrdenBackend) {
    val document = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(300, 600, 1).create()
    val page = document.startPage(pageInfo)
    val canvas = page.canvas
    val paint = android.graphics.Paint()

    val numMesa = orden.mesa?.numero?.toString() ?: "N/A"
    val nombreArchivo = "Factura_Mesa_${numMesa}_${System.currentTimeMillis()}.pdf"

    paint.textSize = 14f
    canvas.drawText("LECHE AGRIA EL TERNERO", 50f, 40f, paint)
    paint.textSize = 10f
    canvas.drawText("Mesa: $numMesa - Ticket #${orden.id}", 20f, 70f, paint)

    var y = 100f

    val detalle = orden.notas ?: "Orden"
    val lineas = detalle.split("\n")

    for (linea in lineas) {
        if (linea.isNotBlank()) {
            canvas.drawText(linea, 20f, y, paint)
            y += 20f
        }
    }

    y += 20f
    canvas.drawText("TOTAL PAGADO: C$${orden.total}", 20f, y, paint)

    document.finishPage(page)

    try {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, nombreArchivo)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                resolver.openOutputStream(uri)?.use { outputStream ->
                    document.writeTo(outputStream)
                }
            }
        } else {

            val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(path, nombreArchivo)
            document.writeTo(FileOutputStream(file))
        }
        document.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}