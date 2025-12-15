package com.example.prueba.network

import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Monitor de conexión con sistema de heartbeat
 * Detecta automáticamente cuando se pierde la conexión con el ESP8266
 */
class ConnectionMonitor(
    private val arduinoClient: ArduinoClient
) {
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    
    private var lastHeartbeat: Long = 0
    private val heartbeatInterval = 10_000L // 10 segundos
    private val heartbeatTimeout = 30_000L // 30 segundos sin respuesta = desconectado
    
    /**
     * Inicia el monitoreo de conexión
     * @param ip Dirección IP del ESP8266
     */
    suspend fun startMonitoring(ip: String) {
        while (true) {
            delay(heartbeatInterval)
            
            if (ip.isEmpty()) {
                _connectionState.value = ConnectionState.DISCONNECTED
                continue
            }
            
            val isConnected = sendHeartbeat(ip)
            val currentTime = System.currentTimeMillis()
            
            when {
                isConnected -> {
                    lastHeartbeat = currentTime
                    if (_connectionState.value != ConnectionState.CONNECTED) {
                        _connectionState.value = ConnectionState.CONNECTED
                        Log.i("ConnectionMonitor", "Conexión establecida con ESP8266")
                    }
                }
                currentTime - lastHeartbeat > heartbeatTimeout -> {
                    if (_connectionState.value != ConnectionState.DISCONNECTED) {
                        _connectionState.value = ConnectionState.DISCONNECTED
                        handleConnectionLoss()
                    }
                }
                else -> {
                    if (_connectionState.value == ConnectionState.CONNECTED) {
                        _connectionState.value = ConnectionState.RECONNECTING
                        Log.w("ConnectionMonitor", "Intentando reconectar...")
                    }
                }
            }
        }
    }
    
    /**
     * Envía un heartbeat al ESP8266
     */
    private suspend fun sendHeartbeat(ip: String): Boolean {
        return try {
            arduinoClient.testConnection(ip)
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Maneja la pérdida de conexión
     */
    private fun handleConnectionLoss() {
        Log.e("ConnectionMonitor", "Conexión perdida con ESP8266")
        // Aquí se puede notificar al usuario, activar modo offline, etc.
    }
    
    /**
     * Detiene el monitoreo
     */
    fun stop() {
        _connectionState.value = ConnectionState.DISCONNECTED
    }
}

/**
 * Estados de conexión posibles
 */
sealed class ConnectionState {
    object CONNECTED : ConnectionState()
    object DISCONNECTED : ConnectionState()
    object RECONNECTING : ConnectionState()
}
