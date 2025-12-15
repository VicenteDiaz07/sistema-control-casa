package com.example.prueba.model

import kotlinx.serialization.Serializable

/**
 * Modelo de datos para lecturas de sensores
 */
@Serializable
data class SensorReading(
    val timestamp: Long = System.currentTimeMillis(),
    val type: String = "",
    val value: Double = 0.0,
    val dispositivo: String = "",
    val synced: Boolean = false
)
