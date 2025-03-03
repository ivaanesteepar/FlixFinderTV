package com.example.flixfindertv.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun NewQuestionsScreen(navController: NavHostController) {
    val generos = listOf(
        "Action", "Romance", "Comedy",
        "Adventure", "Animation", "Crime",
        "Documentary", "Family", "Fantasy",
        "History", "Horror", "Music",
        "Mistery", "Science Fiction", "Thriller",
        "Western", "Kids", "Reality",
        "Soap", "Talk"
    )

    var generosSeleccionados by remember { mutableStateOf<List<String>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Which are your favourite genres?",
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(32.dp))

        LazyColumn {
            itemsIndexed(generos.chunked(3)) { _, filaGeneros ->  // Agrupar en filas de 3
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
                                        errorMessage = "Solo puedes seleccionar hasta 2 géneros"
                                    }
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = it, color = Color.Red, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (generosSeleccionados.size == 2) {
                    // Obtener el ID del usuario actual
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        // Obtener referencia a la colección de usuarios
                        val userRef = firestore.collection("usuarios").document(userId)

                        // Actualizar los campos 'esNuevo' y 'generosFavoritos' del usuario
                        userRef.update(
                            "esNuevo", false,
                            "generosFavoritos", generosSeleccionados
                        ).addOnSuccessListener {
                            // Si la actualización fue exitosa, navegar a la pantalla principal
                            navController.navigate("home")
                        }.addOnFailureListener { e ->
                            // Si ocurre un error, mostrar mensaje de error
                            errorMessage = "Error al guardar la selección: ${e.message}"
                        }
                    } else {
                        errorMessage = "No se pudo obtener el usuario actual"
                    }
                } else {
                    errorMessage = "Debes seleccionar dos géneros"
                }
            },
            enabled = generosSeleccionados.isNotEmpty()
        ) {
            Text(text = "Aceptar")
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
