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
fun BottomNavigationBar(navController: NavController, uid: String) {
    NavigationBar(
        containerColor = androidx.compose.ui.graphics.Color.Gray, // Fondo grisáceo
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

            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icono,
                        contentDescription = item.titulo,
                        modifier = Modifier.padding(top = 20.dp), // Ajusta el espacio superior
                        tint = if (currentRoute?.startsWith(item.ruta) == true) {
                            androidx.compose.ui.graphics.Color.Blue // Si la ruta actual comienza con la ruta del ítem (para el perfil con el uid)
                        } else {
                            androidx.compose.ui.graphics.Color.Unspecified
                        }
                    )
                },
                label = { Text(item.titulo) },
                selected = false, // Desactiva el efecto de selección
                onClick = {
                    // Si es el perfil, pasamos el `uid` en la ruta
                    val route = if (item == BottomItems.Profile) {
                        "profile/$uid"  // Aquí pasamos el UID
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
