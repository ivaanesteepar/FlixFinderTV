package com.example.flixfindertv.utils

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter

@Composable
fun MovieDetailsContent(
    movieCoverUrl: String,
    movieTitle: String,
    movieDescription: String,
    movieGenre: String,
    moviePopularity: Double,
    releaseDate: String,
    voteAverage: String
) {
    val voteAvg = voteAverage.toDoubleOrNull() ?: 0.0
    val truncatedVoteAvg = (voteAvg * 10).toInt() / 10.0
    val voteAvgFormatted = String.format("%.1f", truncatedVoteAvg)

    val color = when {
        voteAvg in 0.0..4.9 -> Color(0xFFFF6F61)
        voteAvg in 5.0..7.5 -> Color(0xFF00B0FF)
        voteAvg in 7.5..10.0 -> Color(0xFF2ECC71)
        else -> Color.Gray
    }

    Row(modifier = Modifier.padding(horizontal = 16.dp)) {
        Box(
            modifier = Modifier
                .width(160.dp)
                .height(240.dp)
                .padding(end = 8.dp)
        ) {
            Image(
                painter = rememberAsyncImagePainter(movieCoverUrl),
                contentDescription = "Portada",
                modifier = Modifier.fillMaxSize()
            )
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
                        fontWeight = FontWeight.Bold // Esto pone el texto en negrita
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
                color = Color.Black,
                modifier = Modifier.padding(start = 16.dp)
            )

            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Genre: ")
                    }
                    append(movieGenre)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black.copy(alpha = 0.7f)
            )


            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Popularity: ")
                    }
                    append(String.format("%.1f", moviePopularity))
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black.copy(alpha = 0.7f)
            )

            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Release Date: ")
                    }
                    append(releaseDate)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black.copy(alpha = 0.7f)
            )

        }
    }

    // Descripción de la película o serie
    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "Description",
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp),
        color = Color.Black
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = movieDescription,
        style = MaterialTheme.typography.bodyMedium,
        color = Color.Black,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp)
    )
}