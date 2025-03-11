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
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.flixfindertv.R
import com.example.flixfindertv.utils.BottomNavigationBar

@Composable
fun ProfileScreen(navController: NavHostController) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    // Usamos un estado para que la UI se actualice cuando los datos cambien
    var userName by remember { mutableStateOf("Usuario desconocido") } // Estado para nombre de usuario
    var userProfilePic by remember { mutableStateOf("") } // Estado para la foto de perfil
    var followingCount by remember { mutableStateOf(0) } // Contador de siguiendo
    var followersCount by remember { mutableStateOf(0) } // Contador de seguidores
    var commentsCount by remember { mutableStateOf(0) } // Contador de comentarios
    val context = LocalContext.current

    // Obtener el uid del usuario autenticado
    val currentUser = auth.currentUser
    if (currentUser != null) {
        val uid = currentUser.uid

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
    }

    // Scaffold para envolver el contenido y añadir el BottomNavigationBar
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) } // Añadimos el menú inferior
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(paddingValues),  // Se aplica el padding del Scaffold
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(150.dp)  // Tamaño de la caja (se aumentó a 150dp)
                    .background(Color.White, shape = CircleShape)  // Fondo blanco
                    .border(2.dp, Color.Black, shape = CircleShape)  // Bordes negros
            ) {
                // Si no hay foto de perfil, mostramos un ícono de persona gris
                if (userProfilePic.isEmpty()) {
                    Image(
                        painter = painterResource(id = R.drawable.no_profile_icon),  // Carga la imagen desde res/drawable
                        contentDescription = "Sin icono",
                        modifier = Modifier
                            .size(100.dp)  // Tamaño del ícono (ajustado a la mitad del tamaño de la caja)
                            .align(Alignment.Center),  // Centra el ícono en la caja
                        contentScale = ContentScale.Crop  // Asegura que la imagen se recorte correctamente
                    )
                } else {
                    Image(
                        painter = rememberAsyncImagePainter(userProfilePic),
                        contentDescription = "Foto de perfil",
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.Center),  // Centra la imagen de perfil
                        contentScale = ContentScale.Crop
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

            // Fila de Siguiendo, Seguidores y Comentarios
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Siguiendo
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Siguiendo")
                    Text(text = followingCount.toString(), style = MaterialTheme.typography.bodySmall) // Valor actualizado dinámicamente
                }

                // Seguidores
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Seguidores")
                    Text(text = followersCount.toString(), style = MaterialTheme.typography.bodySmall) // Valor actualizado dinámicamente
                }

                // Comentarios
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Comentarios")
                    Text(text = commentsCount.toString(), style = MaterialTheme.typography.bodySmall) // Valor actualizado dinámicamente
                }
            }

            Spacer(modifier = Modifier.height(64.dp))  // Espacio entre las filas

            // Columna para Películas favoritas
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                // Películas favoritas
                Text(
                    text = "Películas favoritas",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(40.dp))  // Espacio entre las columnas

            // Columna para Series favoritas
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                // Series favoritas
                Text(
                    text = "Series favoritas",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black
                )
            }
            Spacer(modifier = Modifier.height(40.dp))
            // Botón de cerrar sesión
            Button(
                onClick = {
                    // Función para cerrar sesión
                    auth.signOut()
                    Toast.makeText(context, "Sesión cerrada", Toast.LENGTH_SHORT).show()
                    // Navegar a la pantalla de inicio de sesión (o la pantalla que desees)
                    navController.navigate("login") // Cambia por el nombre de la pantalla de login
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red // Fondo rojo
                )
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
