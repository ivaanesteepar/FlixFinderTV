package com.example.flixfindertv.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.flixfindertv.R
import com.example.flixfindertv.ui.viewmodels.CommentsViewModel
import com.example.flixfindertv.ui.viewmodels.UsersViewModel
import kotlinx.coroutines.launch

@Composable
fun FollowingScreen(navController: NavController, uid: String) {
    val usersViewModel: UsersViewModel = viewModel()
    val commentsViewModel: CommentsViewModel = viewModel()

    var following by remember { mutableStateOf<List<Pair<String, String>>?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(uid) {
        coroutineScope.launch {
            following = usersViewModel.getFollowingUsers(uid)
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start // Alinea el contenido a la izquierda
        ) {
            IconButton(
                onClick = { navController.popBackStack() }, // Permite volver atrás en la navegación
                modifier = Modifier
                    .size(48.dp) // Tamaño total del botón (círculo)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver atrás",
                    tint = Color.Black // Color del ícono
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            if (following.isNullOrEmpty()) {
                // Mostrar el mensaje si no hay seguidores
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Este usuario no tiene seguidores.",
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn {
                    items(following ?: emptyList()) { follower ->
                        var profilePhotoUrl by remember { mutableStateOf<String?>(null) }

                        LaunchedEffect(follower.second) {
                            commentsViewModel.getUserProfilePhoto(follower.second) { url ->
                                profilePhotoUrl = url
                            }
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clickable {
                                    navController.navigate("profile/${follower.first}")
                                },
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val painter = if (!profilePhotoUrl.isNullOrEmpty()) {
                                    rememberAsyncImagePainter(profilePhotoUrl)
                                } else {
                                    painterResource(R.drawable.no_profile_icon) // Imagen por defecto
                                }

                                Image(
                                    painter = painter,
                                    contentDescription = "Foto de perfil",
                                    modifier = Modifier
                                        .size(40.dp)
                                        .border(2.dp, Color.Black, CircleShape)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = follower.second)
                            }
                        }
                    }
                }
            }
        }
    }
}
