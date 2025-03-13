package com.example.flixfindertv.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.flixfindertv.ui.viewmodels.ConexionViewModel
import com.example.flixfindertv.ui.viewmodels.MoviesViewModel
import com.example.flixfindertv.utils.BottomNavigationBar
import com.google.firebase.auth.FirebaseAuth

@Composable
fun HomeScreen(navController: NavHostController, viewModel: MoviesViewModel, conexionViewModel: ConexionViewModel) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid

    Scaffold(
        bottomBar = {
            if (uid != null) {
                BottomNavigationBar(navController, uid)
            }
        }
    ) { paddingValues ->
        // Contenido principal de la pantalla
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(paddingValues)
        ) {
            // Agregar el texto "Podría interesarte..." con tamaño aumentado
            Text(
                text = "You might be interested...",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 24.sp), // Tamaño de texto aumentado
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Aquí podrías agregar más componentes si es necesario
        }
    }
}







