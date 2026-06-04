package com.lecheagriaelternero.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay

@Composable
fun PantallaSplash(navController: NavController) {
    LaunchedEffect(Unit) {
        delay(3000L) // Espera 3 segundos
        navController.navigate("roles") {
            popUpTo("splash") { inclusive = true }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)), // Fondo oscuro como tu imagen
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Bienvenido al Sistema de Gestión\nde Clientes de Leche Agria\n\"El Ternero\"",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        Box(
            modifier = Modifier
                .size(150.dp)
                .background(Color.DarkGray, shape = androidx.compose.foundation.shape.CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("LOGO", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
        }

        Spacer(modifier = Modifier.height(64.dp))

        CircularProgressIndicator(color = Color(0xFFE65100)) // Bolita naranja

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Analizando y actualizando datos...",
            fontSize = 14.sp,
            color = Color.LightGray
        )
    }
}