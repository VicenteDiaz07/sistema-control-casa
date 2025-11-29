package com.example.prueba.model

data class Alerta(
    val id: String = "",
    val mensaje: String = "",
    val timestamp: Long = 0,
    val tipo: String = "", // "movimiento", "alarma", "sistema"
    val dispositivo: String = "",
    val leida: Boolean = false
)
