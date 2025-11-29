package com.example.prueba.vistas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.prueba.model.Alerta
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistorialAlertasScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    var listaAlertas by remember { mutableStateOf(listOf<Alerta>()) }
    var cargando by remember { mutableStateOf(true) }

    // Leer alertas de Firestore ordenadas por timestamp descendente
    LaunchedEffect(Unit) {
        db.collection("alertas")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50) // Últimas 50 alertas
            .addSnapshotListener { snapshot, error ->
                cargando = false
                if (snapshot != null && error == null) {
                    listaAlertas = snapshot.toObjects(Alerta::class.java)
                }
            }
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController, currentRoute = "historial")
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                "Historial de Alertas",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B263B)
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            if (cargando) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (listaAlertas.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No hay alertas registradas",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(listaAlertas) { alerta ->
                        AlertaCard(alerta)
                    }
                }
            }
        }
    }
}

@Composable
fun AlertaCard(alerta: Alerta) {
    val colorFondo = when (alerta.tipo) {
        "movimiento" -> Color(0xFFFFF3E0)
        "alarma" -> Color(0xFFFFEBEE)
        "sistema" -> Color(0xFFE3F2FD)
        else -> Color(0xFFF5F5F5)
    }

    val colorTexto = when (alerta.tipo) {
        "movimiento" -> Color(0xFFE65100)
        "alarma" -> Color(0xFFC62828)
        "sistema" -> Color(0xFF0D47A1)
        else -> Color(0xFF424242)
    }

    val icono = when (alerta.tipo) {
        "movimiento" -> "🚶"
        "alarma" -> "🚨"
        "sistema" -> "⚙️"
        else -> "📋"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colorFondo),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icono,
                fontSize = 32.sp,
                modifier = Modifier.padding(end = 12.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alerta.mensaje,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = colorTexto
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = formatearFecha(alerta.timestamp),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                
                if (alerta.dispositivo.isNotEmpty()) {
                    Text(
                        text = "Dispositivo: ${alerta.dispositivo}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

fun formatearFecha(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
