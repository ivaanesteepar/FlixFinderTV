package com.example.flixfindertv.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController

@Composable
fun DetailsScreen (navController: NavHostController, id: String) {
    Text("id: $id")
}