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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.disk.DiskCache
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.flixfindertv.R
import com.example.flixfindertv.models.Peliculas
import com.example.flixfindertv.ui.viewmodels.GenresViewModel
import com.example.flixfindertv.ui.viewmodels.MoviesViewModel
import com.example.flixfindertv.ui.viewmodels.UsersViewModel
import java.io.File

@Composable
fun ContentListExplore(
    movies: List<Peliculas>,
    navController: NavController,
    listState: LazyListState,
) {
    val context = LocalContext.current

    val imageLoader = remember { ImageLoaderProvider.getImageLoader(context) }

    val moviesViewModel: MoviesViewModel = viewModel()
    val genresViewModel: GenresViewModel = viewModel()
    var movieGenre by remember { mutableStateOf("") }
    val usersViewModel: UsersViewModel = viewModel()

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(movies.filter { !moviesViewModel.containsNonLatinCharacters(it.titulo) }) { _, movie ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(120.dp)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val selectedMovieGenres = movie.genre_ids
                            genresViewModel.fetchGenreNames(selectedMovieGenres) { genres ->
                                movieGenre = genres.joinToString(", ")
                                usersViewModel.updateFavoriteGenre(movieGenre)
                            }
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
                            }

                            // Cachea las portadas
                            val painter = rememberAsyncImagePainter(
                                model = ImageRequest.Builder(context)
                                    .data(imageUrl)
                                    .diskCachePolicy(CachePolicy.ENABLED)
                                    .placeholder(R.drawable.no_poster_image)
                                    .error(R.drawable.no_poster_image)
                                    .fallback(R.drawable.no_poster_image)
                                    .build(),
                                imageLoader = imageLoader
                            )

                            Image(
                                painter = painter,
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
