package com.example.flixfindertv.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.example.flixfindertv.ui.viewmodels.MoviesViewModel
import com.example.flixfindertv.utils.BottomNavigationBar

@Composable
fun HomeScreen(navController: NavHostController, viewModel: MoviesViewModel) {

    // Obtenemos las películas de la lista de StateFlow
    val movies by viewModel.listaPeliculas.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(true)

    // Llamada a la función para obtener las películas de la API
    LaunchedEffect(Unit) {
        if (movies.isEmpty() && !isLoading) {
            println("Obteniendo películas...")
            viewModel.obtenerTodasLasPeliculas(apiKey = "6ae1f349f576ac17daf45c3d7dfbae9e", language = "es-ES")
        }
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Películas", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(8.dp))

            // Si está cargando, mostramos el cargador
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()  // Indicador de carga
                }
            } else {
                // Verifica si la lista tiene elementos
                if (movies.isEmpty()) {
                    println("No se han cargado películas")
                } else {
                    // Cuando los datos están cargados, mostramos las películas
                    println("Películas cargadas: ${movies.size}")
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),  // Altura de la fila de imágenes
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(movies) { movie ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(120.dp) // Tamaño de cada imagen + título
                            ) {
                                // Card que contiene la imagen y el título
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    elevation = CardDefaults.cardElevation(4.dp)
                                ) {
                                    Column {
                                        // Imagen de la película
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(160.dp)  // Ajusta la altura de la imagen
                                        ) {
                                            val imageUrl = "https://image.tmdb.org/t/p/w500${movie.imagen}"
                                            Image(
                                                painter = rememberAsyncImagePainter(imageUrl),
                                                contentDescription = "Imagen de la película",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }

                                        // Título de la película con fondo colorido
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFF6200EE)) // Color de fondo para el título
                                                .padding(4.dp)
                                        ) {
                                            Text(
                                                text = movie.titulo,  // Título de la película
                                                style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.padding(horizontal = 8.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
