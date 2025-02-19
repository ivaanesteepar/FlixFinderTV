package com.example.flixfindertv.utils

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.flixfindertv.models.BottomItems

@Composable
fun BottomNavigationBar(navController: NavController) {
    NavigationBar(
        containerColor = androidx.compose.ui.graphics.Color.Gray, // Fondo grisáceo
        modifier = Modifier.height(80.dp) // Aumentar la altura de la barra de navegación
    ) {
        val items = listOf(
            BottomItems.Home,
            BottomItems.Explore,
            BottomItems.Search,
            BottomItems.Profile
        )
        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icono,
                        contentDescription = item.titulo,
                        modifier = Modifier.padding(top = 20.dp), // Desplazar íconos hacia abajo
                        tint = if (navController.currentDestination?.route == item.ruta) androidx.compose.ui.graphics.Color.Yellow else androidx.compose.ui.graphics.Color.Unspecified
                    )
                },
                label = { Text(item.titulo) },
                selected = false, // Desactiva el efecto de selección
                onClick = {
                    navController.navigate(item.ruta) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

