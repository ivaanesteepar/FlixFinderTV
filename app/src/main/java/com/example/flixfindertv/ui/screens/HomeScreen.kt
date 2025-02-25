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
import com.example.flixfindertv.ui.viewmodels.MoviesViewModel
import com.example.flixfindertv.utils.BottomNavigationBar

@Composable
fun HomeScreen(navController: NavHostController, viewModel: MoviesViewModel) {

    val movies by viewModel.listaPeliculas.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(true)
    val series by viewModel.listaSeries.observeAsState(emptyList())

    LaunchedEffect(Unit) {
        if (movies.isEmpty() && series.isEmpty() && !isLoading) {
            viewModel.obtenerPeliculasPopulares(apiKey = "6ae1f349f576ac17daf45c3d7dfbae9e", language = "es-ES")
            viewModel.obtenerSeriesPopulares(apiKey = "6ae1f349f576ac17daf45c3d7dfbae9e", language = "es-ES")
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
            Text("Películas populares", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(8.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                if (movies.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(movies) { movie ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(120.dp)
                            ) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    elevation = CardDefaults.cardElevation(4.dp)
                                ) {
                                    Column {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(160.dp)
                                        ) {
                                            val imageUrl = "https://image.tmdb.org/t/p/w500${movie.portada}"
                                            Image(
                                                painter = rememberAsyncImagePainter(imageUrl),
                                                contentDescription = "Imagen de la película",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFF6200EE))
                                                .padding(4.dp)
                                        ) {
                                            Text(
                                                text = movie.titulo,
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
                Spacer(modifier = Modifier.height(16.dp))
                Text("Series populares", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(8.dp))

                println("Series cargadas: ${series.size}")
                if (series.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(series) { serie ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(120.dp)
                            ) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    elevation = CardDefaults.cardElevation(4.dp)
                                ) {
                                    Column {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(160.dp)
                                        ) {
                                            val imageUrl = "https://image.tmdb.org/t/p/w500${serie.portada}"
                                            Image(
                                                painter = rememberAsyncImagePainter(imageUrl),
                                                contentDescription = "Imagen de la serie",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFF03A9F4))
                                                .padding(4.dp)
                                        ) {
                                            Text(
                                                text = serie.titulo,
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
