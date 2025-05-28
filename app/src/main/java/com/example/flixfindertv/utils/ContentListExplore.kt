package com.example.flixfindertv.utils

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.flixfindertv.R
import com.example.flixfindertv.models.Peliculas
import com.example.flixfindertv.ui.viewmodels.GenresViewModel
import com.example.flixfindertv.ui.viewmodels.MoviesViewModel
import com.example.flixfindertv.ui.viewmodels.UsersViewModel

@Composable
fun ContentListExplore(
    movies: List<Peliculas>,
    navController: NavController,
    listState: LazyListState,
) {
    val moviesViewModel: MoviesViewModel = viewModel()
    val genresViewModel: GenresViewModel = viewModel()
    var movieGenre by remember { mutableStateOf("") }
    val usersViewModel: UsersViewModel = viewModel()


    // LazyRow que contiene las películas
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(movies.filter { !moviesViewModel.containsNonLatinCharacters(it.titulo) }) { index, movie ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(120.dp)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            // Obtener el género de la película seleccionada (movie.genre_ids)
                            val selectedMovieGenres = movie.genre_ids
                            genresViewModel.fetchGenreNames(selectedMovieGenres) { genres ->
                                movieGenre = genres.joinToString(", ")
                                usersViewModel.updateFavoriteGenre(movieGenre)
                            }

                            // Navegar a la pantalla de detalles de la película
                            navController.navigate("detalles/${movie.id}/${movie.esSerie}")
                        },
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        ) {
                            val imageUrl = movie.poster_path?.takeIf { it.isNotEmpty() }?.let {
                                "https://image.tmdb.org/t/p/w500$it"
                            } ?: ""

                            Image(
                                painter = if (imageUrl.isNotEmpty()) {
                                    rememberAsyncImagePainter(imageUrl)
                                } else {
                                    painterResource(id = R.drawable.no_poster_image) // Imagen predeterminada
                                },
                                contentDescription = "Imagen de la película",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (movie.esSerie) Color(0xFF4DB6AC) else Color(0xFF42A5F5)
                                )
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
