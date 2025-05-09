package com.example.flixfindertv.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.ui.graphics.vector.ImageVector

// Representa los ítems de la barra de navegación inferior
sealed class BottomItems(
    val ruta: String,
    val titulo: String,
    val icono: ImageVector
){
    object Home: BottomItems("home", "Home", Icons.Default.Home)
    object Explore: BottomItems("explore", "Explore", Icons.Default.LiveTv)
    object Profile: BottomItems("profile", "Profile", Icons.Default.Person)
    object Trivia: BottomItems("trivia", "Trivia", Icons.Default.Quiz)

}