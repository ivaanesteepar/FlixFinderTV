package com.example.flixfindertv.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.flixfindertv.ui.viewmodels.ConexionViewModel
import com.example.flixfindertv.ui.viewmodels.MoviesViewModel
import com.example.flixfindertv.utils.BottomNavigationBar
import com.example.flixfindertv.utils.ScreenRecharge
import java.io.IOException
import java.net.SocketTimeoutException

fun contieneCaracteresNoLatinos(titulo: String): Boolean {
    // Expresión regular para detectar caracteres en chino, japonés, coreano o ruso
    val regex = "[\\u4E00-\\u9FFF\\u3040-\\u30FF\\uAC00-\\uD7AF\\u0400-\\u04FF]".toRegex()
    return regex.containsMatchIn(titulo)
}

@Composable
fun HomeScreen(navController: NavHostController, viewModel: MoviesViewModel, conexionViewModel: ConexionViewModel) {

    val movies by viewModel.listaPeliculas.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)
    val isSeriesLoading by viewModel.isLoadingSeries.observeAsState(false)
    val series by viewModel.listaSeries.observeAsState(emptyList())
    val hayConexion by conexionViewModel.conexionEstablecida

    val maxMovies = 100  // Límite de películas
    val maxSeries = 100  // Límite de series

    // Ejecutar la comprobación de conexión cuando la pantalla se renderiza
    LaunchedEffect(key1 = navController) {
        conexionViewModel.isOnline()
        println("la conexion es: $hayConexion")
    }

    // Cargar películas y series iniciales cuando haya conexión
    LaunchedEffect(hayConexion) {
        if (hayConexion) {
            // Si no hay conexión, cargamos las películas y series desde la base de datos local
            viewModel.obtenerPeliculasPopularesLocal()
            viewModel.obtenerSeriesPopularesLocal()
        }
    }

    // Detectar cuando el usuario llega a la película 20, 40, 60, etc. y cargar más películas
    val movieListState = rememberLazyListState()
    val seriesListState = rememberLazyListState()

    // Cargar más películas cuando llegas al final de la lista de películas
    LaunchedEffect(movieListState.firstVisibleItemIndex) {
        val threshold = 5  // Umbral de carga

        // Verificar si estamos cerca del final de la lista de películas y si no se ha alcanzado el límite de 100
        if (movieListState.firstVisibleItemIndex >= (movies.size - threshold) && !isLoading && movies.size < maxMovies) {
            viewModel.obtenerPeliculasPopularesLocal()  // Cargar más películas
        }
    }

    LaunchedEffect(seriesListState.firstVisibleItemIndex) {
        val threshold = 8  // Umbral de carga

        // Verificar si estamos cerca del final de la lista de series y si no se ha alcanzado el límite de 100
        if (seriesListState.firstVisibleItemIndex >= (series.size - threshold) && !isSeriesLoading && series.size < maxSeries) {
            viewModel.obtenerSeriesPopularesLocal()
        }
    }


    LaunchedEffect(movies) {
        println("Películas cargadas: ${movies.size}")
    }

    LaunchedEffect(series) {
        println("Series cargadas: ${series.size}")
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
            if (!hayConexion) {
                ScreenRecharge(conexionViewModel) {
                    if (hayConexion) {
                        viewModel.obtenerPeliculasPopularesLocal()
                        viewModel.obtenerSeriesPopularesLocal()
                    } else {
                        println("No se pudo recargar, sigue sin conexión.")
                    }
                }
            }

            // Mostrar el indicador de carga solo si estamos cargando y no hay películas ni series
            if (isLoading && (movies.isEmpty() && series.isEmpty())) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // Mostrar las películas cargadas
                if (movies.isNotEmpty()) {
                    Text("Popular movies", style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        state = movieListState,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(movies.filter { !contieneCaracteresNoLatinos(it.titulo) }) { movie ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(120.dp)
                            ) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            navController.navigate("detalles/${movie.id}/false")
                                        },
                                    elevation = CardDefaults.cardElevation(4.dp)
                                ) {
                                    Column {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(160.dp)
                                        ) {
                                            val imageUrl = "https://image.tmdb.org/t/p/w500${movie.poster_path}"
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

                // Mostrar las series solo si hay series cargadas
                if (series.isNotEmpty()) {
                    Text("Popular series", style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        state = seriesListState,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(series.filter { !contieneCaracteresNoLatinos(it.titulo) }) { serie ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(120.dp)
                            ) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            navController.navigate("detalles/${serie.id}/true")
                                        },
                                    elevation = CardDefaults.cardElevation(4.dp)
                                ) {
                                    Column {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(160.dp)
                                        ) {
                                            val imageUrl = "https://image.tmdb.org/t/p/w500${serie.poster_path}"
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




