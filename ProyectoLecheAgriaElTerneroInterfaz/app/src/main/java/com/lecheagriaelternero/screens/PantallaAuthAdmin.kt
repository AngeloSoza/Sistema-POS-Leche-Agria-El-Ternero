package com.lecheagriaelternero.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAuthAdmin(navController: NavController) {
    var pin by remember { mutableStateOf("") }
    val context = LocalContext.current
    val pinCorrecto = "2026"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Acceso Restringido", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF5F5F5))
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(shape = CircleShape, color = Color(0xFFFFF3E0), modifier = Modifier.size(100.dp)) {
                Icon(Icons.Default.Lock, contentDescription = "Seguridad", tint = Color(0xFFE65100), modifier = Modifier.padding(24.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Panel de Administración", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color(0xFF1E1E1E))
            Text("Ingrese su PIN para acceder a las finanzas", color = Color.Gray, modifier = Modifier.padding(top = 8.dp, bottom = 32.dp))

            OutlinedTextField(
                value = pin,
                onValueChange = { if (it.length <= 4) pin = it },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontSize = 32.sp, letterSpacing = 8.sp),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.width(200.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF1B6D24), unfocusedBorderColor = Color.LightGray)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (pin == pinCorrecto) {
                        pin = ""
                        navController.navigate("admin_dashboard") {
                            popUpTo("auth_admin") { inclusive = true }
                        }
                    } else {
                        Toast.makeText(context, "PIN Incorrecto", Toast.LENGTH_SHORT).show()
                        pin = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(0.6f).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B6D24))
            ) {
                Text("Desbloquear", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}