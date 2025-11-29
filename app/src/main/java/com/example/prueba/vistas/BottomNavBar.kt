package com.example.prueba.vistas

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController

sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Main : BottomNavItem("main", Icons.Default.Home, "Principal")
    object Historial : BottomNavItem("historial", Icons.Default.List, "Historial")
    object Config : BottomNavItem("config", Icons.Default.Settings, "Config")
}

@Composable
fun BottomNavigationBar(navController: NavController, currentRoute: String) {
    NavigationBar(
        containerColor = Color.White,
        contentColor = Color(0xFF0D47A1)
    ) {
        val items = listOf(
            BottomNavItem.Main,
            BottomNavItem.Historial,
            BottomNavItem.Config
        )

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            // Evitar múltiples copias de la misma pantalla
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF0D47A1),
                    selectedTextColor = Color(0xFF0D47A1),
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = Color(0xFFE3F2FD)
                )
            )
        }
    }
}
