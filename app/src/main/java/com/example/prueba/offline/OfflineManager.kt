package com.example.prueba.offline

import android.content.Context
import android.util.Log
import com.example.prueba.network.ArduinoClient
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Gestor de modo offline
 * Maneja la cola de comandos cuando no hay conexión
 */
class OfflineManager(
    private val context: Context,
    private val db: FirebaseFirestore
) {
    private val _isOfflineMode = MutableStateFlow(false)
    val isOfflineMode: StateFlow<Boolean> = _isOfflineMode.asStateFlow()
    
    private val pendingCommands = mutableListOf<PendingCommand>()
    
    /**
     * Activa el modo offline
     */
    fun enableOfflineMode() {
        _isOfflineMode.value = true
        Log.i("OfflineManager", "Modo offline activado")
    }
    
    /**
     * Desactiva el modo offline
     */
    fun disableOfflineMode() {
        _isOfflineMode.value = false
        Log.i("OfflineManager", "Modo offline desactivado")
    }
    
    /**
     * Agrega un comando a la cola de pendientes
     */
    fun queueCommand(command: PendingCommand) {
        pendingCommands.add(command)
        Log.i("OfflineManager", "Comando agregado a cola: ${command.type}")
        
        // Guardar en Firestore (se sincronizará automáticamente cuando haya conexión)
        db.collection("pending_commands")
            .add(command)
            .addOnSuccessListener {
                Log.d("OfflineManager", "Comando guardado en Firestore")
            }
            .addOnFailureListener { e ->
                Log.e("OfflineManager", "Error guardando comando: ${e.message}")
            }
    }
    
    /**
     * Sincroniza comandos pendientes cuando hay conexión
     */
    suspend fun syncPendingCommands(arduinoClient: ArduinoClient, ip: String) {
        if (pendingCommands.isEmpty()) {
            return
        }
        
        Log.i("OfflineManager", "Sincronizando ${pendingCommands.size} comandos pendientes")
        
        val commandsToRemove = mutableListOf<PendingCommand>()
        
        for (command in pendingCommands) {
            try {
                val success = arduinoClient.sendCommand(ip, command.type)
                if (success) {
                    commandsToRemove.add(command)
                    Log.d("OfflineManager", "Comando sincronizado: ${command.type}")
                }
            } catch (e: Exception) {
                Log.e("OfflineManager", "Error sincronizando comando: ${e.message}")
            }
        }
        
        pendingCommands.removeAll(commandsToRemove)
        
        // Limpiar comandos sincronizados de Firestore
        commandsToRemove.forEach { command ->
            db.collection("pending_commands")
                .whereEqualTo("timestamp", command.timestamp)
                .get()
                .addOnSuccessListener { documents ->
                    for (doc in documents) {
                        doc.reference.delete()
                    }
                }
        }
    }
    
    /**
     * Obtiene el número de comandos pendientes
     */
    fun getPendingCommandsCount(): Int = pendingCommands.size
}

/**
 * Comando pendiente de ejecución
 */
data class PendingCommand(
    val type: String,
    val timestamp: Long = System.currentTimeMillis(),
    val params: Map<String, String> = emptyMap()
)
