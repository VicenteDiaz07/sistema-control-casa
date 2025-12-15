package com.example.prueba.network

import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class ArduinoClient {
    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 5000
            connectTimeoutMillis = 5000
            socketTimeoutMillis = 5000
        }
        engine {
            connectTimeout = 5_000
            socketTimeout = 5_000
        }
    }

    /**
     * Prueba la conexión con el Arduino
     * @param ip Dirección IP del Arduino (ej: "192.168.1.100")
     * @return true si la conexión es exitosa, false en caso contrario
     */
    suspend fun testConnection(ip: String): Boolean {
        return try {
            val response: HttpResponse = client.get("http://$ip/status")
            response.status.isSuccess()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Obtiene el estado del sistema de seguridad desde el Arduino
     * @param ip Dirección IP del Arduino
     * @return Estado del sistema (puede incluir sensores, alarmas, etc.)
     */
    suspend fun getStatus(ip: String): String? {
        return try {
            val response: HttpResponse = client.get("http://$ip/status")
            if (response.status.isSuccess()) {
                response.bodyAsText()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Envía un comando al Arduino
     * @param ip Dirección IP del Arduino
     * @param command Comando a enviar (ej: "alarm_on", "alarm_off", "silence")
     * @return true si el comando se envió exitosamente
     */
    suspend fun sendCommand(ip: String, command: String): Boolean {
        return try {
            val response: HttpResponse = client.post("http://$ip/command") {
                contentType(ContentType.Application.Json)
                setBody("{\"command\":\"$command\"}")
            }
            response.status.isSuccess()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Obtiene las alertas recientes del Arduino
     * @param ip Dirección IP del Arduino
     * @return Lista de alertas en formato JSON string
     */
    suspend fun getAlerts(ip: String): String? {
        return try {
            val response: HttpResponse = client.get("http://$ip/alerts")
            if (response.status.isSuccess()) {
                response.bodyAsText()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Cambia el modo del sistema (Encendido/Apagado)
     * @param ip Dirección IP del Arduino
     * @param mode "on" para encendido, "off" para apagado
     * @return true si el cambio fue exitoso
     */
    suspend fun setMode(ip: String, mode: String): Boolean {
        return try {
            val response: HttpResponse = client.post("http://$ip/mode") {
                contentType(ContentType.Application.Json)
                setBody("{\"mode\":\"$mode\"}")
            }
            response.status.isSuccess()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Obtiene datos pendientes de sincronización del ESP8266
     * @param ip Dirección IP del ESP8266
     * @return Lista de lecturas de sensores pendientes
     */
    suspend fun getPendingData(ip: String): List<Map<String, Any>>? {
        return try {
            val response: HttpResponse = client.get("http://$ip/pending-data")
            if (response.status.isSuccess()) {
                val jsonString = response.bodyAsText()
                // Parsear JSON manualmente ya que es una lista simple
                parseJsonArray(jsonString)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Limpia los datos ya sincronizados del ESP8266
     * @param ip Dirección IP del ESP8266
     * @return true si se limpiaron exitosamente
     */
    suspend fun clearSyncedData(ip: String): Boolean {
        return try {
            val response: HttpResponse = client.post("http://$ip/clear-synced")
            response.status.isSuccess()
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Parser simple de JSON array
     */
    private fun parseJsonArray(jsonString: String): List<Map<String, Any>> {
        // Implementación básica - en producción usar una librería JSON
        val result = mutableListOf<Map<String, Any>>()
        // Por ahora retornar lista vacía, se puede mejorar con kotlinx.serialization
        return result
    }

    fun close() {
        client.close()
    }
}
