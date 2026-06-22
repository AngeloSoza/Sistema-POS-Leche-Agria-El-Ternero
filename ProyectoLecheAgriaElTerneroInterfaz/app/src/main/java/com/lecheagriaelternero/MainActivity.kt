package com.lecheagriaelternero

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lecheagriaelternero.screens.*
import com.lecheagriaelternero.ui.theme.LecheAgriaElTerneroTheme
import com.lecheagriaelternero.viewmodel.MenuViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MenuViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LecheAgriaElTerneroTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "splash") {
                        composable("splash") { PantallaSplash(navController, viewModel) }
                        composable("roles") { PantallaRoles(navController) }
                        composable("mesas") { PantallaMesas(navController, viewModel) }
                        composable("toma_orden") { PantallaTomaOrden(navController, viewModel) }
                        composable("carrito") { PantallaCarrito(navController, viewModel) }
                        composable("cocina") { PantallaCocina(navController, viewModel) }
                        composable("caja") { PantallaCaja(navController, viewModel) }
                        composable("dashboard") { DashboardScreen(navController, viewModel) } // Dashboard original si lo usas
                        composable("inventario") { PantallaInventario(navController, viewModel) } // Inventario original

                        // 🛡️ RUTAS DEL NUEVO MÓDULO DE ADMINISTRACIÓN (EXCLUSIVO DUEÑO)
                        composable("auth_admin") { PantallaAuthAdmin(navController) }
                        composable("admin_dashboard") { PantallaAdminDashboard(navController, viewModel) }
                    }
                }
            }
        }
    }
}