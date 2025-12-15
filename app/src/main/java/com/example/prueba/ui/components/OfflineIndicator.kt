package com.example.prueba.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Sync
/**
 * Indicador de estado offline/sincronización
 * Muestra cuando la app está en modo offline o hay datos pendientes de sincronizar
 */
@Composable
fun OfflineIndicator(
    isOnline: Boolean,
    pendingSync: Int = 0
) {
    if (!isOnline || pendingSync > 0) {
        Surface(
            color = if (!isOnline) Color(0xFFFF9800) else Color(0xFF2196F3),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (!isOnline) Icons.Default.CloudOff else Icons.Default.Sync,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (!isOnline) 
                        "Modo Offline - Datos guardados localmente"
                    else 
                        "Sincronizando $pendingSync elementos...",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }
    }
}

/**
 * Estado de conectividad de red
 */
@Composable
fun connectivityState(): State<Boolean> {
    // Por ahora retornar siempre true
    // En producción usar ConnectivityManager
    return remember { mutableStateOf(true) }
}
