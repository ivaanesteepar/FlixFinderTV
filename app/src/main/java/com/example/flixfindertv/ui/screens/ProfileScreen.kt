package com.example.flixfindertv.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.material3.Text
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Modifier
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.flixfindertv.R
import com.example.flixfindertv.models.Peliculas
import com.example.flixfindertv.room.database.AppDatabase
import com.example.flixfindertv.room.repository.MovieRepository
import com.example.flixfindertv.ui.viewmodels.ConexionViewModel
import com.example.flixfindertv.ui.viewmodels.UsersViewModel
import com.example.flixfindertv.utils.BottomNavigationBar


@Composable
fun UserAvatar(userProfilePic: String) {
    Box(
        modifier = Modifier
            .padding(top = 24.dp)
            .size(150.dp)
            .background(Color.White, shape = CircleShape)
            .border(2.dp, Color.Black, shape = CircleShape)
    ) {
        val modifier = Modifier
            .fillMaxSize()
            .align(Alignment.Center)
            .clip(CircleShape)
            .border(5.dp, Color.White, CircleShape)
        if (userProfilePic.isEmpty()) {
            Image(
                painter = painterResource(id = R.drawable.no_profile_icon),
                contentDescription = "Sin icono",
                modifier = modifier,
                contentScale = ContentScale.Crop
            )
        } else {
            Image(
                painter = rememberAsyncImagePainter(userProfilePic),
                contentDescription = "Foto de perfil",
                modifier = modifier,
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun FavoriteItemCard(
    item: Peliculas,
    isSerie: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter("https://image.tmdb.org/t/p/w500${item.poster_path}"),
                    contentDescription = "Imagen",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isSerie) Color(0xFF4DB6AC) else Color(0xFF42A5F5))
                    .padding(4.dp)
            ) {
                Text(
                    text = item.titulo,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}

@Composable
fun FavouritesSection(
    title: String,
    items: List<Peliculas>,
    noFavoritesMessage: String,
    onItemClick: (Peliculas) -> Unit,
    onViewMore: () -> Unit,
    isSerie: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
            modifier = Modifier.align(Alignment.Start) // Alineado a la izquierda
        )
        Spacer(modifier = Modifier.height(15.dp))
        if (items.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items.take(3).forEach { item ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(120.dp)
                    ) {
                        FavoriteItemCard(item, isSerie) { onItemClick(item) }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "View More",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                    modifier = Modifier.clickable { onViewMore() }
                )
            }
        } else {
            Text(
                text = noFavoritesMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}

// Pantalla que muestra el perfil del usuario
@Composable
fun ProfileScreen(navController: NavController, uid: String, isComment: Boolean) {
    val auth = FirebaseAuth.getInstance()
    val usersViewModel: UsersViewModel = viewModel()
    val conexionViewModel: ConexionViewModel = viewModel()
    val hayConexion by conexionViewModel.conexionEstablecida.collectAsState()

    var userName by remember { mutableStateOf("Unknown User") }
    var userProfilePic by remember { mutableStateOf("") }
    var followingCount by remember { mutableStateOf(0) }
    var followersCount by remember { mutableStateOf(0) }
    var commentsCount by remember { mutableStateOf(0) }
    var favoriteMovies by remember { mutableStateOf<List<Peliculas>>(emptyList()) }
    var favoriteSeries by remember { mutableStateOf<List<Peliculas>>(emptyList()) }
    val context = LocalContext.current
    val currentUser = auth.currentUser
    val activity = context as? Activity
    val isFollowing = remember { mutableStateOf(false) }
    var favoriteMoviesOffline by remember { mutableStateOf<List<Peliculas>>(emptyList()) }
    var favoriteSeriesOffline by remember { mutableStateOf<List<Peliculas>>(emptyList()) }

    val usuario by usersViewModel.usuarioState.collectAsState()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val movieDao = AppDatabase.getDatabase(context).movieDao()
    val repository = MovieRepository(movieDao)

    var previousUid by rememberSaveable { mutableStateOf<String?>(null) }


    BackHandler {
        if (!isComment) {
            activity?.finish()
        } else {
            navController.popBackStack()
        }
    }

    LaunchedEffect(Unit) {
        if (userId != null && previousUid != userId) {
            usersViewModel.cargarFavoritasDesdeFirestore(
                userId = userId,
                repository = repository
            )
            previousUid = userId
        }
    }

    if (currentUser != null) {
        val currentUid = currentUser.uid

        LaunchedEffect(usuario) {
            usuario?.let { user ->
                userName = user.nombre
                userProfilePic = user.fotoPerfil ?: ""
                followingCount = user.siguiendo.size
                followersCount = user.seguidores.size
                commentsCount = user.numComentarios
            }
        }

        LaunchedEffect(uid) {
            usersViewModel.startListening(uid)
            usersViewModel.checkIfFollowing(currentUid, uid) { following ->
                isFollowing.value = following
            }
        }

        LaunchedEffect(key1 = uid) {
            usersViewModel.getFavoriteMovies(uid, onSuccess = { peliculasFavoritas ->
                favoriteMovies = peliculasFavoritas
            }, onFailure = { exception ->
                Toast.makeText(context, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            })

            usersViewModel.getFavoriteSeries(uid, onSuccess = { seriesFavoritas ->
                favoriteSeries = seriesFavoritas
            }, onFailure = { exception ->
                Toast.makeText(context, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            })

            usersViewModel.getPeliculasFavoritasDesdeRoom()
            usersViewModel.getSeriesFavoritasDesdeRoom()
        }

        favoriteMoviesOffline = usersViewModel.favouriteMovies.value
        favoriteSeriesOffline = usersViewModel.favouriteSeries.value

        Scaffold(
            bottomBar = {
                if (!isComment) {
                    BottomNavigationBar(navController, uid)
                }
            }
        ) { paddingValues ->
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isComment) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            IconButton(
                                onClick = {
                                    navController.popBackStack()
                                },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Volver atrás",
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    UserAvatar(userProfilePic)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    if (!isComment) {
                        Button(
                            modifier = Modifier.fillMaxWidth(0.3f),
                            onClick = {
                                navController.navigate("edit_profile")
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD3D3D3)
                            )
                        ) {
                            Text(
                                text = "Edit",
                                color = Color.Black
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = if (uid == currentUid || isFollowing.value) {
                                Modifier.clickable {
                                    navController.navigate("users_list/$uid/true")
                                }
                            } else Modifier,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(text = "Following", color = Color.White)
                            Spacer(modifier = Modifier.height(5.dp))
                            Text(
                                text = followingCount.toString(),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White
                            )
                        }
                        Column(
                            modifier = if (uid == currentUid || isFollowing.value) {
                                Modifier.clickable {
                                    navController.navigate("users_list/$uid/false")
                                }
                            } else Modifier,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(text = "Followers", color = Color.White)
                            Spacer(modifier = Modifier.height(5.dp))
                            Text(
                                text = followersCount.toString(),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White
                            )
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "Comments", color = Color.White)
                            Spacer(modifier = Modifier.height(5.dp))
                            Text(
                                text = commentsCount.toString(),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(25.dp))
                    if (uid != currentUid) {
                        Button(
                            onClick = {
                                if (hayConexion) {
                                    if (isFollowing.value) {
                                        usersViewModel.unfollowUser(currentUid, uid,
                                            onSuccess = {
                                                Toast.makeText(
                                                    context,
                                                    "You have unfollowed this user",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                isFollowing.value = false
                                            },
                                            onFailure = { exception ->
                                                Toast.makeText(
                                                    context,
                                                    "Error: ${exception.message}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        )
                                    } else {
                                        usersViewModel.followUser(currentUid, uid,
                                            onSuccess = {
                                                Toast.makeText(
                                                    context,
                                                    "You are now following this user!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                isFollowing.value = true
                                            },
                                            onFailure = { exception ->
                                                Toast.makeText(
                                                    context,
                                                    "Error: ${exception.message}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        )
                                    }
                                } else {
                                    Toast.makeText(
                                        context,
                                        "You need an internet connection to follow/unfollow an user",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isFollowing.value) Color(0xFF87CEEB) else Color.Blue
                            )
                        ) {
                            Text(
                                text = if (isFollowing.value) "Following" else "Follow",
                                color = Color.White
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(25.dp))

                    // SECCIÓN: Favourite Movies
                    FavouritesSection(
                        title = "Favourite Movies",
                        items = if (hayConexion) favoriteMovies else favoriteMoviesOffline,
                        noFavoritesMessage = if (uid == currentUid) "You don't have favourite movies" else "This user has no favourite movies",
                        onItemClick = { movie -> navController.navigate("detalles/${movie.id}/false") { launchSingleTop = true } },
                        onViewMore = { navController.navigate("favourite_movies/$uid/false") { popUpTo("favourite_movies") { inclusive = true }; launchSingleTop = true } },
                        isSerie = false
                    )

                    // SECCIÓN: Favourite TV Shows
                    FavouritesSection(
                        title = "Favourite TV shows",
                        items = if (hayConexion) favoriteSeries else favoriteSeriesOffline,
                        noFavoritesMessage = if (uid == currentUid) "You don't have favourite TV shows" else "This user has no favourite TV shows",
                        onItemClick = { series -> navController.navigate("detalles/${series.id}/true") { launchSingleTop = true } },
                        onViewMore = { navController.navigate("favourite_movies/$uid/true") { popUpTo("favourite_movies") { inclusive = true }; launchSingleTop = true } },
                        isSerie = true
                    )

                    if (currentUser == null) {
                        Text(
                            text = "You must log in to access this section",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Red
                        )
                    } else {
                        if (!isComment) {
                            var errorMessage by remember { mutableStateOf("") }
                            Button(
                                onClick = {
                                    if (!hayConexion) {
                                        Toast.makeText(
                                            context,
                                            "No internet connection. Logout operation could not be completed.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        return@Button
                                    }
                                    try {
                                        auth.signOut()
                                        usersViewModel.saveSession(
                                            context,
                                            false,
                                            uid
                                        )
                                        Toast.makeText(
                                            context,
                                            "Session closed",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        navController.navigate("login")
                                    } catch (e: Exception) {
                                        errorMessage = "Error: ${e.message}"
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                            ) {
                                Text(
                                    text = "Log out",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.White
                                )
                            }
                            if (errorMessage.isNotEmpty()) {
                                Text(
                                    text = errorMessage,
                                    color = Color.Red,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}