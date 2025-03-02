package com.example.flixfindertv.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.flixfindertv.models.Comentario
import com.example.flixfindertv.models.Peliculas
import com.example.flixfindertv.utils.BottomNavigationBar
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay

@Composable
fun SearchScreen(navController: NavHostController) {
    var searchQuery by remember { mutableStateOf("") }
    var movieResults by remember { mutableStateOf<List<Peliculas>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) } // Para mostrar un estado de carga

    val firestore = FirebaseFirestore.getInstance()

    // Llamada a la búsqueda con un debounce
    LaunchedEffect(searchQuery) {
        // Aplicamos un retraso de 500ms para evitar hacer una búsqueda con cada letra
        if (searchQuery.isNotEmpty()) {
            isSearching = true
            delay(500) // Retraso de 500ms
            searchMovie(firestore, searchQuery) { movies ->
                movieResults = movies
                isSearching = false
            }
        } else {
            movieResults = emptyList() // Si no hay query, no mostrar resultados
            isSearching = false
        }
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) } // Añadimos el menú inferior
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            // Cuadro de búsqueda con botón de búsqueda
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar películas o series") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                trailingIcon = {
                    if (isSearching) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = Color.Gray
                        )
                    } else {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Buscar")
                    }
                }
            )

            LazyColumn {
                items(movieResults) { movie ->
                    // Pasamos el navController a cada MovieCard
                    MovieCard(movie = movie, navController = navController)
                }
            }
        }
    }
}


fun searchMovie(firestore: FirebaseFirestore, query: String, onResult: (List<Peliculas>) -> Unit) {
    if (query.isNotEmpty()) {
        val peliculasRef = firestore.collection("peliculas")
        val seriesRef = firestore.collection("series")

        // Hacemos las dos búsquedas de manera independiente
        val peliculasQuery = peliculasRef
            .orderBy("tituloOriginal")
            .startAt(query)
            .endAt(query + "\uf8ff")
            .get()

        val seriesQuery = seriesRef
            .orderBy("nombreAlternativo")
            .startAt(query)
            .endAt(query + "\uf8ff")
            .get()

        // Esperamos las dos búsquedas y combinamos los resultados
        peliculasQuery.addOnSuccessListener { peliculasResult ->
            seriesQuery.addOnSuccessListener { seriesResult ->
                val peliculas = peliculasResult.documents.mapNotNull { document ->
                    val movie = Peliculas(
                        id = document.getString("id") ?: "",
                        tituloOriginal = document.getString("tituloOriginal"),
                        nombreAlternativo = document.getString("nombreAlternativo"),
                        descripcion = document.getString("descripcion") ?: "",
                        fecha = document.getString("fecha"),
                        portada = document.getString("portada") ?: "",
                        votoPromedio = document.getString("votoPromedio") ?: "0.0",
                        numVotos = document.getString("numVotos") ?: "0",
                        generos = (document.get("generos") as? List<String>) ?: emptyList(),
                        esAdulto = document.getBoolean("esAdulto") ?: false,
                        banner = document.getString("banner") ?: "",
                        comentarios = (document.get("comentarios") as? List<Map<String, Any>>)?.map {
                            Comentario(
                                usuario = it["usuario"] as? String ?: "",
                                comentario = it["comentario"] as? String ?: ""
                            )
                        } ?: emptyList()
                    )
                    movie
                }

                val series = seriesResult.documents.mapNotNull { document ->
                    val movie = Peliculas(
                        id = document.getString("id") ?: "",
                        tituloOriginal = document.getString("tituloOriginal"),
                        nombreAlternativo = document.getString("nombreAlternativo"),
                        descripcion = document.getString("descripcion") ?: "",
                        fecha = document.getString("fecha"),
                        portada = document.getString("portada") ?: "",
                        votoPromedio = document.getString("votoPromedio") ?: "0.0",
                        numVotos = document.getString("numVotos") ?: "0",
                        generos = (document.get("generos") as? List<String>) ?: emptyList(),
                        esAdulto = document.getBoolean("esAdulto") ?: false,
                        banner = document.getString("banner") ?: "",
                        comentarios = (document.get("comentarios") as? List<Map<String, Any>>)?.map {
                            Comentario(
                                usuario = it["usuario"] as? String ?: "",
                                comentario = it["comentario"] as? String ?: ""
                            )
                        } ?: emptyList()
                    )
                    movie
                }

                // Combinamos los resultados de ambas colecciones
                val combinedResults = peliculas + series
                onResult(combinedResults) // Pasamos la lista combinada a la UI
            }
        }
            .addOnFailureListener {
                onResult(emptyList()) // Si ocurre un error, pasar una lista vacía
            }
    } else {
        onResult(emptyList()) // Si no hay texto de búsqueda, retornar lista vacía
    }
}



@Composable
fun MovieCard(movie: Peliculas, navController: NavHostController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable {
                // Navegar a la pantalla de detalles pasando el ID de la película
                navController.navigate("detalles/${movie.id}")
            },
        shape = MaterialTheme.shapes.small.copy(CornerSize(16.dp)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Imagen de la portada de la película
            val imageUrl = "https://image.tmdb.org/t/p/w500${movie.portada}"
            Image(
                painter = rememberAsyncImagePainter(imageUrl),
                contentDescription = "Portada de ${movie.titulo}",
                modifier = Modifier.fillMaxWidth().height(200.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Título de la película
            val displayTitle = movie.tituloOriginal ?: movie.nombreAlternativo ?: "Título no disponible"
            Text(
                text = displayTitle,
                style = MaterialTheme.typography.titleMedium,
                fontSize = 18.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))
            // Descripción de la película
            Text(
                text = movie.descripcion,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

