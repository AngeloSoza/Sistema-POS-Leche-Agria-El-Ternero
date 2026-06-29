package com.lecheagriaelternero.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.lecheagriaelternero.R
import com.lecheagriaelternero.viewmodel.MenuViewModel

// UI del Splash Screen Mejorada y Diseñada por Ervin Pérez
@Composable
fun PantallaSplash(navController: NavController, viewModel: MenuViewModel) {
    // 🛡️ ESCUDO ACTIVADO: Escuchamos a la base de datos en tiempo real
    val estaCargando by viewModel.estaCargando.collectAsStateWithLifecycle()

    LaunchedEffect(estaCargando) {
        // Solo avanzamos a "roles" cuando la base de datos dice: "Terminé de descargar todo al 100%"
        if (!estaCargando) {
            navController.navigate("roles") {
                popUpTo("splash") { inclusive = true }
            }
        }
    }

    // MEJORA UI: Fondo con gradiente sutil para darle más profundidad que un color sólido
    val fondoGradiente = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1E1E1E), // Gris oscuro arriba
            Color(0xFF050505)  // Casi negro puro abajo
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(fondoGradiente),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(0.9f) // MEJORA UI: Un poco más ancho para que el texto respire mejor
        ) {

            // MEJORA UI: Título dividido para darle énfasis al nombre del negocio
            Text(
                text = "Sistema de Ventas y Pedidos",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFB0BEC5), // Gris azulado claro
                textAlign = TextAlign.Center,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "LECHE AGRIA\n\"EL TERNERO\"",
                fontSize = 34.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 38.sp,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // MEJORA UI: Surface detrás del logo para darle un brillo/sombra sutil (Efecto premium)
            Surface(
                modifier = Modifier.size(270.dp),
                shape = CircleShape,
                color = Color.Transparent,
                shadowElevation = 12.dp // Sombra suave para despegar el logo del fondo
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.03f)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_ternero),
                        contentDescription = "Logo El Ternero",
                        modifier = Modifier
                            .size(260.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Spacer(modifier = Modifier.height(56.dp))

            // MEJORA UI: Indicador de progreso con color de riel (track) para que se vea más moderno
            CircularProgressIndicator(
                color = Color(0xFFFF9800), // Naranja vibrante
                trackColor = Color(0xFF333333), // Riel gris oscuro de fondo
                strokeWidth = 5.dp,
                modifier = Modifier.size(56.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Sabor que enamora...",
                fontSize = 18.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                color = Color(0xFFFFB74D), // Naranja un poco más suave para hacer contraste
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Texto dinámico que justifica la espera
            Text(
                text = "Sincronizando con Base de Datos segura...",
                fontSize = 13.sp,
                color = Color(0xFF888888), // Gris intermedio, no llama tanto la atención
                textAlign = TextAlign.Center,
                letterSpacing = 0.5.sp
            )
        }
    }
}