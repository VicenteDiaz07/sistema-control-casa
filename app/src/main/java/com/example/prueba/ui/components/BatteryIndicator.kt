package com.example.prueba.ui.components

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Indicador de batería del dispositivo móvil
 * Muestra el nivel de batería y si está cargando
 */
@Composable
fun BatteryIndicator() {
    val context = LocalContext.current
    val batteryInfo = remember { getBatteryInfo(context) }
    
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Ícono de batería según nivel
        Icon(
            imageVector = if (batteryInfo.level <= 20) 
                Icons.Default.BatteryAlert 
            else 
                Icons.Default.BatteryFull,
            contentDescription = "Batería",
            tint = when {
                batteryInfo.level <= 20 -> Color(0xFFD32F2F)  // Rojo
                batteryInfo.level <= 50 -> Color(0xFFFF9800)  // Naranja
                else -> Color(0xFF4CAF50)  // Verde
            },
            modifier = Modifier.size(24.dp)
        )
        
        // Texto con porcentaje
        Text(
            text = "${batteryInfo.level}%",
            fontSize = 14.sp,
            color = Color(0xFF1B263B)
        )
        
        // Indicador de carga
        if (batteryInfo.isCharging) {
            Text(
                text = "⚡",
                fontSize = 14.sp
            )
        }
    }
}

/**
 * Información de batería
 */
data class BatteryInfo(
    val level: Int,
    val isCharging: Boolean
)

/**
 * Obtiene información de la batería del dispositivo
 */
fun getBatteryInfo(context: Context): BatteryInfo {
    val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
        context.registerReceiver(null, filter)
    }
    
    val level = batteryStatus?.let { intent ->
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        (level * 100 / scale.toFloat()).toInt()
    } ?: 100
    
    val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
    val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                     status == BatteryManager.BATTERY_STATUS_FULL
    
    return BatteryInfo(level, isCharging)
}

/**
 * Estimación de consumo de batería
 * Basado en el uso de la app
 */
@Composable
fun BatteryConsumptionEstimate(
    pollingIntervalSeconds: Int = 3
) {
    val estimatedHours = remember(pollingIntervalSeconds) {
        // Estimación aproximada basada en intervalo de polling
        when {
            pollingIntervalSeconds <= 3 -> "~8-12 horas"
            pollingIntervalSeconds <= 10 -> "~12-18 horas"
            pollingIntervalSeconds <= 30 -> "~18-24 horas"
            else -> "~24+ horas"
        }
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Consumo Estimado",
                fontSize = 14.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = Color(0xFF1B263B)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Duración estimada: $estimatedHours",
                fontSize = 12.sp,
                color = Color(0xFF666666)
            )
            Text(
                text = "Intervalo de actualización: ${pollingIntervalSeconds}s",
                fontSize = 12.sp,
                color = Color(0xFF666666)
            )
        }
    }
}
