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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.flixfindertv.R
import com.example.flixfindertv.utils.BottomNavigationBar

@Composable
fun ProfileScreen(navController: NavHostController, uid: String) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    // Usamos un estado para que la UI se actualice cuando los datos cambien
    var userName by remember { mutableStateOf("Usuario desconocido") }
    var userProfilePic by remember { mutableStateOf("") }
    var followingCount by remember { mutableStateOf(0) }
    var followersCount by remember { mutableStateOf(0) }
    var commentsCount by remember { mutableStateOf(0) }
    val context = LocalContext.current

    // Obtener el uid del usuario autenticado
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

        // Scaffold para envolver el contenido y añadir el BottomNavigationBar
        Scaffold(
            bottomBar = { BottomNavigationBar(navController, uid) }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(150.dp)  // Tamaño del contenedor circular
                        .background(Color.White, shape = CircleShape)
                        .border(2.dp, Color.Black, shape = CircleShape)
                ) {
                    // Si no hay foto de perfil, mostramos un ícono de persona gris
                    if (userProfilePic.isEmpty()) {
                        Image(
                            painter = painterResource(id = R.drawable.no_profile_icon),
                            contentDescription = "Sin icono",
                            modifier = Modifier
                                .fillMaxSize()
                                .align(Alignment.Center)
                                .clip(CircleShape),  // Asegura que la imagen se ajuste al círculo
                            contentScale = ContentScale.Crop  // Recorta la imagen para ajustarla al círculo sin deformarla
                        )
                    } else {
                        Image(
                            painter = rememberAsyncImagePainter(userProfilePic),
                            contentDescription = "Foto de perfil",
                            modifier = Modifier
                                .fillMaxSize()
                                .align(Alignment.Center)
                                .clip(CircleShape),  // Asegura que la imagen se ajuste al círculo
                            contentScale = ContentScale.Crop  // Recorta la imagen para ajustarla al círculo sin deformarla
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Nombre del usuario
                Text(
                    text = userName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(0.3f),
                    onClick = {
                        navController.navigate("edit_profile")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD3D3D3) // Color naranja
                    )
                ){
                    Text(
                        text = "Edit",
                        color = Color.Black
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                // Fila de Siguiendo, Seguidores y Comentarios
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Siguiendo
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            // Acción cuando se hace clic en "Siguiendo"
                            // Por ejemplo, navegar a una pantalla que muestra a los usuarios seguidos
                        }
                    ) {
                        Text(text = "Siguiendo")
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(text = followingCount.toString(), style = MaterialTheme.typography.bodySmall)
                    }

                    // Seguidores
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            // Acción cuando se hace clic en "Seguidores"
                            // Por ejemplo, navegar a una pantalla que muestra a los seguidores
                        }
                    ) {
                        Text(text = "Seguidores")
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(text = followersCount.toString(), style = MaterialTheme.typography.bodySmall)
                    }

                    // Comentarios
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            // Acción cuando se hace clic en "Comentarios"
                            // Por ejemplo, navegar a una pantalla que muestra los comentarios
                        }
                    ) {
                        Text(text = "Comentarios")
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(text = commentsCount.toString(), style = MaterialTheme.typography.bodySmall)
                    }
                }


                Spacer(modifier = Modifier.height(64.dp))  // Espacio entre las filas

                // Columna para Películas favoritas
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Películas favoritas",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Columna para Series favoritas
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Series favoritas",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Black
                    )
                }
                Spacer(modifier = Modifier.height(40.dp))

                // Verificamos si el usuario está logueado
                if (currentUser == null) {
                    Text(
                        text = "Debes iniciar sesión para acceder a esta sección.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Red
                    )
                } else {
                    // Solo mostrar el botón de cerrar sesión si el uid actual es el mismo que el uid del perfil
                    if (uid == currentUid) {
                        Button(
                            onClick = {
                                // Función para cerrar sesión
                                auth.signOut()
                                Toast.makeText(context, "Sesión cerrada", Toast.LENGTH_SHORT).show()
                                // Navegar a la pantalla de inicio de sesión
                                navController.navigate("login")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) {
                            Text(
                                text = "Cerrar sesión",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    } else {
        // Si no hay un usuario autenticado, mostramos un mensaje indicando que debe iniciar sesión
        Text(
            text = "No estás autenticado. Por favor, inicia sesión.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Red
        )
    }
}
