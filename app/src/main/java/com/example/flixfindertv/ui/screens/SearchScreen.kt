package com.example.flixfindertv.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.flixfindertv.models.Peliculas
import com.example.flixfindertv.ui.viewmodels.GenresViewModel
import com.example.flixfindertv.ui.viewmodels.MoviesViewModel
import com.example.flixfindertv.utils.BottomNavigationBar
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay

@Composable
fun SearchScreen(navController: NavHostController) {
    var searchQuery by remember { mutableStateOf("") }
    var movieResults by remember { mutableStateOf<List<Peliculas>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var filterExpanded by remember { mutableStateOf(false) }
    var orderAscending by remember { mutableStateOf(false) }
    var orderDescending by remember { mutableStateOf(false) }
    var filterMovie by remember { mutableStateOf(false) }
    var filterSerie by remember { mutableStateOf(false) }
    val firestore = FirebaseFirestore.getInstance()
    val viewModel = MoviesViewModel()
    val genresViewModel = GenresViewModel()

    var selectedGenre by remember { mutableStateOf<String?>(null) }
    var selectedGenreId by remember { mutableStateOf<Int?>(null) }


    val genres = listOf(
        "Music", "Romance", "Family", "War", "Action &\nAdventure", "Kids", "News",
        "Reality", "Sci-Fi &\nFantasy", "Soap", "Talk", "War &\nPolitics", "TV Movie",
        "Adventure", "Fantasy", "Animation", "Drama", "Horror", "Action", "Comedy",
        "History", "Western", "Thriller", "Crime", "Science\nFiction", "Mystery", "Documentary"
    )

    val genreColumns = genres.chunked(9) // Divide la lista en 3 columnas de 9 elementos

    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotEmpty()) {
            isSearching = true
            delay(500)
            viewModel.searchMovie(firestore, searchQuery) { movies ->
                movieResults = movies
                isSearching = false
            }
        } else {
            movieResults = emptyList()
            isSearching = false
        }
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search for movies or series") },
                    modifier = Modifier.weight(1f),
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
                IconButton(onClick = { filterExpanded = true }) {
                    Icon(imageVector = Icons.Default.FilterList, contentDescription = "Filtrar")
                }

                Box(modifier = Modifier.offset(x = (-16).dp, y = 70.dp)) {
                    DropdownMenu(
                        expanded = filterExpanded,
                        onDismissRequest = { filterExpanded = false },
                        modifier = Modifier.fillMaxWidth() // Asegúrate de que el menú ocupa todo el ancho
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(550.dp) // Mantener la caja gris con altura fija de 600.dp
                        ) {
                            // Column con scroll, ajustamos el padding inferior para evitar que se solape con el botón
                            Column(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .verticalScroll(rememberScrollState())  // Permite el desplazamiento vertical
                                    .padding(8.dp)  // Añadir padding si es necesario

                            ) {
                                DropdownMenuItem(text = { Text("Filtrar por género") }, onClick = {})

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                    genreColumns.forEach { columnGenres ->
                                        Column {
                                            columnGenres.forEach { genre ->
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Checkbox(
                                                        checked = selectedGenre == genre,
                                                        onCheckedChange = { isChecked ->
                                                            if (isChecked) {
                                                                genresViewModel.obtenerIdGeneroPorNombre(genre) { genreId ->
                                                                    selectedGenre = genre
                                                                    selectedGenreId = genreId
                                                                    println("Seleccionado: $genre, ID: $genreId")
                                                                }
                                                            } else {
                                                                selectedGenre = null
                                                                selectedGenreId = null
                                                            }
                                                        }
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(genre)
                                                }
                                            }
                                        }
                                    }
                                }

                                HorizontalDivider()

                                DropdownMenuItem(text = { Text("Filtrar por tipo") }, onClick = {})

                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(checked = filterMovie, onCheckedChange = {
                                        filterMovie = it
                                        if (it) filterSerie = false // Si se selecciona película, desmarcar serie
                                    })
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Películas")
                                }

                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(checked = filterSerie, onCheckedChange = {
                                        filterSerie = it
                                        if (it) filterMovie = false // Si se selecciona serie, desmarcar película
                                    })
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Series")
                                }
                                Spacer(modifier = Modifier.height(40.dp))

                            }

                            // Colocamos el botón de forma fija al final
                            Button(
                                onClick = {
                                    filterExpanded = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter) // Esto asegura que esté en la parte inferior del Box
                            ) {
                                Text(text = "Aceptar")
                            }
                        }
                    }
                }
            }

            LazyColumn {
                items(movieResults.filter {
                    (filterMovie && !it.esSerie) || (filterSerie && it.esSerie) || (!filterMovie && !filterSerie)
                }) { movie ->
                    MovieCard(movie = movie, navController = navController)
                }
            }
        }
    }
}


@Composable
fun MovieCard(movie: Peliculas, navController: NavHostController) {
    val isSerie = movie.esSerie
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable {
                navController.navigate("detalles/${movie.id}/${isSerie}")
            },
        shape = MaterialTheme.shapes.small.copy(CornerSize(16.dp)),
        //elevation = CardDefaults.cardElevation(1.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSerie) Color(0xFFB3E5FC) else Color(0xFFD1C4E9) // Cambia LightGray por el color que prefieras
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val imageUrl = "https://image.tmdb.org/t/p/w500${movie.poster_path}"
            Image(
                painter = rememberAsyncImagePainter(imageUrl),
                contentDescription = "Portada de ${movie.titulo}",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            val displayTitle = movie.title ?: movie.name ?: "Título no disponible"
            Text(
                text = displayTitle,
                style = MaterialTheme.typography.titleMedium,
                fontSize = 18.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = movie.overview,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

