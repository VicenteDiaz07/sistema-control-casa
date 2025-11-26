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
import com.example.prueba.network.ArduinoClient
import kotlinx.coroutines.launch

@Composable
fun ConfigScreen(navController: NavController) {
    val contexto = LocalContext.current
    val prefs = contexto.getSharedPreferences("config", Context.MODE_PRIVATE)
    val scope = rememberCoroutineScope()
    val arduinoClient = remember { ArduinoClient() }

    // 🔹 Cargar valores guardados o valores por defecto
    var modo by remember { mutableStateOf(prefs.getString("modo", "Encendido") ?: "Encendido") }
    var nombreDispositivo by remember { mutableStateOf(prefs.getString("nombre", "Casa Principal") ?: "Casa Principal") }
    var arduinoIP by remember { mutableStateOf(prefs.getString("arduino_ip", "192.168.1.100") ?: "192.168.1.100") }
    
    // Estados para la prueba de conexión
    var estadoConexion by remember { mutableStateOf("No probado") }
    var colorEstado by remember { mutableStateOf(Color.Gray) }
    var probandoConexion by remember { mutableStateOf(false) }

    // 🔹 La conexión siempre será WiFi (no editable)
    val conexion = "WiFi"

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController, currentRoute = "config")
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

                    // 🔹 IP del Arduino
                    Text("IP del Arduino", fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = arduinoIP,
                        onValueChange = { arduinoIP = it },
                        singleLine = true,
                        placeholder = { Text("192.168.1.100") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 🔹 Estado de conexión
                    Text(
                        text = "Estado: $estadoConexion",
                        fontSize = 14.sp,
                        color = colorEstado,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 🔹 Conexión fija (solo WiFi)
                    Text("Tipo de Conexión", fontWeight = FontWeight.Medium)
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
                    Button(
                        onClick = {
                            probandoConexion = true
                            estadoConexion = "Probando..."
                            colorEstado = Color(0xFFFF9800)
                            
                            scope.launch {
                                val resultado = arduinoClient.testConnection(arduinoIP)
                                probandoConexion = false
                                if (resultado) {
                                    estadoConexion = "✓ Conectado"
                                    colorEstado = Color(0xFF2E7D32)
                                } else {
                                    estadoConexion = "✗ Error de conexión"
                                    colorEstado = Color(0xFFC62828)
                                }
                            }
                        },
                        enabled = !probandoConexion,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0D47A1),
                            disabledContainerColor = Color.Gray
                        )
                    ) {
                        if (probandoConexion) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Probar Conexión",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    MainButton("Guardar Cambios") {
                        val editor = prefs.edit()
                        editor.putString("modo", modo)
                        editor.putString("nombre", nombreDispositivo)
                        editor.putString("arduino_ip", arduinoIP)
                        editor.apply()
                        navController.navigate("main")
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // 🔹 Botón de cerrar sesión
                    Button(
                        onClick = {
                            // Cerrar sesión de Firebase
                            com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                            // Navegar al login y limpiar el back stack
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
                    ) {
                        Text(
                            text = "Cerrar Sesión",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
