package com.example.flixfindertv.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.FirebaseFirestore
import com.example.flixfindertv.models.Peliculas

@Composable
fun DetailsScreen(navController: NavHostController, id: String, esSerie: Boolean) {
    var movieTitle by remember { mutableStateOf("") }
    var movieDescription by remember { mutableStateOf("") }
    var movieBannerUrl by remember { mutableStateOf("") }
    var movieCoverUrl by remember { mutableStateOf("") }
    var moviePopularity by remember { mutableStateOf(0.0) }
    var movieGenre by remember { mutableStateOf("Cargando...") }
    var releaseDate by remember { mutableStateOf("") }
    var voteAverage by remember { mutableStateOf("") }

    // Estado para mostrar el dialogo de calificación
    var showRatingDialog by remember { mutableStateOf(false) }
    var ratingValue by remember { mutableStateOf(0) } // Valor de la calificación sobre 10
    var commentText by remember { mutableStateOf("") }

    // Estado para mostrar el diálogo de respuesta
    var showReplyDialog by remember { mutableStateOf(false) }
    var replyText by remember { mutableStateOf("") }

    // Base URL para TMDb
    val imageBaseUrl = "https://image.tmdb.org/t/p/w500"
    val firestore = FirebaseFirestore.getInstance()
    val collectionName = if (esSerie) "series" else "peliculas"

    // Cargar los detalles de la película o serie
    LaunchedEffect(id) {
        firestore.collection(collectionName)
            .document(id)
            .get()
            .addOnSuccessListener { document ->
                val movie = document.toObject(Peliculas::class.java)
                movieTitle = movie?.title.takeIf { it?.isNotBlank() == true } ?: movie?.name ?: "Título no encontrado"
                movieDescription = movie?.overview?.takeIf { it.isNotBlank() } ?: "No hay descripción"
                movieBannerUrl = if (movie?.backdrop_path?.isNotEmpty() == true) {
                    "$imageBaseUrl${movie.backdrop_path}"
                } else {
                    ""
                }
                movieCoverUrl = if (movie?.poster_path?.isNotEmpty() == true) {
                    "$imageBaseUrl${movie.poster_path}"
                } else {
                    ""
                }
                moviePopularity = movie?.popularity ?: 0.0
                val genreIds = movie?.genre_ids ?: emptyList()
                if (genreIds.isNotEmpty()) {
                    fetchGenreNames(genreIds) { genres ->
                        movieGenre = if (genres.isNotEmpty()) genres.joinToString(", ") else "Género no disponible"
                    }
                } else {
                    movieGenre = "Género no disponible"
                }
                releaseDate = if (esSerie) {
                    movie?.release_date_series ?: "Fecha no disponible"
                } else {
                    movie?.release_date ?: "Fecha no disponible"
                }
                voteAverage = movie?.vote_average ?: "N/A"
            }
            .addOnFailureListener { e ->
                movieGenre = "Error al cargar géneros"
                releaseDate = "Error al cargar fecha"
                voteAverage = "Error al cargar calificación"
            }
    }

    // Pantalla con Scroll
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        // Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        ) {
            // Imagen del banner
            Image(
                painter = rememberAsyncImagePainter(movieBannerUrl),
                contentDescription = "Banner",
                modifier = Modifier.fillMaxSize()
            )

            // Botón para volver atrás en la esquina superior izquierda dentro de un círculo gris
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .padding(16.dp) // Ajusta el espacio alrededor del icono
                    .align(Alignment.TopStart) // Posiciona el botón en la parte superior izquierda
                    .background(Color.Gray, CircleShape) // Fondo gris con forma circular
                    .size(50.dp)
                    .padding(8.dp) // Espacio alrededor del icono dentro del círculo
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White // Cambia el color del icono a blanco
                )
            }
        }

        // Contenido principal (Portada, Título, Descripción, Fecha de lanzamiento, Promedio de votos)
        MovieDetailsContent(
            movieCoverUrl = movieCoverUrl,
            movieTitle = movieTitle,
            movieDescription = movieDescription,
            movieGenre = movieGenre,
            moviePopularity = moviePopularity,
            releaseDate = releaseDate,
            voteAverage = voteAverage
        )

        // Comentarios
        Spacer(modifier = Modifier.height(36.dp))
        Text(
            text = "Comments",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp),
            color = Color.Black
        )

        // Lista de comentarios
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(8.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                repeat(5) { index -> // Reemplaza este bloque con tus datos reales
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp), // Espacio entre tarjetas
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Comentario #${index + 1}: Este es un comentario de ejemplo123456.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Black
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Ícono de respuesta
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Reply,
                                    contentDescription = "Responder",
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable { showReplyDialog = true }, // Al hacer clic, mostrar el diálogo
                                    tint = Color.Blue
                                )
                            }
                        }
                    }
                }
            }
        }

        // Mostrar el AlertDialog para responder
        if (showReplyDialog) {
            AlertDialog(
                onDismissRequest = { showReplyDialog = false },
                title = { Text("Escribe tu respuesta") },
                text = {
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        label = { Text("Respuesta") },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        // Lógica para manejar la respuesta (podrías agregar la respuesta a la base de datos)
                        showReplyDialog = false
                    }) {
                        Text("Enviar")
                    }
                },
                dismissButton = {
                    Button(onClick = { showReplyDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

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


fun fetchGenreNames(genreIds: List<Int>, onResult: (List<String>) -> Unit) {
    val firestore = FirebaseFirestore.getInstance()
    val genreNames = mutableListOf<String>()
    var count = 0

    if (genreIds.isEmpty()) {
        onResult(emptyList())
        return
    }

    genreIds.forEach { genreId ->
        firestore.collection("generos").document(genreId.toString()).get()
            .addOnSuccessListener { document ->
                val genreName = document.getString("name")
                genreName?.let {
                    genreNames.add(it)
                }
            }
            .addOnCompleteListener {
                count++
                if (count == genreIds.size) {
                    onResult(genreNames)
                }
            }
    }
}
