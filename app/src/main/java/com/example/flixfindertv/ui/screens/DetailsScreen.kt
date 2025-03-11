package com.example.flixfindertv.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import com.example.flixfindertv.models.Peliculas

@Composable
fun DetailsScreen(navController: NavHostController, id: String, esSerie: Boolean) {
    var movieTitle by remember { mutableStateOf("") }
    var moviePopularity by remember { mutableStateOf(0.0) }  // Para almacenar la popularidad
    var movieGenre by remember { mutableStateOf("Cargando...") }  // Para almacenar el género como cadena

    val firestore = FirebaseFirestore.getInstance()
    val collectionName = if (esSerie) "series" else "peliculas" // Seleccionamos la colección dependiendo de si es una serie o no

    // Cargar los detalles de la película o serie cuando se entra en la pantalla
    LaunchedEffect(id) {
        firestore.collection(collectionName)
            .document(id)
            .get()
            .addOnSuccessListener { document ->
                val movie = document.toObject(Peliculas::class.java)
                println("Detalles de la película: $movie")

                movieTitle = movie?.title.takeIf { it?.isNotBlank() == true } ?: movie?.name ?: "Título no encontrado"
                moviePopularity = movie?.popularity ?: 0.0  // Asignamos la popularidad de la película o serie

                val genreIds = movie?.genre_ids ?: emptyList()
                println("Genre IDs encontrados: $genreIds")

                if (genreIds.isNotEmpty()) {
                    fetchGenreNames(genreIds) { genres ->
                        movieGenre = if (genres.isNotEmpty()) genres.joinToString(", ") else "Género no disponible"
                        println("Géneros obtenidos: $movieGenre")
                    }
                } else {
                    movieGenre = "Género no disponible"
                }
            }
            .addOnFailureListener { e ->
                println("Error al obtener detalles: ${'$'}{e.message}")
                movieGenre = "Error al cargar géneros"
            }
    }

    Column {
        Text("ID: $id")
        Text("Título: $movieTitle")
        Text("Popularidad: $moviePopularity")
        Text("Género: $movieGenre")
    }
}

fun fetchGenreNames(genreIds: List<Int>, onResult: (List<String>) -> Unit) {
    val firestore = FirebaseFirestore.getInstance()
    val genreNames = mutableListOf<String>()
    var count = 0
    println("Buscando nombres de género para IDs: $genreIds")

    if (genreIds.isEmpty()) {
        onResult(emptyList())
        return
    }

    genreIds.forEach { genreId ->
        firestore.collection("generos").document(genreId.toString()).get()
            .addOnSuccessListener { document ->
                val genreName = document.getString("name")
                if (genreName != null) {
                    println("El nombre del genero es: $genreName")
                    genreNames.add(genreName)
                } else {
                    println("No se encontró género para ID: $genreId")
                }
            }
            .addOnFailureListener { e ->
                println("Error al obtener género ${'$'}genreId: ${'$'}{e.message}")
            }
            .addOnCompleteListener {
                count++
                if (count == genreIds.size) {
                    onResult(genreNames)
                }
            }
    }
}
