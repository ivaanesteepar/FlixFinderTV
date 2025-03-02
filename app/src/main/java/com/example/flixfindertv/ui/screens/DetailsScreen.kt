package com.example.flixfindertv.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import com.example.flixfindertv.models.Peliculas

@Composable
fun DetailsScreen(navController: NavHostController, id: String) {
    var movieTitle by remember { mutableStateOf("") }

    // Obtener los detalles de la película con el id
    val firestore = FirebaseFirestore.getInstance()

    // Cargar los detalles de la película cuando se entra en la pantalla
    LaunchedEffect(id) {
        firestore.collection("peliculas")
            .document(id)
            .get()
            .addOnSuccessListener { document ->
                val movie = document.toObject(Peliculas::class.java)
                movieTitle = movie?.tituloOriginal ?: "Título no encontrado"
            }
    }

    Column {
        Text("ID: $id")
        Text("Título: $movieTitle")
    }

}

