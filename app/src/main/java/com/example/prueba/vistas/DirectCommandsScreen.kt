package com.example.prueba.vistas

import android.content.Context
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.prueba.network.ArduinoClient
import kotlinx.coroutines.launch

/**
 * Pantalla de comandos directos al ESP8266
 * Permite enviar comandos manuales y ver información del sistema
 */
@Composable
fun DirectCommandsScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("config", Context.MODE_PRIVATE)
    val arduinoIP = prefs.getString("arduino_ip", "") ?: ""
    
    val scope = rememberCoroutineScope()
    val arduinoClient = remember { ArduinoClient() }
    
    var systemInfo by remember { mutableStateOf("") }
    var commandResult by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    
    val scrollState = rememberScrollState()
    
    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController, currentRoute = "commands")
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Comandos Directos",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B263B)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "ESP8266: $arduinoIP",
                fontSize = 14.sp,
                color = Color(0xFF666666)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Información del Sistema
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Información del Sistema",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (systemInfo.isNotEmpty()) {
                        Text(
                            text = systemInfo,
                            fontSize = 12.sp,
                            color = Color(0xFF333333)
                        )
                    } else {
                        Text(
                            text = "Presiona 'Obtener Info' para ver el estado",
                            fontSize = 12.sp,
                            color = Color(0xFF999999)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Button(
                        onClick = {
                            isLoading = true
                            scope.launch {
                                val status = arduinoClient.getStatus(arduinoIP)
                                systemInfo = status ?: "Error obteniendo información"
                                isLoading = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading && arduinoIP.isNotEmpty()
                    ) {
                        Text(if (isLoading) "Cargando..." else "Obtener Info del Sistema")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Comandos Rápidos
            Text(
                text = "Comandos Rápidos",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Botón: Activar Alarma
            CommandButton(
                text = "🔔 Activar Alarma",
                color = Color(0xFFD32F2F),
                enabled = !isLoading && arduinoIP.isNotEmpty()
            ) {
                isLoading = true
                scope.launch {
                    val success = arduinoClient.sendCommand(arduinoIP, "alarm_on")
                    commandResult = if (success) "✓ Alarma activada" else "✗ Error"
                    isLoading = false
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Botón: Silenciar Alarma
            CommandButton(
                text = "🔕 Silenciar Alarma",
                color = Color(0xFF2E7D32),
                enabled = !isLoading && arduinoIP.isNotEmpty()
            ) {
                isLoading = true
                scope.launch {
                    val success = arduinoClient.sendCommand(arduinoIP, "silence")
                    commandResult = if (success) "✓ Alarma silenciada" else "✗ Error"
                    isLoading = false
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Botón: Reiniciar ESP8266
            CommandButton(
                text = "🔄 Reiniciar ESP8266",
                color = Color(0xFFFF9800),
                enabled = !isLoading && arduinoIP.isNotEmpty()
            ) {
                isLoading = true
                scope.launch {
                    val success = arduinoClient.sendCommand(arduinoIP, "reset")
                    commandResult = if (success) "✓ Reiniciando..." else "✗ Error"
                    isLoading = false
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Resultado del Comando
            if (commandResult.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (commandResult.startsWith("✓")) 
                            Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                    )
                ) {
                    Text(
                        text = commandResult,
                        modifier = Modifier.padding(16.dp),
                        color = if (commandResult.startsWith("✓")) 
                            Color(0xFF2E7D32) else Color(0xFFD32F2F)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Botón Volver
            OutlinedButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver")
            }
        }
    }
}

@Composable
fun CommandButton(
    text: String,
    color: Color,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(50.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        enabled = enabled,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text, fontSize = 16.sp, color = Color.White)
    }
}
