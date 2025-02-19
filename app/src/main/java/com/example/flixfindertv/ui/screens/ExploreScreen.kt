package com.example.flixfindertv.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.example.flixfindertv.utils.BottomNavigationBar

@Composable
fun ExploreScreen(navController: NavHostController) {
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) } // Añadimos el menú inferior
    ) { paddingValues ->
        Modifier.padding(paddingValues)
    }
}
