package com.lecheagriaelternero.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
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

    // SISTEMA DE REFRESCADO AUTOMÁTICO ULTRARRÁPIDO
    LaunchedEffect(Unit) {
        while(true) {
            viewModel.cargarOrdenes()
            delay(1500)
        }
    }

    // FILTRO INTERACTIVO DE COCINA
    var filtroActual by remember { mutableStateOf("PENDIENTE") }

    val ordenesFiltradas = ordenesActivas.filter {
        if (filtroActual == "TODAS") true else it.estado == filtroActual
    }.sortedBy { it.id }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Monitor de Cocina (KDS)", fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Sincronizado en tiempo real", fontSize = 12.sp, color = Color.LightGray)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E1E)),
                actions = {
                    IconButton(onClick = { viewModel.cargarOrdenes() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refrescar", tint = Color.White)
                    }
                }
            )
        },
        containerColor = Color(0xFF121212)
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            // BARRA DE FILTROS
            ScrollableTabRow(
                selectedTabIndex = if (filtroActual == "PENDIENTE") 0 else if (filtroActual == "ENTREGADO") 1 else 2,
                containerColor = Color(0xFF1E1E1E),
                contentColor = Color.White,
                edgePadding = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(selected = filtroActual == "PENDIENTE", onClick = { filtroActual = "PENDIENTE" }) {
                    Text("POR PREPARAR", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
                }
                Tab(selected = filtroActual == "ENTREGADO", onClick = { filtroActual = "ENTREGADO" }) {
                    Text("ENTREGADAS", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
                }
                Tab(selected = filtroActual == "TODAS", onClick = { filtroActual = "TODAS" }) {
                    Text("TODAS", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
                }
            }

            if (ordenesFiltradas.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay órdenes en esta categoría", color = Color.Gray, fontSize = 18.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(ordenesFiltradas) { orden ->
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
    val colorFondo = if (isEntregada) Color(0xFF388E3C) else Color(0xFF1B6D24)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colorFondo),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("MESA ${orden.mesa?.numero ?: "N/A"}", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)
                    Text("Ticket #${orden.id}", fontSize = 14.sp, color = Color(0xFFA5D6A7))
                }

                Surface(
                    color = if (isEntregada) Color(0xFF66BB6A) else Color(0xFFD32F2F),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        orden.estado,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            HorizontalDivider(color = Color(0xFF4CAF50), modifier = Modifier.padding(vertical = 12.dp))

            Surface(
                color = Color.White,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val notasTexto = orden.notas ?: ""
                    val lineas = notasTexto.split("\n")

                    lineas.forEach { linea ->
                        if (linea.isNotBlank()) {
                            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                                if (linea.startsWith("- ")) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF1B6D24), modifier = Modifier.size(20.dp).padding(top = 2.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(linea.substring(2), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1E1E1E))
                                } else if (linea.startsWith("📝")) {
                                    Text(linea, fontSize = 16.sp, color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                                } else {
                                    Spacer(modifier = Modifier.width(28.dp))
                                    Text(linea, fontSize = 16.sp, color = Color.DarkGray)
                                }
                            }
                        }
                    }
                }
            }

            if (!isEntregada) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.cambiarEstadoOrden(orden.id, "ENTREGADO") },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E1E))
                ) {
                    Text("ORDEN LISTA PARA ENTREGAR ✅", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White)
                }
            }
        }
    }
}