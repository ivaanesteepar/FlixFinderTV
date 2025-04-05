package com.example.flixfindertv.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.flixfindertv.R
import com.example.flixfindertv.ui.viewmodels.CommentsViewModel
import com.example.flixfindertv.ui.viewmodels.UsersViewModel
import kotlinx.coroutines.launch

@Composable
fun UserListScreen(navController: NavController, uid: String, isFollowing: Boolean) {
    val usersViewModel: UsersViewModel = viewModel()
    val commentsViewModel: CommentsViewModel = viewModel()

    var userList by remember { mutableStateOf<List<Pair<String, String>>?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    println("isFollowing aqui es: $isFollowing")

    LaunchedEffect(uid, isFollowing) {
        coroutineScope.launch {
            userList = if (isFollowing) {
                usersViewModel.getFollowingUsers(uid)
            } else {
                usersViewModel.getFollowersUsers(uid)
            }
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.fondo_app),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver atrás",
                        tint = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (isFollowing) "FOLLOWING" else "FOLLOWERS",
                color = Color.White,
                modifier = Modifier.fillMaxWidth(),
                fontSize = 25.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                if (userList.isNullOrEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Este usuario no tiene ${if (isFollowing) "seguidos" else "seguidores"}.",
                            color = Color.Gray
                        )
                    }
                } else {
                    LazyColumn {
                        items(userList ?: emptyList()) { user ->
                            var profilePhotoUrl by remember { mutableStateOf<String?>(null) }

                            LaunchedEffect(user.second) {
                                commentsViewModel.getUserProfilePhoto(user.second) { url ->
                                    profilePhotoUrl = url
                                }
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                                    .clickable {
                                        navController.navigate("profile/${user.first}/true")
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
                                        painterResource(R.drawable.no_profile_icon)
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
                                    Text(text = user.second)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

