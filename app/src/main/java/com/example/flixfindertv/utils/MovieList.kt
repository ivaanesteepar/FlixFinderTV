package com.example.flixfindertv.utils

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.flixfindertv.models.Peliculas
import com.example.flixfindertv.ui.screens.contieneCaracteresNoLatinos

@Composable
fun MovieList(
    movies: List<Peliculas>,
    navController: NavController,
    listState: LazyListState, // Añadido para pasar el state correspondiente
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        state = listState, // Usar el state correspondiente para el desplazamiento
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
