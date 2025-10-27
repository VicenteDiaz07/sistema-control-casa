package com.example.prueba.vistas

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MainScreen(navController: NavController) {
    var alerta by remember { mutableStateOf("") }

    val contexto = LocalContext.current
    val prefs = contexto.getSharedPreferences("config", Context.MODE_PRIVATE)
    val nombreDispositivo = prefs.getString("nombre", "Casa Principal")
    val modo = prefs.getString("modo", "Encendido")

    // 🔹 Actualizar alerta cada 5 segundos si está en modo "Encendido"
    LaunchedEffect(modo) {
        while (true) {
            delay(5000)
            if (modo == "Encendido") {
                val horaActual = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                alerta = "Movimiento detectado a las $horaActual"
            } else {
                alerta = "" // no mostrar alerta si está apagado
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // 🔹 Título principal
                Text(
                    text = "Alertas Actuales",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B263B),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 🔹 Mostrar información del dispositivo
                Text(
                    text = "Dispositivo: $nombreDispositivo",
                    fontSize = 16.sp,
                    color = Color(0xFF1B263B)
                )

                Text(
                    text = "Estado: $modo",
                    fontSize = 16.sp,
                    color = if (modo == "Encendido") Color(0xFF2E7D32) else Color(0xFFB71C1C)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 🔹 Mostrar UNA sola alerta si está encendido
                if (modo == "Encendido" && alerta.isNotEmpty()) {
                    AlertCard(texto = alerta)
                } else {
                    Text(
                        text = if (modo == "Encendido") "Esperando alertas..." else "",
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 🔹 Botones
                MainButton("Silenciar Alarma") { /* acción simulada */ }
                Spacer(modifier = Modifier.height(12.dp))
                MainButton("Ver Detalles del Sensor") {
                    navController.navigate("config")
                }
                Spacer(modifier = Modifier.height(12.dp))
                MainButton("Volver") { navController.popBackStack() }
            }
        }
    }
}

// 🔹 Alerta visual
@Composable
fun AlertCard(texto: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFCDD2), shape = RoundedCornerShape(8.dp))
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = texto,
            color = Color(0xFFB71C1C),
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// 🔹 Botón reutilizable
@Composable
fun MainButton(texto: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1))
    ) {
        Text(
            text = texto,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
