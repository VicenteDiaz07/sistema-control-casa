package com.example.prueba.power

import android.content.Context
import androidx.work.*
import com.example.prueba.network.ArduinoClient
import com.example.prueba.sync.SyncManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * WorkManager para sincronización periódica en background
 * Optimizado para ahorro de energía
 */
class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val prefs = applicationContext.getSharedPreferences("config", Context.MODE_PRIVATE)
            val arduinoIP = prefs.getString("arduino_ip", "") ?: ""
            val modo = prefs.getString("modo", "Encendido") ?: "Encendido"
            
            if (arduinoIP.isEmpty() || modo == "Apagado") {
                return@withContext Result.success()
            }
            
            // Sincronizar datos
            val arduinoClient = ArduinoClient()
            val db = FirebaseFirestore.getInstance()
            val syncManager = SyncManager(db, arduinoClient)
            
            val result = syncManager.syncESP8266ToFirebase(arduinoIP)
            
            arduinoClient.close()
            
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
    
    companion object {
        private const val WORK_NAME = "sync_worker"
        
        /**
         * Programa sincronización periódica
         * @param context Contexto de la aplicación
         * @param intervalMinutes Intervalo en minutos (mínimo 15)
         */
        fun schedule(context: Context, intervalMinutes: Long = 15) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)  // Solo cuando batería no está baja
                .build()
            
            val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
                intervalMinutes, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
        }
        
        /**
         * Cancela la sincronización periódica
         */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
