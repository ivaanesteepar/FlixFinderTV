package com.example.flixfindertv.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.FirebaseFirestore
import com.example.flixfindertv.models.Peliculas
import com.example.flixfindertv.ui.viewmodels.UsersViewModel
import com.example.flixfindertv.utils.MovieDetailsContent

@Composable
fun DetailsScreen(navController: NavHostController, id: String, esSerie: Boolean) {
    val usersViewModel: UsersViewModel = viewModel()
    var movieTitle by remember { mutableStateOf("") }
    var movieDescription by remember { mutableStateOf("") }
    var movieBannerUrl by remember { mutableStateOf("") }
    var movieCoverUrl by remember { mutableStateOf("") }
    var moviePopularity by remember { mutableStateOf(0.0) }
    var movieGenre by remember { mutableStateOf("Cargando...") }
    var releaseDate by remember { mutableStateOf("") }
    var voteAverage by remember { mutableStateOf("") }
    val isDialogOpen = remember { mutableStateOf(false) }
    val selectedStars = remember { mutableStateOf(0) }
    val commentText = remember { mutableStateOf("") }

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

                // Verificar si está en los favoritos
                usersViewModel.checkIfFavorite(id, esSerie)
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

            // Corazón en la esquina superior derecha
            IconButton(
                onClick = {
                    val isCurrentlyFavorite = usersViewModel.isFavorite.value
                    if (!isCurrentlyFavorite) {
                        // Si no está en favoritos, añadimos a favoritos
                        usersViewModel.saveToFavorites(id, movieTitle, movieDescription, movieCoverUrl, esSerie)
                    } else {
                        // Si ya está en favoritos, lo eliminamos de favoritos
                        usersViewModel.removeFromFavorites(id, esSerie)
                    }
                    // Asegurarnos de que el estado del corazón se actualice inmediatamente
                    usersViewModel.checkIfFavorite(id, esSerie)
                },
                modifier = Modifier
                    .padding(16.dp) // Ajusta el espacio alrededor del icono
                    .align(Alignment.TopEnd) // Posiciona el botón en la parte superior derecha
                    .background(Color.Gray.copy(alpha = 0.5f), CircleShape) // Fondo gris con forma circular
                    .size(50.dp)
                    .padding(8.dp) // Espacio alrededor del icono dentro del círculo
            ) {
                // Icono que cambia de color dependiendo de si está en favoritos
                Icon(
                    imageVector = if (usersViewModel.isFavorite.value) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Favorito",
                    tint = if (usersViewModel.isFavorite.value) Color.Red else Color.White // Rojo si está favorito, blanco si no
                )
            }

            // Botón para volver atrás en la esquina superior izquierda dentro de un círculo gris
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .padding(16.dp) // Ajusta el espacio alrededor del icono
                    .align(Alignment.TopStart) // Posiciona el botón en la parte superior izquierda
                    .background(Color.Gray.copy(alpha = 0.5f), CircleShape) // Fondo gris con forma circular
                    .size(50.dp)
                    .padding(8.dp) // Espacio alrededor del icono dentro del círculo
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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

        // Comentarios y demás contenido
        Spacer(modifier = Modifier.height(36.dp))
        Text(
            text = "Comments",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp),
            color = Color.Black
        )

        // Botón para abrir el dialogo de comentar
        Button(
            onClick = { isDialogOpen.value = true },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Comment")
        }

        // Mostrar el diálogo de comentario
        if (isDialogOpen.value) {
            AlertDialog(
                onDismissRequest = {
                    isDialogOpen.value = false
                    commentText.value = ""
                    selectedStars.value = 0
                },
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(), // Hace que el Box ocupe todo el ancho disponible
                        contentAlignment = Alignment.Center // Centra el contenido dentro del Box
                    ) {
                        Text("Leave your comment")
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth() // Hace que el diálogo ocupe todo el ancho
                            .padding(16.dp) // Añade padding al contenido
                    ) {
                        // Estrellas
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(0.dp), // Espaciado cero
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (i in 1..5) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .weight(1f) // Hace que las estrellas se distribuyan equitativamente
                                        .clickable {
                                            // Si la estrella que se ha pulsado es la misma que la seleccionada,
                                            // desmarcar todas las estrellas
                                            selectedStars.value = if (selectedStars.value == i * 2) 0 else i * 2
                                            println("selectedStar: ${selectedStars.value}")
                                        }
                                ) {
                                    Icon(
                                        imageVector = if (i * 2 <= selectedStars.value) Icons.Filled.Star else Icons.Filled.StarBorder,
                                        contentDescription = "Estrella",
                                        tint = if (i * 2 <= selectedStars.value) Color.Yellow else Color.Gray,
                                        modifier = Modifier.size(50.dp) // Tamaño de las estrellas más grande
                                    )
                                }
                            }
                        }

                        // Campo de texto para el comentario
                        OutlinedTextField(
                            value = commentText.value,
                            onValueChange = { commentText.value = it },
                            label = { Text("Write your comment") },
                            modifier = Modifier
                                .fillMaxWidth() // Ancho completo
                                .height(250.dp) // Aumenta la altura del campo de texto
                                .padding(top = 16.dp) // Padding superior
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        // Aquí puedes guardar el comentario y la calificación
                        isDialogOpen.value = false
                    }) {
                        Text("Send")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        isDialogOpen.value = false
                        commentText.value = "" // Reseteamos el texto cuando se cancela
                        selectedStars.value = 0
                    }) {
                        Text("Cancel")
                    }
                }
            )

        }


    }
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
