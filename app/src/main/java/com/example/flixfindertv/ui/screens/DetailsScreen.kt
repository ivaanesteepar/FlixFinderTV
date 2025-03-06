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
fun DetailsScreen(navController: NavHostController, id: String, esSerie: Boolean) {
    var movieTitle by remember { mutableStateOf("") }
    var moviePopularity by remember { mutableStateOf(0.0) }  // Para almacenar la popularidad

    // Obtener los detalles de la película o serie con el id
    val firestore = FirebaseFirestore.getInstance()
    val collectionName = if (esSerie) "series" else "peliculas" // Seleccionamos la colección dependiendo de si es una serie o no

    // Cargar los detalles de la película o serie cuando se entra en la pantalla
    LaunchedEffect(id) {
        firestore.collection(collectionName)
            .document(id)
            .get()
            .addOnSuccessListener { document ->
                val movie = document.toObject(Peliculas::class.java)
                println("Detalles: $movie")

                // Si el título original no está disponible, usamos el nombre alternativo
                movieTitle = movie?.title.takeIf { it?.isNotBlank() == true } ?: movie?.name ?: "Título no encontrado"
                moviePopularity = movie?.popularity ?: 0.0  // Asignamos la popularidad de la película o serie
            }
    }

    Column {
        Text("ID: $id")
        Text("Título: $movieTitle")
        Text("Popularidad: $moviePopularity")  // Mostrar la popularidad correctamente
    }
}
