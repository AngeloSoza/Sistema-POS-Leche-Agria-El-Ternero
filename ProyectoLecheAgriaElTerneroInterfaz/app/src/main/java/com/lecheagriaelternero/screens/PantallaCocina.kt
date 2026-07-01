package com.lecheagriaelternero.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.lecheagriaelternero.model.OrdenBackend
import com.lecheagriaelternero.viewmodel.MenuViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCocina(navController: NavController, viewModel: MenuViewModel) {
    val ordenesActivas by viewModel.ordenesActivas.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        while(true) {
            viewModel.cargarOrdenes()
            delay(1500)
        }
    }

    var filtroActual by remember { mutableStateOf("PENDIENTE") }

    val ordenesFiltradas = ordenesActivas.filter {
        if (filtroActual == "TODAS") true else it.estado == filtroActual
    }.sortedBy { it.id }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Monitor de Cocina", fontWeight = FontWeight.Bold, color = Color(0xFF1E1E1E), fontSize = 20.sp)
                        Text("Sincronizado en tiempo real", fontSize = 12.sp, color = Color.Gray)
                    }
                },

                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF5F5F5)),
                actions = {
                    IconButton(onClick = { viewModel.cargarOrdenes() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refrescar", tint = Color(0xFF1E1E1E))
                    }
                }
            )
        },

        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            ScrollableTabRow(
                selectedTabIndex = if (filtroActual == "PENDIENTE") 0 else if (filtroActual == "ENTREGADO") 1 else 2,
                containerColor = Color.White,
                contentColor = Color(0xFF1E1E1E),
                indicator = { tabPositions ->
                    val colorVerdeAdmin = Color(0xFF1B6D24)
                    if (filtroActual == "PENDIENTE") TabRowDefaults.Indicator(Modifier.tabIndicatorOffset(tabPositions[0]), color = colorVerdeAdmin)
                    else if (filtroActual == "ENTREGADO") TabRowDefaults.Indicator(Modifier.tabIndicatorOffset(tabPositions[1]), color = colorVerdeAdmin)
                    else TabRowDefaults.Indicator(Modifier.tabIndicatorOffset(tabPositions[2]), color = colorVerdeAdmin)
                },
                edgePadding = 0.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(selected = filtroActual == "PENDIENTE", onClick = { filtroActual = "PENDIENTE" }) {
                    Text("POR PREPARAR", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.SemiBold)
                }
                Tab(selected = filtroActual == "ENTREGADO", onClick = { filtroActual = "ENTREGADO" }) {
                    Text("ENTREGADAS", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.SemiBold)
                }
                Tab(selected = filtroActual == "TODAS", onClick = { filtroActual = "TODAS" }) {
                    Text("TODAS", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.SemiBold)
                }
            }

            if (ordenesFiltradas.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No hay órdenes en esta categoría", color = Color.Gray, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(ordenesFiltradas, key = { it.id }) { orden ->
                        OrdenCocinaCard(orden = orden, viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun OrdenCocinaCard(orden: OrdenBackend, viewModel: MenuViewModel) {
    val isEntregada = orden.estado == "ENTREGADO"

    val colorFondo = if (isEntregada) Color(0xFFF1F8E9) else Color.White
    val colorVerdeAdmin = Color(0xFF1B6D24)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = colorFondo),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("MESA ${orden.mesa?.numero ?: "N/A"}", fontSize = 26.sp, fontWeight = FontWeight.Black, color = Color(0xFF1E1E1E))
                    Text("Ticket #${orden.id}", fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                }

                Surface(
                    color = if (isEntregada) colorVerdeAdmin else Color(0xFFE65100),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = orden.estado,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }

            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 16.dp))

            Surface(
                color = Color(0xFFF9F9F9),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val notasTexto = orden.notas ?: ""
                    val lineas = notasTexto.split("\n")

                    lineas.forEach { linea ->
                        val trimmed = linea.trim()
                        if (trimmed.isNotBlank()) {
                            if (trimmed.contains("--- ADICIÓN SOLICITADA ---") || trimmed.contains("--- ADICIÓN ---")) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
                                    Text(" ADICIONES ", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.Gray)
                                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            } else {
                                Row(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    val isNuevo = trimmed.contains("⭐ [NUEVO]") || trimmed.contains("🔴 NUEVO:")
                                    val isEntregado = trimmed.contains("✅ [ENTREGADO]") || trimmed.contains("✅ YA PEDIDO:") || trimmed.contains("✅ ENTREGADO:") || trimmed.contains("✓")
                                    val isHeader = trimmed.startsWith("- ")
                                    val isNotaGral = trimmed.startsWith("📝")

                                    when {
                                        isNuevo && !isEntregada -> {
                                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.size(20.dp).padding(top = 2.dp))
                                            Spacer(modifier = Modifier.width(10.dp))
                                            val texto = trimmed.replace("⭐ [NUEVO]", "").replace("🔴 NUEVO:", "").trim()
                                            Text(texto, fontWeight = FontWeight.Black, fontSize = 17.sp, color = Color(0xFFD32F2F))
                                        }
                                        isEntregado || isEntregada -> {
                                            val colorVerdeAdmin = Color(0xFF1B6D24)
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = colorVerdeAdmin, modifier = Modifier.size(20.dp).padding(top = 2.dp))
                                            Spacer(modifier = Modifier.width(10.dp))
                                            val texto = trimmed
                                                .replace("✅ [ENTREGADO]", "")
                                                .replace("⭐ [NUEVO]", "")
                                                .replace("🔴 NUEVO:", "")
                                                .replace("✅ YA PEDIDO:", "")
                                                .replace("✅ ENTREGADO:", "")
                                                .replace("✓", "")
                                                .trim()
                                            Text(
                                                text = texto,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = colorVerdeAdmin
                                            )
                                        }
                                        isHeader -> {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = if (isEntregada) Color(0xFF1B6D24) else Color(0xFFD32F2F), modifier = Modifier.size(20.dp).padding(top = 2.dp))
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(trimmed.substring(2), fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFF1E1E1E))
                                        }
                                        isNotaGral -> {
                                            Text(trimmed, fontSize = 14.sp, color = Color(0xFFE65100), fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                                        }
                                        else -> {
                                            Spacer(modifier = Modifier.width(30.dp))
                                            Text(trimmed, fontSize = 15.sp, color = Color(0xFF616161), fontWeight = FontWeight.Medium)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (!isEntregada) {
                Spacer(modifier = Modifier.height(18.dp))
                Button(
                    onClick = { viewModel.cambiarEstadoOrden(orden.id, "ENTREGADO") },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colorVerdeAdmin)
                ) {
                    Text("MARCAR COMO LISTA ✅", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 1.sp)
                }
            }
        }
    }
}