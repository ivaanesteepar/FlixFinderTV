package com.example.flixfindertv.utils

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.flixfindertv.R
import com.example.flixfindertv.models.Peliculas

@Composable
fun MovieCard(movie: Peliculas, navController: NavHostController) {
    val isSerie = movie.esSerie
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable {
                navController.navigate("detalles/${movie.id}/${isSerie}")
            },
        shape = MaterialTheme.shapes.small.copy(CornerSize(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = if (isSerie) Color(0xFF4DB6AC) else Color(0xFF42A5F5) // Cambia los colores si es necesario
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val imagePainter = if (movie.poster_path != null) {
                rememberAsyncImagePainter("https://image.tmdb.org/t/p/w500${movie.poster_path}")
            } else {
                rememberAsyncImagePainter(R.drawable.no_poster_image)  // Imagen local si no hay poster_path
            }

            // Cargar la imagen
            Image(
                painter = imagePainter,
                contentDescription = "Portada de ${movie.title}",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))
            val displayTitle = movie.title ?: movie.name ?: "Título no disponible"
            Text(
                text = displayTitle,
                style = MaterialTheme.typography.titleMedium,
                fontSize = 18.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = movie.overview.ifBlank { "No hay descripción disponible" },
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 14.sp,
                color = Color.Black,
                maxLines = 6,  // Limita el texto a 6 líneas
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis // Agrega "..." al final si el texto es más largo
            )

        }
    }
}

