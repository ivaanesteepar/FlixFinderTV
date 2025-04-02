package com.example.flixfindertv.utils

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.flixfindertv.models.BottomItems

@Composable
fun BottomNavigationBar(navController: NavController, uid: String) {
    NavigationBar(
        containerColor = Color.Transparent, // Fondo transparente
        modifier = Modifier.height(80.dp) // Altura de la barra de navegación
    ) {
        val items = listOf(
            BottomItems.Home,
            BottomItems.Explore,
            BottomItems.Trivia,
            BottomItems.Profile
        )

        items.forEach { item ->
            // Obtén la ruta de la pantalla actual
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            val isSelected = currentRoute?.startsWith(item.ruta) == true

            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icono,
                        contentDescription = item.titulo,
                        modifier = Modifier.padding(top = 20.dp),
                        tint = if (isSelected) Color.Blue else Color.White // Cambia el color según si está seleccionado
                    )
                },
                label = if (isSelected) {
                    // Muestra el texto solo si el ítem está seleccionado
                    { Text(item.titulo, color = Color.Blue) }
                } else {
                    // No mostrar texto cuando no está seleccionado
                    null
                },
                selected = isSelected, // Marca como seleccionado
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent, // Color de fondo del ítem seleccionado = if (isSelected) LightYellow, // Color del icono cuando está seleccionado
                ),
                onClick = {
                    // Si es el perfil, pasamos el `uid` en la ruta
                    val route = if (item == BottomItems.Profile) {
                        "profile/$uid/false"
                    } else {
                        item.ruta
                    }
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

