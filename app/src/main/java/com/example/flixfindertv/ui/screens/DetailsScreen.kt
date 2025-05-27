package com.example.flixfindertv.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.flixfindertv.R
import com.google.firebase.firestore.FirebaseFirestore
import com.example.flixfindertv.models.Peliculas
import com.example.flixfindertv.ui.viewmodels.CommentsViewModel
import com.example.flixfindertv.ui.viewmodels.ConexionViewModel
import com.example.flixfindertv.ui.viewmodels.GenresViewModel
import com.example.flixfindertv.ui.viewmodels.MoviesViewModel
import com.example.flixfindertv.ui.viewmodels.UsersViewModel
import com.example.flixfindertv.utils.MovieDetailsContent
import com.example.flixfindertv.utils.validateComment
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

// Función que muestra la pantalla de detalles de una película o serie
@Composable
fun DetailsScreen(navController: NavHostController, id: String, esSerie: Boolean) {
    val usersViewModel: UsersViewModel = viewModel()
    val conexionViewModel: ConexionViewModel = viewModel()
    val commentsViewModel: CommentsViewModel = viewModel()
    val moviesViewModel: MoviesViewModel = viewModel()
    val genresViewModel: GenresViewModel = viewModel()
    var movie by remember { mutableStateOf<Peliculas?>(null) }
    var movieId by remember { mutableStateOf("") }
    var movieTitle by remember { mutableStateOf("") }
    var movieDescription by remember { mutableStateOf("") }
    var movieBannerUrl by remember { mutableStateOf<String?>(null) }
    var movieCoverUrl by remember { mutableStateOf("") }
    var moviePopularity by remember { mutableStateOf(0.0) }
    var voteCount by remember { mutableStateOf("") }
    var movieGenre by remember { mutableStateOf("") }
    var releaseDate by remember { mutableStateOf("") }
    var voteAverage by remember { mutableStateOf("") }
    var trailerUrl by remember { mutableStateOf("") }
    var original_language by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    var director by remember { mutableStateOf("") }
    var directorPhoto by remember { mutableStateOf("") }
    val isDialogOpen = remember { mutableStateOf(false) }
    val selectedStars = remember { mutableStateOf(0) }
    val commentText = remember { mutableStateOf("") }
    var usuarioNombre by remember { mutableStateOf("") }

    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val collectionName = if (esSerie) "series" else "peliculas"
    val hayConexion by conexionViewModel.conexionEstablecida.collectAsState()

    val errorMessage = remember { mutableStateOf("") }
    var showTrailer by remember { mutableStateOf(false) }
    val context = LocalContext.current // Para mostrar el Toast
    var isUpdating = false
    val snackbarHostState = remember { SnackbarHostState() }
    val showOffensiveSnackbar = remember { mutableStateOf(false) }


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

    LaunchedEffect(showOffensiveSnackbar.value) {
        if (showOffensiveSnackbar.value) {
            // Muestra el Snackbar con duración larga (7 segundos)
            snackbarHostState.showSnackbar(
                message = "Tu mensaje será revisado antes de publicarse.",
                duration = SnackbarDuration.Long // 7 segundos
            )
            // Cuando el Snackbar termine, actualiza el estado
            showOffensiveSnackbar.value = false
        }
    }

    LaunchedEffect(id) {
        firestore.collection(collectionName)
            .document(id)
            .get()
            .addOnSuccessListener { document ->
                movie = document.toObject(Peliculas::class.java)
                movie?.let {
                    movieId = it.id
                    movieTitle = it.title ?: it.name ?: ""
                    movieDescription = it.overview
                    original_language = it.original_language
                    status = it.status
                    voteCount = it.vote_count

                    movieBannerUrl = it.backdrop_path?.takeIf { path -> path.isNotEmpty() }
                        ?.let { path -> "https://image.tmdb.org/t/p/w500$path" }


                    movieCoverUrl = it.poster_path?.takeIf { path -> path.isNotEmpty() }
                        ?.let { path -> "https://image.tmdb.org/t/p/w500$path" } ?: ""


                    moviePopularity = it.popularity
                    director = it.director_name ?: ""
                    directorPhoto = it.director_photo_url ?: ""

                    val genreIds = it.genre_ids
                    if (genreIds.isNotEmpty()) {
                        genresViewModel.fetchGenreNames(genreIds) { genres ->
                            movieGenre = genres.joinToString(", ")
                        }
                    } else {
                        movieGenre = "Genre not available"
                    }

                    releaseDate = if (esSerie) {
                        it.release_date_series ?: "Date not available"
                    } else {
                        it.release_date ?: "Date not available"
                    }

                    voteAverage = it.vote_average
                    trailerUrl = it.trailer.toString()

                    usersViewModel.checkIfFavorite(id, esSerie)
                }
            }
        commentsViewModel.getComments(id)
    }
    val commentsList by commentsViewModel.comments.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.fondo_app),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            // Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            ) {
                Image(
                    painter = if (!movieBannerUrl.isNullOrEmpty()) {
                        rememberAsyncImagePainter(model = movieBannerUrl)
                    } else {
                        painterResource(id = R.drawable.banner_placeholder)
                    },
                    contentDescription = "Banner",
                    modifier = Modifier.fillMaxSize()
                )

                println("valor hayConexion aqui: $hayConexion")
                if (hayConexion) {
                    // Corazón en la esquina superior derecha
                    IconButton(
                        onClick = {
                            val isCurrentlyFavorite = usersViewModel.isFavorite.value
                            if (!isCurrentlyFavorite) {
                                // Si no está en favoritos, añadimos a favoritos
                                usersViewModel.saveToFavorites(
                                    context,
                                    id,
                                    movieTitle,
                                    movieCoverUrl,
                                    esSerie
                                )
                                movie?.let { usersViewModel.saveToLocalFavorites(context, it) }
                                usersViewModel.updateFavoriteGenre(movieGenre)
                            } else {
                                // Si ya está en favoritos, lo eliminamos de favoritos
                                usersViewModel.removeFromFavorites(id, esSerie)
                                movie?.let { usersViewModel.removeFromLocalFavorites(it) }
                            }
                            // Asegurarnos de que el estado del corazón se actualice inmediatamente
                            usersViewModel.checkIfFavorite(id, esSerie)
                        },
                        modifier = Modifier
                            .padding(16.dp) // Ajusta el espacio alrededor del icono
                            .align(Alignment.TopEnd) // Posiciona el botón en la parte superior derecha
                            .background(
                                Color.Gray.copy(alpha = 0.5f),
                                CircleShape
                            ) // Fondo gris con forma circular
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
                }

                // Botón para volver atrás en la esquina superior izquierda dentro de un círculo gris
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .padding(16.dp) // Ajusta el espacio alrededor del icono
                        .align(Alignment.TopStart) // Posiciona el botón en la parte superior izquierda
                        .background(
                            Color.Gray.copy(alpha = 0.5f),
                            CircleShape
                        ) // Fondo gris con forma circular
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
                movieId = movieId,
                movieCoverUrl = movieCoverUrl,
                movieTitle = movieTitle,
                movieDescription = movieDescription,
                movieGenre = movieGenre,
                releaseDate = releaseDate,
                originalLanguage = original_language,
                status = status,
                director = director,
                directorPhoto = directorPhoto
            )
            if (hayConexion) {
                Spacer(modifier = Modifier.height(16.dp))
                // Verificar si trailerUrl contiene un videoId válido
                val videoId = trailerUrl.split("v=").getOrNull(1)?.takeWhile { it != '&' }
                if (videoId != null) {
                    val thumbnailUrl = "https://img.youtube.com/vi/$videoId/hqdefault.jpg"

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(200.dp)
                            .background(Color.Transparent)
                    ) {
                        // Mostrar la miniatura del video solo si showTrailer es false
                        if (!showTrailer) {
                            Image(
                                painter = rememberAsyncImagePainter(thumbnailUrl),
                                contentDescription = "Trailer Preview",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(210.dp) // Mantiene la proporción de un video (relación 16:9)
                                    .clickable {
                                        // Cuando se hace clic en la miniatura, actualizamos el estado para mostrar el trailer
                                        showTrailer = true
                                    }
                            )
                        }

                        // Icono de YouTube centrado
                        Image(
                            painter = painterResource(id = R.drawable.youtube_icon),
                            contentDescription = "Trailer Preview",
                            modifier = Modifier
                                .size(50.dp) // Ajusta el tamaño del icono
                                .align(Alignment.Center) // Centra el icono dentro de la Box
                                .clickable {
                                    // Cuando se hace clic en el ícono, actualizamos el estado para mostrar el trailer
                                    showTrailer = true
                                }
                        )

                        // Mostrar el trailer si el estado lo indica
                        if (showTrailer) {
                            ShowTrailer(videoId) // Mostrar el video
                        }
                    }
                } else {
                    // Si no se obtiene un videoId válido, muestra un mensaje adecuado
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No trailer available",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .padding(top = 26.dp)
                ) {
                    Text(
                        text = "To watch the trailer, you need an internet connection",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
            Spacer(modifier = Modifier.height(36.dp))
            Text(
                text = "Comments",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                color = Color.White
            )
            if (hayConexion) {
                Button(
                    onClick = { isDialogOpen.value = true },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Comment")
                }

                // Se ejecutará la función siempre que cualquiera de los parametros cambie, en este caso commentsList
                movie?.let { ShowComments(navController, commentsList, commentsViewModel) }

            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .padding(bottom = 20.dp)
                ) {
                    Text(
                        text = "To view the comments, you need an internet connection",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
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
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(0.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                for (i in 1..5) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                selectedStars.value =
                                                    if (selectedStars.value == i * 2) 0 else i * 2
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
                        val coroutineScope = rememberCoroutineScope()
                        TextButton(onClick = {
                            if (selectedStars.value == 0) {
                                errorMessage.value = "Please select at least one star."
                            } else if (commentText.value.isBlank()) {
                                errorMessage.value = "Please write a comment."
                            }
                            else if (userId != null) {
                                coroutineScope.launch {
                                    val isOffensive = validateComment(commentText.value)
                                    if (isOffensive) {
                                        val toast = Toast.makeText(
                                            context,
                                            "Offensive message detected. Your comment will be reviewed.",
                                            Toast.LENGTH_LONG
                                        )
                                        toast.show()
                                        commentsViewModel.sendComment(
                                            id,
                                            usuarioNombre,
                                            selectedStars.value,
                                            commentText.value,
                                            true
                                        )
                                    } else {
                                        commentsViewModel.sendComment(
                                            id,
                                            usuarioNombre,
                                            selectedStars.value,
                                            commentText.value,
                                            false
                                        )
                                    }
                                    // Aumentar el contador de comentarios en Firebase
                                    moviesViewModel.incrementUserCommentCount(userId)

                                    // Si le damos una buena puntuación (por lo menos 3 estrellas), recomendamos el genero
                                    if (selectedStars.value > 6) {
                                        usersViewModel.updateFavoriteGenre(movieGenre)
                                    }

                                    moviesViewModel.calculateNewVoteAverage(
                                        movieId,
                                        selectedStars.value
                                    ) { updatedVoteAverage ->
                                        if (updatedVoteAverage != null) {
                                            // Si se obtiene un nuevo promedio válido, actualizamos en Firebase
                                            if (!isUpdating) {
                                                isUpdating =
                                                    true  // Evitamos que se actualicen los valores mientras estamos en el proceso de actualización

                                                // Convertimos el String a Float para pasarlo a updateVoteAverageInFirebase
                                                val updatedVoteAverageFloat =
                                                    updatedVoteAverage.toFloatOrNull()

                                                if (updatedVoteAverageFloat != null) {
                                                    // Actualizamos el promedio de votos en Firebase
                                                    moviesViewModel.updateVoteAverageInFirebase(
                                                        movieId,
                                                        updatedVoteAverageFloat
                                                    )

                                                    // También actualizamos la popularidad
                                                    moviesViewModel.calculateNewPopularity(movieId) { newPopularity ->
                                                        if (newPopularity != null) {
                                                            // Actualizamos la popularidad en Firebase
                                                            moviesViewModel.updatePopularityInFirebase(
                                                                movieId,
                                                                newPopularity
                                                            )
                                                        }
                                                    }
                                                } else {
                                                    // Si no se puede convertir el String a Float, manejar el error
                                                    Toast.makeText(
                                                        context,
                                                        "Error converting average to number",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        } else {
                                            // Si no se pudo calcular el nuevo promedio, manejar el error
                                            Toast.makeText(
                                                context,
                                                "Error calculating the average vote",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }

                                        // Restablecemos el flag después de la actualización
                                        isUpdating = false
                                    }

                                    // Cerrar el diálogo en ambos casos
                                    isDialogOpen.value = false
                                    selectedStars.value = 0
                                    commentText.value = ""
                                    errorMessage.value = ""
                                }
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
                            errorMessage.value = ""
                        }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ShowTrailer(videoId: String) {
    YoutubePlayer(videoId)
}



