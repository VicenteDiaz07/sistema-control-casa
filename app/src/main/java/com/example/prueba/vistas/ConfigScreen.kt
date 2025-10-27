package com.example.prueba.vistas

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.BorderStroke

@Composable
fun ConfigScreen(navController: NavController) {
    val contexto = LocalContext.current
    val prefs = contexto.getSharedPreferences("config", Context.MODE_PRIVATE)

    // 🔹 Cargar valores guardados o valores por defecto
    var modo by remember { mutableStateOf(prefs.getString("modo", "Encendido") ?: "Encendido") }
    var nombreDispositivo by remember { mutableStateOf(prefs.getString("nombre", "Casa Principal") ?: "Casa Principal") }

    // 🔹 La conexión siempre será WiFi (no editable)
    val conexion = "WiFi"

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
                Text(
                    text = "Configuración del Sistema",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B263B),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 🔹 Selector de modo (toca para alternar entre Encendido / Apagado)
                Text("Modo Automático", fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            modo = if (modo == "Encendido") "Apagado" else "Encendido"
                        },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFF79747E)),
                    color = Color.Transparent
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Tocar para cambiar",
                            fontSize = 12.sp,
                            color = Color(0xFF79747E)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = modo,
                            fontSize = 16.sp,
                            color = Color(0xFF1B263B)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 🔹 Campo editable para el nombre del dispositivo
                Text("Nombre del Dispositivo", fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = nombreDispositivo,
                    onValueChange = { nombreDispositivo = it },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 🔹 Conexión fija (solo WiFi)
                Text("Conexión", fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = conexion,
                    onValueChange = {},
                    readOnly = true,
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 🔹 Botones de acción
                MainButton("Probar Conexión") {
                    // Aquí podrías probar conexión con el Arduino
                }
                Spacer(modifier = Modifier.height(12.dp))
                MainButton("Guardar Cambios") {
                    val editor = prefs.edit()
                    editor.putString("modo", modo)
                    editor.putString("nombre", nombreDispositivo)
                    editor.apply()
                    navController.popBackStack() // vuelve al main
                }
                Spacer(modifier = Modifier.height(12.dp))
                MainButton("Volver") { navController.popBackStack() }
            }
        }
    }
}
