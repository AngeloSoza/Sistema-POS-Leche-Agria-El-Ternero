package com.lecheagriaelternero.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun PantallaRoles(navController: NavController) {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Selecciona tu Área",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF1E1E1E)
            )

            Spacer(modifier = Modifier.height(40.dp))

            TarjetaModulo(
                icono = "📝",
                colorFondoIcono = Color(0xFFE8F5E9),
                titulo = "Meseros",
                subtitulo = "Tomar órdenes y mesas"
            ) { navController.navigate("mesas") }

            Spacer(modifier = Modifier.height(16.dp))

            TarjetaModulo(
                icono = "🍳",
                colorFondoIcono = Color(0xFFFFF3E0),
                titulo = "Cocina (KDS)",
                subtitulo = "Monitor de preparación"
            ) { navController.navigate("cocina") }

            Spacer(modifier = Modifier.height(16.dp))

            TarjetaModulo(
                icono = "💰",
                colorFondoIcono = Color(0xFFE3F2FD),
                titulo = "Caja Principal",
                subtitulo = "Cobros y facturación"
            ) { navController.navigate("caja") }

            Spacer(modifier = Modifier.height(16.dp))

            TarjetaModulo(
                icono = "📊",
                colorFondoIcono = Color(0xFFF3E5F5),
                titulo = "Administración",
                subtitulo = "Dashboard e Inventario"
            ) { navController.navigate("auth_admin") }
        }
    }
}

@Composable
fun TarjetaModulo(icono: String, colorFondoIcono: Color, titulo: String, subtitulo: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = colorFondoIcono,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(icono, fontSize = 32.sp)
                }
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(titulo, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E1E))
                Text(subtitulo, fontSize = 14.sp, color = Color.Gray)
            }
        }
    }
}