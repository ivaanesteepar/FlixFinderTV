package com.example.flixfindertv.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
    val isLoading by viewModel.isLoading.observeAsState(true)
    val series by viewModel.listaSeries.observeAsState(emptyList())
    val hayConexion by conexionViewModel.conexionEstablecida
    var hecho by remember{mutableStateOf(true)}

    // Ejecutar la comprobación de conexión cuando la pantalla se renderiza
    LaunchedEffect(key1 = navController) {
        // Esperamos el resultado de la función isOnline() asincrónica
        conexionViewModel.isOnline()
        println("la conexion es: $hayConexion")
    }

    LaunchedEffect(hayConexion) {
        println("la conexion en esta parte es: $hayConexion")

        if (hayConexion && hecho) {
            try {
                // Intentamos realizar las peticiones a la API
                viewModel.obtenerPeliculasPopulares(
                    apiKey = "6ae1f349f576ac17daf45c3d7dfbae9e",
                    language = "en-US"
                )
                viewModel.obtenerSeriesPopulares(
                    apiKey = "6ae1f349f576ac17daf45c3d7dfbae9e",
                    language = "en-US"
                )
                viewModel.obtenerPeliculasPopularesLocal(
                    apiKey = "6ae1f349f576ac17daf45c3d7dfbae9e",
                    language = "en-US"
                )
                viewModel.obtenerSeriesPopularesLocal(
                    apiKey = "6ae1f349f576ac17daf45c3d7dfbae9e",
                    language = "en-US"
                )
                hecho = false
            } catch (e: SocketTimeoutException) {
                // Manejo de timeout (conexión tardada)
                println("Timeout: La solicitud de red ha tardado demasiado.")
            } catch (e: IOException) {
                // Manejo de otros errores de red (por ejemplo, sin conexión)
                println("Error de red: ${e.message}")
                // Notificar al usuario que la red no está disponible y permitir recargar
            } catch (e: Exception) {
                // Manejo de otras excepciones inesperadas
                println("Error inesperado: ${e.message}")
            }
        } else {
            println("No hay conexión. Se mostrarán datos locales.")
            // Si no hay conexión, manejar UI mostrando los datos locales, y posiblemente mostrar mensaje de error
        }
    }

    // Usa LaunchedEffect para las funciones suspend
    LaunchedEffect(key1 = navController) {
        val peliculasEnFirestore = viewModel.contarPeliculasEnFirestore()
        val seriesEnFirestore = viewModel.contarSeriesEnFirestore()
        println("Películas en Firestore: $peliculasEnFirestore y Series en Firestore: $seriesEnFirestore")
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
                ScreenRecharge(conexionViewModel){
                    if (hayConexion) {
                        viewModel.obtenerPeliculasPopulares(
                            apiKey = "6ae1f349f576ac17daf45c3d7dfbae9e",
                            language = "en-US"
                        )
                        viewModel.obtenerSeriesPopulares(
                            apiKey = "6ae1f349f576ac17daf45c3d7dfbae9e",
                            language = "en-US"
                        )
                    } else {
                        // Si no hay conexión, podrías mostrar un mensaje o manejar este caso.
                        println("No se pudo recargar, sigue sin conexión.")
                    }
                }
            }

            // Solo mostrar la lista de películas y series si hay conexión
            if (hayConexion && !isLoading) {
                // Popular Movies Section
                Text("Popular movies", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(8.dp))

                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    println("Peliculas cargadas: ${movies.size}")
                    if (movies.isNotEmpty()) {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
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
                                                navController.navigate("detalles/${movie.id}")
                                            },
                                        elevation = CardDefaults.cardElevation(4.dp)
                                    ) {
                                        Column {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(160.dp)
                                            ) {
                                                val imageUrl =
                                                    "https://image.tmdb.org/t/p/w500${movie.portada}"
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
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        color = Color.White
                                                    ),
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

                Spacer(modifier = Modifier.height(16.dp))

                // Popular Series Section
                Text("Popular series", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(8.dp))

                println("Series cargadas: ${series.size}")
                if (series.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
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
                                            navController.navigate("detalles/${serie.id}")
                                        },
                                    elevation = CardDefaults.cardElevation(4.dp)
                                ) {
                                    Column {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(160.dp)
                                        ) {
                                            val imageUrl =
                                                "https://image.tmdb.org/t/p/w500${serie.portada}"
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
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    color = Color.White
                                                ),
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

