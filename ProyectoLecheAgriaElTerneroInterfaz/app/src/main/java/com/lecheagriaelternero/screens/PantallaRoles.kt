package com.lecheagriaelternero.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border // 🛡️ IMPORTACIÓN AÑADIDA AQUÍ
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun PantallaRoles(navController: NavController) {
    val bgApp = Color(0xFFF4F6F8) // Gris súper claro y moderno
    val greenGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF2E7D32), // Verde esmeralda oscuro
            Color(0xFF1B5E20)  // Verde bosque profundo
        )
    )

    Scaffold(
        containerColor = bgApp
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Cabecera curva/degradada al fondo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(greenGradient)
            )

            // Contenido escroleable encima del fondo
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(56.dp))

                // Etiqueta de la marca en la cabecera
                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "LECHE AGRIA EL TERNERO",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Selecciona tu Área",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )

                Text(
                    text = "Por favor, elige tu módulo para iniciar el turno",
                    fontSize = 15.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Contenedor de las tarjetas (hace el efecto de solapamiento)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TarjetaModulo(
                        icono = "📝",
                        colorPrincipal = Color(0xFF4CAF50), // Verde
                        titulo = "Meseros",
                        subtitulo = "Tomar órdenes y gestionar mesas"
                    ) { navController.navigate("mesas") }

                    TarjetaModulo(
                        icono = "🍳",
                        colorPrincipal = Color(0xFFFF9800), // Naranja
                        titulo = "Cocina (KDS)",
                        subtitulo = "Monitor de preparación de platillos"
                    ) { navController.navigate("cocina") }

                    TarjetaModulo(
                        icono = "💰",
                        colorPrincipal = Color(0xFF2196F3), // Azul
                        titulo = "Caja Principal",
                        subtitulo = "Cobros, facturación y recibos"
                    ) { navController.navigate("caja") }

                    TarjetaModulo(
                        icono = "📊",
                        colorPrincipal = Color(0xFF9C27B0), // Morado
                        titulo = "Administración",
                        subtitulo = "Dashboard, arqueos e inventario"
                    ) { navController.navigate("auth_admin") }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun TarjetaModulo(
    icono: String,
    colorPrincipal: Color,
    titulo: String,
    subtitulo: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp) // Sombra pronunciada para dar efecto flotante
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Diseño de icono concéntrico (Capa externa clara, capa interna brillante)
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(colorPrincipal.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.White, CircleShape)
                        .padding(4.dp), // Borde blanco interno
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(colorPrincipal.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(icono, fontSize = 24.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Textos
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1A1C1E)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitulo,
                    fontSize = 13.sp,
                    color = Color(0xFF6C757D),
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Botón indicador interactivo
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFF8F9FA), CircleShape)
                    .border(1.dp, Color(0xFFE5E7EB), CircleShape), // Ya no dará error
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = "Entrar",
                    tint = colorPrincipal, // La flecha hereda el color del módulo
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}