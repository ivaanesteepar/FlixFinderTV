package com.example.flixfindertv.utils

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.flixfindertv.R
import com.example.flixfindertv.ui.viewmodels.MoviesViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun MovieDetailsContent(
    movieId: String,
    movieCoverUrl: String,
    movieTitle: String,
    movieDescription: String?,
    movieGenre: String?,
    releaseDate: String?,
    originalLanguage: String?,
    status: String?,
    director: String?,
    directorPhoto: String?,
    seasons: Int?,
    esSerie: Boolean,
    duration: Int?
) {
    val context = LocalContext.current
    val viewModel: MoviesViewModel = viewModel()
    val voteAverage by viewModel.voteAverage.collectAsState()
    val popularity by viewModel.popularity.collectAsState()
    val voteCount by viewModel.voteCount.collectAsState()
    val truncatedVoteAvg = (voteAverage * 10).toInt() / 10.0
    val cappedVoteAvg = minOf(truncatedVoteAvg, 10.0) // Asegura que la media no sea mayor a 10
    val voteAvgFormatted = if (cappedVoteAvg % 1.0 == 0.0) {
        cappedVoteAvg.toInt().toString()
    } else {
        String.format("%.1f", cappedVoteAvg)
    }

    val imageLoader = remember { ImageLoaderProvider.getImageLoader(context) }

    val color = when {
        voteAverage < 5.0 -> Color(0xFFFF6F61) // Rojo
        voteAverage <= 7.5 -> Color(0xFF00B0FF) // Azul
        voteAverage <= 10.0 -> Color(0xFF2ECC71) // Verde
        else -> Color(0xFF2ECC71)
    }

    LaunchedEffect(movieId) {
        viewModel.observeMovieDetails(movieId)
    }

    Row(modifier = Modifier.padding(horizontal = 16.dp)) {
        Box(
            modifier = Modifier
                .width(160.dp)
                .height(255.dp)
                .padding(end = 8.dp)
        ) {
            val painter = rememberAsyncImagePainter(
                model = ImageRequest.Builder(context)
                    .data(movieCoverUrl)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .placeholder(R.drawable.no_poster_image)
                    .error(R.drawable.no_poster_image)
                    .fallback(R.drawable.no_poster_image)
                    .build(),
                imageLoader = imageLoader
            )

            // Portada
            Image(
                painter = painter,
                contentDescription = "Portada",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(160.dp)
                    .height(255.dp)
                    .padding(top = 20.dp)
            )

            // Círculo justo debajo de la portada, alineado a la derecha
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color)
            ) {
                Text(
                    text = voteAvgFormatted,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(4.dp)
                )
            }
        }


        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = movieTitle,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                modifier = Modifier.padding(start = 16.dp)
            )

            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Genre: ")
                    }
                    append(movieGenre ?: "N/A") // Si no hay género, muestra "N/A"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.padding(start = 16.dp)
            )

            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Popularity: ")
                    }
                    append(
                        String.format(
                            "%.1f",
                            popularity ?: 0.0
                        )
                    ) // Si no hay popularidad, muestra "0.0"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.padding(start = 16.dp)
            )

            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Release Date: ")
                    }
                    try {
                        val originalDate =
                            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(
                                releaseDate ?: ""
                            )
                        val formattedDate =
                            originalDate?.let {
                                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it)
                            }
                        append(formattedDate ?: "N/A") // Si no hay fecha, muestra "N/A"
                    } catch (e: Exception) {
                        append(releaseDate ?: "N/A")  // Si no hay fecha, muestra "N/A"
                    }
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.padding(start = 16.dp)
            )
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Votes: ")
                    }
                    append(voteCount ?: "N/A") // Si no hay conteo de votos, muestra "N/A"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.padding(start = 16.dp)
            )
            if (esSerie){
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("Seasons: ")
                        }
                        append(seasons.toString())
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
            else{
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("Duration: ")
                        }
                        append("$duration min")
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(32.dp))
    Text(
        text = "Description",
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp),
        color = Color.White
    )
    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = if (movieDescription.isNullOrEmpty()) "Description not available" else movieDescription,
        style = MaterialTheme.typography.bodyMedium,
        color = if (movieDescription.isNullOrEmpty()) Color.Gray else Color.White,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp)
    )

    Spacer(modifier = Modifier.height(36.dp))
    Text(
        text = "Direction",
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp),
        color = Color.White
    )
    Spacer(modifier = Modifier.height(16.dp))
    Card(
        modifier = Modifier
            .width(140.dp)
            .padding(horizontal = 16.dp), // Agrega márgenes
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                val painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(context)
                        .data(directorPhoto)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .placeholder(R.drawable.no_poster_image)
                        .error(R.drawable.no_poster_image)
                        .fallback(R.drawable.no_poster_image)
                        .build(),
                    imageLoader = imageLoader
                )

                Image(
                    painter = painter,
                    contentDescription = "Foto del director",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFD1A1D2)) // Fondo morado claro
                    .padding(4.dp)
            ) {
                Text(
                    text = if (director.isNullOrEmpty()) "Unknown" else director,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.Black
                    ),
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(36.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 25.dp),
        horizontalArrangement = Arrangement.Center // Esto asegura que las columnas se centren horizontalmente
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Original Language:",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Text(
                text = originalLanguage ?: "N/A", // Si no hay idioma original, muestra "N/A"
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Status:",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Text(
                text = status ?: "N/A", // Si no hay estado, muestra "N/A"
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )
        }
    }
}
