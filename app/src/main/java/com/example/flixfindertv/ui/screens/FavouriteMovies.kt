package com.example.flixfindertv.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.material.icons.filled.ArrowBack

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FavouriteMovies(navController: NavController, esSerie: Boolean) {
    val userViewModel: UsersViewModel = viewModel()
    // Estados para las películas y series favoritas
    val favoriteMovies = remember { mutableStateOf<List<Peliculas>>(emptyList()) }
    val favoriteSeries = remember { mutableStateOf<List<Peliculas>>(emptyList()) }

    // Estado para manejar errores
    val errorMessage = remember { mutableStateOf<String?>(null) }

    // Obtener las películas favoritas
    LaunchedEffect(Unit) {
        userViewModel.getFavoriteMovies(
            onSuccess = { movies ->
                favoriteMovies.value = movies
            },
            onFailure = { exception ->
                errorMessage.value = "Error al obtener las películas favoritas: ${exception.message}"
            }
        )

        // Obtener las series favoritas
        userViewModel.getFavoriteSeries(
            onSuccess = { series ->
                favoriteSeries.value = series
            },
            onFailure = { exception ->
                errorMessage.value = "Error al obtener las series favoritas: ${exception.message}"
            }
        )
    }

    // Mostrar mensaje de error si lo hay
    errorMessage.value?.let {
        Text(
            text = it,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Red
        )
    }

    // Mostrar la flecha atrás y el contenido
    Column(
        modifier = Modifier
            .fillMaxWidth()
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
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Volver",
                tint = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(16.dp)) // Espacio entre el botón y las portadas

        // Mostrar las portadas de acuerdo al tipo (películas o series)
        if (esSerie) {
            if (favoriteSeries.value.isNotEmpty()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    favoriteSeries.value.chunked(3).forEach { chunk ->
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
                                    val imageUrl = "https://image.tmdb.org/t/p/w500${series.poster_path}"
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
                Text(
                    text = "No tienes series favoritas.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        } else {
            if (favoriteMovies.value.isNotEmpty()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    favoriteMovies.value.chunked(3).forEach { chunk ->
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
                                    val imageUrl = "https://image.tmdb.org/t/p/w500${movie.poster_path}"
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
                Text(
                    text = "No tienes películas favoritas.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    }
}
