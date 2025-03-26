package com.example.flixfindertv.ui.screens

import com.example.flixfindertv.utils.YoutubePlayer
import com.example.flixfindertv.utils.ShowComments
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.flixfindertv.R
import com.google.firebase.firestore.FirebaseFirestore
import com.example.flixfindertv.models.Peliculas
import com.example.flixfindertv.ui.viewmodels.CommentsViewModel
import com.example.flixfindertv.ui.viewmodels.UsersViewModel
import com.example.flixfindertv.utils.MovieDetailsContent
import com.google.firebase.auth.FirebaseAuth

@Composable
fun DetailsScreen(navController: NavHostController, id: String, esSerie: Boolean) {
    val usersViewModel: UsersViewModel = viewModel()
    val commentsViewModel: CommentsViewModel = viewModel()

    var movieTitle by remember { mutableStateOf("") }
    var movieDescription by remember { mutableStateOf("") }
    var movieBannerUrl by remember { mutableStateOf("") }
    var movieCoverUrl by remember { mutableStateOf("") }
    var moviePopularity by remember { mutableStateOf(0.0) }
    var movieGenre by remember { mutableStateOf("Cargando...") }
    var releaseDate by remember { mutableStateOf("") }
    var voteAverage by remember { mutableStateOf("") }
    var trailerUrl by remember { mutableStateOf("") }
    var original_language by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    val isDialogOpen = remember { mutableStateOf(false) }
    val selectedStars = remember { mutableStateOf(0) }
    val commentText = remember { mutableStateOf("") }
    var usuarioNombre by remember { mutableStateOf("") }

    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val collectionName = if (esSerie) "series" else "peliculas"

    val errorMessage = remember { mutableStateOf("") }
    var showTrailer by remember { mutableStateOf(false) }

    // Obtener el userId desde FirebaseAuth
    val userId = auth.currentUser?.uid
    LaunchedEffect(userId) {
        if (userId != null) {
            firestore.collection("usuarios").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    usuarioNombre = document.getString("nombre") ?: "Usuario desconocido"
                }
        }
    }

    LaunchedEffect(id) {
        firestore.collection(collectionName)
            .document(id)
            .get()
            .addOnSuccessListener { document ->
                val movie = document.toObject(Peliculas::class.java)
                movieTitle = movie?.title.takeIf { it?.isNotBlank() == true } ?: movie?.name ?: "Título no encontrado"
                movieDescription = movie?.overview?.takeIf { it.isNotBlank() } ?: "No hay descripción"
                original_language = movie?.original_language?.takeIf { it.isNotBlank() } ?: "No hay idioma original"
                status = movie?.status?.takeIf { it.isNotBlank() } ?: "Desconocido"
                movieBannerUrl = if (movie?.backdrop_path?.isNotEmpty() == true) {
                    "https://image.tmdb.org/t/p/w500${movie.backdrop_path}"
                } else {
                    ""
                }
                movieCoverUrl = if (movie?.poster_path?.isNotEmpty() == true) {
                    "https://image.tmdb.org/t/p/w500${movie.poster_path}"
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
                trailerUrl = movie?.trailer.toString()
                usersViewModel.checkIfFavorite(id, esSerie)
            }
        // Obtener comentarios en tiempo real
        commentsViewModel.getComments(id)
    }
    val commentsList by commentsViewModel.comments.collectAsState()

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
                painter = if (movieBannerUrl.isNotEmpty()) {
                    rememberAsyncImagePainter(movieBannerUrl)
                } else {
                    painterResource(id = R.drawable.banner_placeholder) // Imagen predeterminada
                },
                contentDescription = "Banner",
                modifier = Modifier.fillMaxSize()
            )

            // Corazón en la esquina superior derecha
            IconButton(
                onClick = {
                    val isCurrentlyFavorite = usersViewModel.isFavorite.value
                    if (!isCurrentlyFavorite) {
                        // Si no está en favoritos, añadimos a favoritos
                        usersViewModel.saveToFavorites(id, movieTitle, movieCoverUrl, esSerie)
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

        MovieDetailsContent(
            movieCoverUrl = movieCoverUrl,
            movieTitle = movieTitle,
            movieDescription = movieDescription,
            movieGenre = movieGenre,
            moviePopularity = moviePopularity,
            releaseDate = releaseDate,
            voteAverage = voteAverage,
            originalLanguage = original_language,
            status = status
        )

        if (trailerUrl.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            // Obtener el videoId de la URL del tráiler
            val videoId = trailerUrl.split("v=")[1].takeWhile { it != '&' }
            val thumbnailUrl = "https://img.youtube.com/vi/$videoId/hqdefault.jpg"

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(200.dp)
                    .background(Color.Transparent)
            ) {
                // Mostrar la miniatura del video
                Image(
                    painter = rememberAsyncImagePainter(thumbnailUrl),
                    contentDescription = "Trailer Preview",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp) // Mantiene la proporción de un video (relación 16:9)
                        .clickable {
                            // Cuando se hace clic en la miniatura, actualizamos el estado
                            showTrailer = true
                        }
                )

                // Icono de YouTube centrado
                Image(
                    painter = painterResource(id = R.drawable.youtube_icon),
                    contentDescription = "Trailer Preview",
                    modifier = Modifier
                        .size(50.dp) // Ajusta el tamaño del icono
                        .align(Alignment.Center) // Centra el icono dentro de la Box
                        .clickable {
                            // Cuando se hace clic en el ícono, actualizamos el estado
                            showTrailer = true
                        }
                )

                // Mostrar el trailer si el estado lo indica
                if (showTrailer) {
                    ShowTrailer(videoId) // Mostrar el video
                }
            }
        }

        Spacer(modifier = Modifier.height(36.dp))
        Text(
            text = "Comments",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp),
            color = Color.Black
        )

        Button(
            onClick = { isDialogOpen.value = true },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Comment")
        }

        ShowComments(commentsList, commentsViewModel)

        if (isDialogOpen.value) {
            AlertDialog(
                onDismissRequest = {
                    isDialogOpen.value = false
                },
                title = { Text("Write your comment") },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(0.dp), verticalAlignment = Alignment.CenterVertically) {
                            for (i in 1..5) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            selectedStars.value = if (selectedStars.value == i * 2) 0 else i * 2
                                        }
                                ) {
                                    Icon(
                                        imageVector = if (i * 2 <= selectedStars.value) Icons.Filled.Star else Icons.Filled.StarBorder,
                                        contentDescription = "Estrella",
                                        tint = if (i * 2 <= selectedStars.value) Color.Yellow else Color.Gray,
                                        modifier = Modifier.size(50.dp)
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = commentText.value,
                            onValueChange = { commentText.value = it },
                            label = { Text("Write your comment") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                                .padding(top = 16.dp)
                        )

                        // Mostrar el mensaje de error si no se seleccionaron estrellas
                        if (errorMessage.value.isNotEmpty()) {
                            Text(
                                text = errorMessage.value,
                                color = Color.Red,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (selectedStars.value == 0) {
                            // Si no se seleccionan estrellas, mostramos el mensaje de error
                            errorMessage.value = "Por favor selecciona al menos una estrella."
                        } else if (commentText.value.isBlank()) {
                            // Si el comentario está vacío, mostramos el mensaje de error
                            errorMessage.value = "Por favor escribe un comentario."
                        } else if (userId != null) {
                            // Llamamos al ViewModel para enviar el comentario
                            commentsViewModel.sendComment(id, usuarioNombre, selectedStars.value, commentText.value)
                            // Reseteamos las estrellas y el texto del comentario después de enviar el comentario
                            selectedStars.value = 0
                            commentText.value = ""
                            errorMessage.value = "" // Limpiamos el mensaje de error
                            isDialogOpen.value = false
                        }
                    }) {
                        Text("Send")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        isDialogOpen.value = false
                        commentText.value = ""
                        selectedStars.value = 0
                        errorMessage.value = "" // Limpiamos el mensaje de error
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun ShowTrailer(videoId: String) {
    // YouTube player
    YoutubePlayer(videoId)
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
