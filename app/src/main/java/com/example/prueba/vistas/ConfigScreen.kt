package com.example.prueba.vistas

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.prueba.network.ArduinoClient
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun ConfigScreen(navController: NavController) {
    val contexto = LocalContext.current
    val prefs = contexto.getSharedPreferences("config", Context.MODE_PRIVATE)
    val scope = rememberCoroutineScope()
    val arduinoClient = remember { ArduinoClient() }

    var modo by remember { mutableStateOf(prefs.getString("modo", "Encendido") ?: "Encendido") }
    var nombreDispositivo by remember { mutableStateOf(prefs.getString("nombre", "Casa Principal") ?: "Casa Principal") }
    var arduinoIP by remember { mutableStateOf(prefs.getString("arduino_ip", "") ?: "") }
    var estadoConexion by remember { mutableStateOf("No probado") }
    var colorEstado by remember { mutableStateOf(Color.Gray) }
    var probandoConexion by remember { mutableStateOf(false) }

    val conexion = "WiFi"
    val scrollState = rememberScrollState()

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
            contentAlignment = Alignment.TopCenter
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
                        .verticalScroll(scrollState)
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
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Tocar para cambiar", fontSize = 12.sp, color = Color(0xFF79747E))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(modo, fontSize = 16.sp, color = Color(0xFF1B263B))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Nombre del Dispositivo", fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = nombreDispositivo,
                        onValueChange = { nombreDispositivo = it },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0D47A1),
                            unfocusedBorderColor = Color(0xFF757575),
                            cursorColor = Color(0xFF0D47A1),
                            focusedTextColor = Color(0xFF1B263B),
                            unfocusedTextColor = Color(0xFF1B263B)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("IP del Arduino", fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = arduinoIP,
                        onValueChange = { arduinoIP = it },
                        singleLine = true,
                        placeholder = { Text("192.168.1.100") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0D47A1),
                            unfocusedBorderColor = Color(0xFF757575),
                            cursorColor = Color(0xFF0D47A1),
                            focusedTextColor = Color(0xFF1B263B),
                            unfocusedTextColor = Color(0xFF1B263B)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Estado: $estadoConexion",
                        fontSize = 14.sp,
                        color = colorEstado,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Tipo de Conexión", fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = conexion,
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFF757575),
                            unfocusedTextColor = Color(0xFF1B263B)
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

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
                        modifier = Modifier.fillMaxWidth().height(50.dp),
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
                            Text("Probar Conexión", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    MainButton("Guardar Cambios") {
                        scope.launch {
                            // Guardar en SharedPreferences
                            val editor = prefs.edit()
                            editor.putString("modo", modo)
                            editor.putString("nombre", nombreDispositivo)
                            editor.putString("arduino_ip", arduinoIP)
                            editor.apply()
                            
                            // Enviar comando al Arduino para sincronizar el modo
                            if (arduinoIP.isNotEmpty()) {
                                val modoArduino = if (modo == "Encendido") "on" else "off"
                                val resultado = arduinoClient.setMode(arduinoIP, modoArduino)
                                
                                if (resultado) {
                                    estadoConexion = "✓ Modo sincronizado con Arduino"
                                    colorEstado = Color(0xFF2E7D32)
                                } else {
                                    estadoConexion = "⚠️ Guardado local, Arduino no responde"
                                    colorEstado = Color(0xFFFF9800)
                                }
                            }
                            
                            navController.navigate("main")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Button(
                        onClick = {
                            FirebaseAuth.getInstance().signOut()
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
                    ) {
                        Text("Cerrar Sesión", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}
