package com.example.prueba.sync

import android.util.Log
import com.example.prueba.network.ArduinoClient
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

/**
 * Gestor de sincronización entre ESP8266 y Firebase
 */
class SyncManager(
    private val db: FirebaseFirestore,
    private val arduinoClient: ArduinoClient
) {
    
    /**
     * Sincroniza datos pendientes del ESP8266 a Firebase
     * @param ip Dirección IP del ESP8266
     */
    suspend fun syncESP8266ToFirebase(ip: String): SyncResult {
        return try {
            // Obtener datos pendientes del ESP8266
            val pendingData = arduinoClient.getPendingData(ip)
            
            if (pendingData.isNullOrEmpty()) {
                return SyncResult.Success(0)
            }
            
            var syncedCount = 0
            pendingData.forEach { reading ->
                try {
                    db.collection("sensor_readings")
                        .add(reading)
                        .await()
                    syncedCount++
                } catch (e: Exception) {
                    Log.e("SyncManager", "Error guardando lectura: ${e.message}")
                }
            }
            
            // Limpiar datos sincronizados del ESP8266
            if (syncedCount > 0) {
                arduinoClient.clearSyncedData(ip)
            }
            
            SyncResult.Success(syncedCount)
        } catch (e: Exception) {
            Log.e("SyncManager", "Error sincronizando: ${e.message}")
            SyncResult.Error(e.message ?: "Error desconocido")
        }
    }
    
    /**
     * Observa el estado de sincronización de Firebase
     * @return Flow con el estado de sincronización
     */
    fun observeSyncStatus(): Flow<SyncStatus> = flow {
        db.collection("sensor_readings")
            .whereEqualTo("synced", false)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // emit(SyncStatus.Error(error.message ?: "Error desconocido"))
                    return@addSnapshotListener
                }
                
                val pendingCount = snapshot?.size() ?: 0
                if (pendingCount > 0) {
                    // emit(SyncStatus.Pending(pendingCount))
                } else {
                    // emit(SyncStatus.Synced)
                }
            }
    }
    
    /**
     * Verifica si hay datos pendientes de sincronizar
     */
    suspend fun hasPendingData(): Boolean {
        return try {
            val snapshot = db.collection("sensor_readings")
                .whereEqualTo("synced", false)
                .limit(1)
                .get()
                .await()
            
            !snapshot.isEmpty
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * Resultado de la sincronización
 */
sealed class SyncResult {
    data class Success(val itemsSynced: Int) : SyncResult()
    data class Error(val message: String) : SyncResult()
}

/**
 * Estado de sincronización
 */
sealed class SyncStatus {
    object Synced : SyncStatus()
    data class Pending(val count: Int) : SyncStatus()
    data class Error(val message: String) : SyncStatus()
}
