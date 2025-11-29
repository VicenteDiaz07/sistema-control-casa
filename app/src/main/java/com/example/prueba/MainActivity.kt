package com.example.prueba

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.prueba.ui.theme.PruebaTheme
import com.example.prueba.vistas.ConfigScreen
import com.example.prueba.vistas.LoginScreen
import com.example.prueba.vistas.Register
import com.example.prueba.vistas.MainScreen
import com.example.prueba.vistas.HistorialAlertasScreen
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inicia Firebase
        auth = Firebase.auth

        setContent {
            PruebaTheme {
                AppNavigation(auth)
            }
        }
    }
}

@Composable
fun AppNavigation(auth: FirebaseAuth) {
    val navController = rememberNavController()

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController = navController,
                startDestination = "login"
            ) {
                composable("login") { LoginScreen(navController, auth) }
                composable("register") { Register(navController, auth) }
                composable("main") { MainScreen(navController) }
                composable("historial") { HistorialAlertasScreen(navController) }
                composable("config") { ConfigScreen(navController) }
            }
        }
    }
}
