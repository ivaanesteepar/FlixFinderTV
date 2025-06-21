package com.ivaanesteepar.flixfindertv.ui.screens

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
import com.ivaanesteepar.flixfindertv.models.Peliculas
import com.ivaanesteepar.flixfindertv.ui.viewmodels.UsersViewModel
import androidx.compose.ui.Alignment
import androidx.compose.material3.IconButton
import androidx.compose.ui.res.painterResource
import com.ivaanesteepar.flixfindertv.R
import com.ivaanesteepar.flixfindertv.ui.viewmodels.ConexionViewModel

// Pantalla que muestra el contenido favorito del usuario, ya sean películas o series
@Composable
fun FavouriteContent(navController: NavController, uid: String, esSerie: Boolean) {
    val userViewModel: UsersViewModel = viewModel()
    val conexionViewModel: ConexionViewModel = viewModel()

    val favoriteMovies = remember { mutableStateOf<List<Peliculas>>(emptyList()) }
    val favoriteSeries = remember { mutableStateOf<List<Peliculas>>(emptyList()) }
    var favoriteMoviesOffline by remember { mutableStateOf<List<Peliculas>>(emptyList()) }
    var favoriteSeriesOffline by remember { mutableStateOf<List<Peliculas>>(emptyList()) }

    val errorMessage = remember { mutableStateOf<String?>(null) }
    val hayConexion by conexionViewModel.conexionEstablecida.collectAsState()

    LaunchedEffect(Unit) {
        userViewModel.getFavoriteMovies(uid,
            onSuccess = { movies -> favoriteMovies.value = movies },
            onFailure = { e -> errorMessage.value = "Error getting favorite movies: ${e.message}" }
        )
        userViewModel.getFavoriteSeries(uid,
            onSuccess = { series -> favoriteSeries.value = series },
            onFailure = { e -> errorMessage.value = "Error getting favorite series: ${e.message}" }
        )
        userViewModel.getPeliculasFavoritasDesdeRoom()
        userViewModel.getSeriesFavoritasDesdeRoom()
    }

    favoriteMoviesOffline = userViewModel.favouriteMovies.value
    favoriteSeriesOffline = userViewModel.favouriteSeries.value

    errorMessage.value?.let {
        Text(text = it, style = MaterialTheme.typography.bodyMedium, color = Color.Red)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.fondo_app),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.padding(top = 16.dp, start = 8.dp).align(Alignment.Start)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            val itemsToShow = if (esSerie) {
                if (hayConexion) favoriteSeries.value else favoriteSeriesOffline
            } else {
                if (hayConexion) favoriteMovies.value else favoriteMoviesOffline
            }

            val emptyText = if (esSerie) "There are no favorite series" else "There are no favorite movies"
            val isSerieFlag = esSerie

            FavouriteGrid(
                items = itemsToShow,
                navController = navController,
                emptyText = emptyText,
                esSerie = isSerieFlag
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FavouriteGrid(
    items: List<Peliculas>,
    navController: NavController,
    emptyText: String,
    esSerie: Boolean
) {
    if (items.isNotEmpty()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            items.chunked(3).forEach { chunk ->
                FlowRow(modifier = Modifier.fillMaxWidth()) {
                    chunk.forEach { item ->
                        Box(
                            modifier = Modifier
                                .width(115.dp)
                                .height(155.dp)
                                .clickable {
                                    navController.navigate("detalles/${item.id}/$esSerie")
                                }
                        ) {
                            val imageUrl = "https://image.tmdb.org/t/p/w500${item.poster_path}"
                            Image(
                                painter = rememberAsyncImagePainter(imageUrl),
                                contentDescription = if (esSerie) "Imagen de la serie" else "Imagen de la película",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = emptyText, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        }
    }
}

