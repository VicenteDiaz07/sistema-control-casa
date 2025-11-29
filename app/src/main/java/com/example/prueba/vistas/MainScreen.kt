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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import com.example.prueba.model.Alerta
import com.example.prueba.network.ArduinoClient
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MainScreen(navController: NavController) {
    var alerta by remember { mutableStateOf("") }
    var ultimaAlertaTimestamp by remember { mutableStateOf(0L) }
    val db = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()
    val arduinoClient = remember { ArduinoClient() }

    val contexto = LocalContext.current
    val prefs = contexto.getSharedPreferences("config", Context.MODE_PRIVATE)
    val nombreDispositivo = prefs.getString("nombre", "Casa Principal")
    val modo = prefs.getString("modo", "Encendido")
    val arduinoIP = prefs.getString("arduino_ip", "")

    // 🔹 Consultar Arduino cada 3 segundos si está en modo "Encendido"
    LaunchedEffect(modo, arduinoIP) {
        while (true) {
            delay(3000)
            if (modo == "Encendido" && !arduinoIP.isNullOrEmpty()) {
                scope.launch {
                    try {
                        // Obtener alertas del Arduino
                        val response = arduinoClient.getAlerts(arduinoIP)
                        
                        if (response != null && response.contains("alertas")) {
                            // Parsear respuesta simple (el Arduino envía JSON)
                            // Formato esperado: {"alertas":[{"mensaje":"...","tiempo":123456}]}
                            
                            // Obtener estado del sistema
                            val status = arduinoClient.getStatus(arduinoIP)
                            if (status != null && status.contains("ultima_alerta")) {
                                // Extraer mensaje de alerta
                                val mensajeMatch = Regex("\"ultima_alerta\":\"([^\"]+)\"").find(status)
                                val tiempoMatch = Regex("\"tiempo_alerta\":(\\d+)").find(status)
                                
                                if (mensajeMatch != null && tiempoMatch != null) {
                                    val mensaje = mensajeMatch.groupValues[1]
                                    val tiempo = tiempoMatch.groupValues[1].toLongOrNull() ?: 0L
                                    
                                    // Solo guardar si es una alerta nueva
                                    if (tiempo > ultimaAlertaTimestamp && mensaje.isNotEmpty()) {
                                        ultimaAlertaTimestamp = tiempo
                                        alerta = mensaje
                                        
                                        // Guardar en Firestore
                                        val nuevaAlerta = Alerta(
                                            id = db.collection("alertas").document().id,
                                            mensaje = mensaje,
                                            timestamp = System.currentTimeMillis(),
                                            tipo = "movimiento",
                                            dispositivo = nombreDispositivo ?: "Desconocido",
                                            leida = false
                                        )
                                        
                                        db.collection("alertas")
                                            .document(nuevaAlerta.id)
                                            .set(nuevaAlerta)
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        // Error de conexión - no hacer nada
                    }
                }
            } else if (modo == "Apagado") {
                alerta = ""
            }
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController, currentRoute = "main")
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                }
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
