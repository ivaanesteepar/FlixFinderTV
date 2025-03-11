package com.example.flixfindertv.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.flixfindertv.ui.viewmodels.ConexionViewModel
import com.example.flixfindertv.ui.viewmodels.MoviesViewModel
import com.example.flixfindertv.utils.BottomNavigationBar
import com.example.flixfindertv.utils.ScreenRecharge
import com.example.flixfindertv.utils.SharedPreferencesManager
import java.io.IOException
import java.net.SocketTimeoutException

@Composable
fun HomeScreen(navController: NavHostController, viewModel: MoviesViewModel, conexionViewModel: ConexionViewModel) {
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
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







