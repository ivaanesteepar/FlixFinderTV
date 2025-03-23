package com.example.flixfindertv.ui.screens

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.flixfindertv.models.Usuarios
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberImagePainter
import com.example.flixfindertv.R
import com.example.flixfindertv.models.Peliculas
import com.example.flixfindertv.ui.viewmodels.UsersViewModel
import com.example.flixfindertv.utils.BottomNavigationBar
import com.example.flixfindertv.utils.MovieList

@Composable
fun ProfileScreen(navController: NavHostController, uid: String) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val usersViewModel: UsersViewModel = viewModel()

    var userName by remember { mutableStateOf("Usuario desconocido") }
    var userProfilePic by remember { mutableStateOf("") }
    var followingCount by remember { mutableStateOf(0) }
    var followersCount by remember { mutableStateOf(0) }
    var commentsCount by remember { mutableStateOf(0) }
    var favoriteMovies by remember { mutableStateOf<List<Peliculas>>(emptyList()) }
    var favoriteSeries by remember { mutableStateOf<List<Peliculas>>(emptyList()) }
    val context = LocalContext.current
    val currentUser = auth.currentUser


    if (currentUser != null) {
        val currentUid = currentUser.uid

        // Recuperar los datos del usuario desde Firestore
        LaunchedEffect(uid) {
            firestore.collection("usuarios")
                .document(uid)
                .addSnapshotListener { documentSnapshot, exception ->
                    if (exception != null) {
                        Toast.makeText(
                            context,
                            "Error al obtener datos: ${exception.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@addSnapshotListener
                    }

                    documentSnapshot?.let { document ->
                        if (document.exists()) {
                            val usuario = document.toObject(Usuarios::class.java)
                            usuario?.let { usuarioData ->
                                userName = usuarioData.nombre ?: "Usuario desconocido"
                                userProfilePic = usuarioData.fotoPerfil ?: ""

                                // Actualizamos los valores de las listas y comentarios
                                followingCount = usuarioData.siguiendo.size ?: 0
                                followersCount = usuarioData.seguidores.size ?: 0
                                commentsCount = usuarioData.numComentarios ?: 0
                            }
                        }
                    }
                }
        }

        LaunchedEffect(key1 = uid) {
            // Para obtener las películas favoritas
            usersViewModel.getFavoriteMovies(onSuccess = { peliculasFavoritas ->
                // Asignamos la lista de películas favoritas directamente
                favoriteMovies = peliculasFavoritas
            }, onFailure = { exception ->
                Toast.makeText(context, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            })

            // Para obtener las series favoritas
            usersViewModel.getFavoriteSeries(onSuccess = { seriesFavoritas ->
                // Asignamos la lista de series favoritas directamente
                favoriteSeries = seriesFavoritas
            }, onFailure = { exception ->
                Toast.makeText(context, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            })
        }



        // Scaffold para envolver el contenido y añadir el BottomNavigationBar
        Scaffold(
            bottomBar = { BottomNavigationBar(navController, uid) }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(150.dp)  // Tamaño del contenedor circular
                        .background(Color.White, shape = CircleShape)
                        .border(2.dp, Color.Black, shape = CircleShape)
                ) {
                    if (userProfilePic.isEmpty()) {
                        Image(
                            painter = painterResource(id = R.drawable.no_profile_icon),
                            contentDescription = "Sin icono",
                            modifier = Modifier
                                .fillMaxSize()
                                .align(Alignment.Center)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = rememberAsyncImagePainter(userProfilePic),
                            contentDescription = "Foto de perfil",
                            modifier = Modifier
                                .fillMaxSize()
                                .align(Alignment.Center)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = userName,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,  // Pone el texto en negrita
                        fontSize = 20.sp  // Ajusta el tamaño del texto a 20sp (puedes ajustarlo según lo necesites)
                    ),
                    color = Color.Black
                )


                Spacer(modifier = Modifier.height(24.dp))
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

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            // Acción cuando se hace clic en "Siguiendo"
                        }
                    ) {
                        Text(text = "Following")
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(text = followingCount.toString(), style = MaterialTheme.typography.bodySmall)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            // Acción cuando se hace clic en "Seguidores"
                        }
                    ) {
                        Text(text = "Followers")
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(text = followersCount.toString(), style = MaterialTheme.typography.bodySmall)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            // Acción cuando se hace clic en "Comentarios"
                        }
                    ) {
                        Text(text = "Comments")
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(text = commentsCount.toString(), style = MaterialTheme.typography.bodySmall)
                    }
                }

                Spacer(modifier = Modifier.height(64.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            // Aquí puedes poner la acción que quieres realizar al hacer clic en el Box
                            navController.navigate("favourite_movies/false")
                        }
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Favourite Movies",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(15.dp))

                        Column(modifier = Modifier.fillMaxWidth()) {
                            if (favoriteMovies.isNotEmpty()) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    // Limitar a las 3 primeras películas
                                    favoriteMovies.take(3).forEach { movie ->
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.width(120.dp)
                                        ) {
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
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
                            } else {
                                Text(
                                    text = "No tienes películas favoritas.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            // Aquí puedes poner la acción que quieres realizar al hacer clic en el Box
                            navController.navigate("favourite_movies/true")
                        }
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Favourite Series",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(15.dp))

                        if (favoriteSeries.isNotEmpty()) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Limitar a las 3 primeras series
                                favoriteSeries.take(3).forEach { series ->
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.width(120.dp)
                                    ) {
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            elevation = CardDefaults.cardElevation(4.dp)
                                        ) {
                                            Column {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(160.dp)
                                                ) {
                                                    val imageUrl =
                                                        "https://image.tmdb.org/t/p/w500${series.poster_path}"
                                                    Image(
                                                        painter = rememberAsyncImagePainter(imageUrl),
                                                        contentDescription = "Imagen de la serie",
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentScale = ContentScale.Crop
                                                    )
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(
                                                            Color(0xFF42A5F5) // Color para las series
                                                        )
                                                        .padding(4.dp)
                                                ) {
                                                    Text(
                                                        text = series.titulo,
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
                                }
                            }
                        } else {
                            Text(
                                text = "No tienes series favoritas.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }

                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
                if (currentUser == null) {
                    Text(
                        text = "Debes iniciar sesión para acceder a esta sección.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Red
                    )
                } else {
                    if (uid == currentUid) {
                        Button(
                            onClick = {
                                auth.signOut()
                                Toast.makeText(context, "Sesión cerrada", Toast.LENGTH_SHORT).show()
                                navController.navigate("login")
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
                    }
                }
            }
        }
    } else {
        Text(
            text = "No estás autenticado. Por favor, inicia sesión.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Red
        )
    }
}
