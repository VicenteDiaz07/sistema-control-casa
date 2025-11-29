package com.example.prueba.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = SecurityBlue40,
    secondary = AlertRed40,
    tertiary = AccentOrange40
)

@Composable
fun PruebaTheme(
    content: @Composable () -> Unit
) {
    // Siempre usar modo claro para evitar bugs con modo oscuro
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}