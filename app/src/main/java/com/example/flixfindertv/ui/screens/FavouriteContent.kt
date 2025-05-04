package com.example.flixfindertv.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.flixfindertv.models.Peliculas
import com.example.flixfindertv.ui.viewmodels.UsersViewModel
import androidx.compose.ui.Alignment
import androidx.compose.material3.IconButton
import androidx.compose.ui.res.painterResource
import com.example.flixfindertv.R
import com.example.flixfindertv.ui.viewmodels.ConexionViewModel

// Pantalla que muestra el contenido favorito del usuario, ya sean películas o series
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FavouriteContent(navController: NavController, uid:String, esSerie: Boolean) {
    val userViewModel: UsersViewModel = viewModel()
    val conexionViewModel: ConexionViewModel = viewModel()

    val favoriteMovies = remember { mutableStateOf<List<Peliculas>>(emptyList()) }
    val favoriteSeries = remember { mutableStateOf<List<Peliculas>>(emptyList()) }
    var favoriteMoviesOffline by remember { mutableStateOf<List<Peliculas>>(emptyList()) }
    var favoriteSeriesOffline by remember { mutableStateOf<List<Peliculas>>(emptyList()) }

    // Estado para manejar errores
    val errorMessage = remember { mutableStateOf<String?>(null) }
    val hayConexion by conexionViewModel.conexionEstablecida.collectAsState()

    // Obtener las películas favoritas
    LaunchedEffect(Unit) {
        userViewModel.getFavoriteMovies(
            uid,
            onSuccess = { movies ->
                favoriteMovies.value = movies
            },
            onFailure = { exception ->
                errorMessage.value = "Error getting favorite movies: ${exception.message}"
            },
        )

        userViewModel.getFavoriteSeries(
            uid,
            onSuccess = { series ->
                favoriteSeries.value = series
            },
            onFailure = { exception ->
                errorMessage.value = "Error getting favorite series: ${exception.message}"
            }
        )
        userViewModel.getPeliculasFavoritasDesdeRoom()
        userViewModel.getSeriesFavoritasDesdeRoom()
    }

    favoriteMoviesOffline = userViewModel.favouriteMovies.value
    favoriteSeriesOffline = userViewModel.favouriteSeries.value

    // Mostrar mensaje de error si lo hay
    errorMessage.value?.let {
        Text(
            text = it,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Red
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.fondo_app),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        // Mostrar la flecha atrás y el contenido
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Botón de flecha atrás
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .padding(top = 16.dp, start = 8.dp)
                    .align(Alignment.Start)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.height(16.dp)) // Espacio entre el botón y las portadas
            // Mostrar las portadas de acuerdo al tipo (películas o series)
            if (esSerie) {
                val seriesToShow = if (hayConexion) favoriteSeries.value else favoriteSeriesOffline
                if (seriesToShow.isNotEmpty()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        seriesToShow.chunked(3).forEach { chunk ->
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                chunk.forEach { series ->
                                    Box(
                                        modifier = Modifier
                                            .width(120.dp)
                                            .height(160.dp)
                                            .clickable {
                                                navController.navigate("detalles/${series.id}/${true}")
                                            }
                                    ) {
                                        val imageUrl =
                                            "https://image.tmdb.org/t/p/w500${series.poster_path}"
                                        Image(
                                            painter = rememberAsyncImagePainter(imageUrl),
                                            contentDescription = "Imagen de la serie",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp)) // Espacio entre las filas de las portadas
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "You have no favorite series",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                val moviesToShow = if (hayConexion) favoriteMovies.value else favoriteMoviesOffline
                if (moviesToShow.isNotEmpty()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        moviesToShow.chunked(3).forEach { chunk ->
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                chunk.forEach { movie ->
                                    Box(
                                        modifier = Modifier
                                            .width(120.dp)
                                            .height(160.dp)
                                            .clickable {
                                                navController.navigate("detalles/${movie.id}/${false}")
                                            }
                                    ) {
                                        val imageUrl =
                                            "https://image.tmdb.org/t/p/w500${movie.poster_path}"
                                        Image(
                                            painter = rememberAsyncImagePainter(imageUrl),
                                            contentDescription = "Imagen de la película",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp)) // Espacio entre las filas de las portadas
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "You have no favorite movies",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }

}
