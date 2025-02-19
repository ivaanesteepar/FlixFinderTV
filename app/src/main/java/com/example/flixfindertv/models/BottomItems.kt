package com.example.flixfindertv.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomItems(
    val ruta: String,
    val titulo: String,
    val icono: ImageVector
){
    object Home: BottomItems("home", "Home", Icons.Default.Home)
    object Explore: BottomItems("explore", "Explore", Icons.Default.LiveTv)
    object Search: BottomItems("search", "Search", Icons.Default.Search)
    object Profile: BottomItems("profile", "Profile", Icons.Default.Person)

}