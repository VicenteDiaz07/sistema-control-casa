package com.example.prueba.network

import android.util.Log
import kotlinx.coroutines.delay

/**
 * Política de reintentos con backoff exponencial
 * Implementa reintentos automáticos con delays crecientes
 */
class RetryPolicy {
    
    /**
     * Ejecuta un bloque de código con reintentos automáticos
     * @param maxRetries Número máximo de reintentos (default: 5)
     * @param initialDelay Delay inicial en ms (default: 1000ms)
     * @param maxDelay Delay máximo en ms (default: 32000ms)
     * @param factor Factor de multiplicación para backoff exponencial (default: 2.0)
     * @param block Bloque de código a ejecutar
     * @return Resultado del bloque si tiene éxito
     * @throws Exception si se exceden los reintentos
     */
    suspend fun <T> executeWithRetry(
        maxRetries: Int = 5,
        initialDelay: Long = 1000,
        maxDelay: Long = 32000,
        factor: Double = 2.0,
        block: suspend () -> T
    ): T {
        var currentDelay = initialDelay
        var lastException: Exception? = null
        
        repeat(maxRetries) { attempt ->
            try {
                return block()
            } catch (e: Exception) {
                lastException = e
                Log.w("RetryPolicy", "Intento ${attempt + 1}/$maxRetries falló: ${e.message}")
                
                if (attempt == maxRetries - 1) {
                    Log.e("RetryPolicy", "Máximo de reintentos alcanzado")
                    throw e
                }
                
                Log.i("RetryPolicy", "Reintentando en ${currentDelay}ms...")
                delay(currentDelay)
                currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
            }
        }
        
        throw lastException ?: Exception("Error desconocido en RetryPolicy")
    }
    
    /**
     * Ejecuta un bloque con reintentos y retorna null en caso de fallo
     * Útil cuando no queremos propagar excepciones
     */
    suspend fun <T> executeWithRetryOrNull(
        maxRetries: Int = 3,
        initialDelay: Long = 1000,
        block: suspend () -> T
    ): T? {
        return try {
            executeWithRetry(maxRetries = maxRetries, initialDelay = initialDelay, block = block)
        } catch (e: Exception) {
            Log.e("RetryPolicy", "Todos los reintentos fallaron: ${e.message}")
            null
        }
    }
}
