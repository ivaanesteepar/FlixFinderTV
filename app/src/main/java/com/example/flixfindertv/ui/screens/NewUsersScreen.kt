package com.example.flixfindertv.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.flixfindertv.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Pantalla que permite a un usuario nuevo seleccionar 2 géneros que mas le gusten
@Composable
fun NewQuestionsScreen(navController: NavHostController) {
    val generos = listOf(
        "Action", "Adventure", "Animation",
        "Comedy", "Crime", "Family",
        "Fantasy", "History", "Horror",
        "Kids", "Music", "Mistery",
        "Reality", "Romance", "Science Fiction",
        "Soap", "Talk", "Thriller",
        "Western"
    )

    var generosSeleccionados by remember { mutableStateOf<List<String>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

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
                .padding(16.dp)
                .padding(top=56.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Which are your favourite genres?",
                fontSize = 20.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(32.dp))

            // Usando Column en lugar de LazyColumn
            generos.chunked(3).forEach { filaGeneros ->  // Agrupar en filas de 3
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    filaGeneros.forEach { genero ->
                        Box(modifier = Modifier.weight(1f)) {  // Se aplica el peso de cada Card
                            GeneroCard(
                                genero = genero,
                                isSelected = generosSeleccionados.contains(genero),
                                onClick = {
                                    if (generosSeleccionados.contains(genero)) { // Si deseleccionamos el género
                                        generosSeleccionados = generosSeleccionados - genero
                                        errorMessage = null
                                    } else if (generosSeleccionados.size < 2) { // Si seleccionamos el género
                                        generosSeleccionados = generosSeleccionados + genero
                                        errorMessage = null
                                    } else {
                                        errorMessage = "You can only select up to 2 genres"
                                    }

                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            errorMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = it, color = Color.Red, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (generosSeleccionados.size == 2) {
                        val userId = auth.currentUser?.uid
                        if (userId != null) {
                            val userRef = firestore.collection("usuarios").document(userId)

                            val timestamp = System.currentTimeMillis()
                            val generosConFecha = generosSeleccionados.associateWith { timestamp }

                            val datos = mapOf(
                                "esNuevo" to false,
                                "generosFavoritos" to generosConFecha
                            )

                            userRef.update(datos)
                                .addOnSuccessListener {
                                    navController.navigate("home")
                                }
                                .addOnFailureListener { e ->
                                    errorMessage = "Error al guardar la selección: ${e.message}"
                                }
                        } else {
                            errorMessage = "Could not retrieve the current user"
                        }
                    } else {
                        errorMessage = "You must select two genres"
                    }
                },
                enabled = generosSeleccionados.isNotEmpty()
            ) {
                Text(text = "Aceptar")
            }
        }
    }
}

@Composable
fun GeneroCard(genero: String, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFFFA500) else Color.LightGray
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = genero, fontSize = 12.sp, color = Color.Black)
        }
    }
}
