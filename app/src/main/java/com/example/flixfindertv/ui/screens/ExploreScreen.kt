package com.example.flixfindertv.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.flixfindertv.R
import com.example.flixfindertv.models.Peliculas
import com.example.flixfindertv.ui.viewmodels.ConexionViewModel
import com.example.flixfindertv.ui.viewmodels.GenresViewModel
import com.example.flixfindertv.ui.viewmodels.MoviesViewModel
import com.example.flixfindertv.ui.viewmodels.SeriesViewModel
import com.example.flixfindertv.utils.BottomNavigationBar
import com.example.flixfindertv.utils.SharedPreferencesManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay

fun contieneCaracteresNoLatinos(titulo: String): Boolean {
    // Expresión regular para detectar caracteres en chino, japonés, coreano o ruso
    val regex = "[\\u4E00-\\u9FFF\\u3040-\\u30FF\\uAC00-\\uD7AF\\u0400-\\u04FF]".toRegex()
    return regex.containsMatchIn(titulo)
}

@Composable
fun ExploreScreen(navController: NavHostController, viewModel: MoviesViewModel, conexionViewModel: ConexionViewModel) {
    val seriesViewModel: SeriesViewModel = viewModel()

    var selectedTab by remember { mutableStateOf("Peliculas") }
    val movies by viewModel.listaPeliculas.observeAsState(emptyList())
    val series by seriesViewModel.listaSeries.observeAsState(emptyList())
    val isSeriesLoading by seriesViewModel.isLoadingSeries.observeAsState(false)
    val isLoading by viewModel.isLoadingPeliculas.observeAsState(false)
    val isActionLoading by viewModel.isLoadingAction.observeAsState(false)
    val isRomanceMoviesLoading by viewModel.isLoadingRomance.observeAsState(false)
    val isFamilyMoviesLoading by viewModel.isLoadingFamily.observeAsState(false)
    val isComedyMoviesLoading by viewModel.isLoadingComedy.observeAsState(false)
    val isThrillerMoviesLoading by viewModel.isLoadingThriller.observeAsState(false)
    val isHorrorMoviesLoading by viewModel.isLoadingHorror.observeAsState(false)
    val isScienceFictionMoviesLoading by viewModel.isLoadingScienceFiction.observeAsState(false)

    val isActionAdventureSeriesLoading by seriesViewModel.isLoadingActionAdventure.observeAsState(false)
    val isAnimationSeriesLoading by seriesViewModel.isLoadingAnimation.observeAsState(false)
    val isComedySeriesLoading by seriesViewModel.isLoadingComedySeries.observeAsState(false)
    val isCrimeSeriesLoading by seriesViewModel.isLoadingCrime.observeAsState(false)
    val isDramaSeriesLoading by seriesViewModel.isLoadingDrama.observeAsState(false)
    val isFamilySeriesLoading by seriesViewModel.isLoadingFamilySeries.observeAsState(false)
    val isKidsSeriesLoading by seriesViewModel.isLoadingKidsSeries.observeAsState(false)

    val hayConexion by conexionViewModel.conexionEstablecida

    val actionMovies by viewModel.listaPeliculasAccion.observeAsState(emptyList())
    val romanceMovies by viewModel.listaPeliculasRomance.observeAsState(emptyList())
    val familyMovies by viewModel.listaPeliculasFamily.observeAsState(emptyList())
    val comedyMovies by viewModel.listaPeliculasComedy.observeAsState(emptyList())
    val thrillerMovies by viewModel.listaPeliculasThriller.observeAsState(emptyList())
    val horrorMovies by viewModel.listaPeliculasHorror.observeAsState(emptyList())
    val scienceFictionMovies by viewModel.listaPeliculasScienceFiction.observeAsState(emptyList())

    val actionAdventureSeries by seriesViewModel.listaSeriesAccionAventura.observeAsState(emptyList())
    val animationSeries by seriesViewModel.listaSeriesAnimacion.observeAsState(emptyList())
    val comedySeries by seriesViewModel.listaSeriesComedia.observeAsState(emptyList())
    val crimeSeries by seriesViewModel.listaSeriesCrimen.observeAsState(emptyList())
    val dramaSeries by seriesViewModel.listaSeriesDrama.observeAsState(emptyList())
    val familySeries by seriesViewModel.listaSeriesFamily.observeAsState(emptyList())
    val kidsSeries by seriesViewModel.listaSeriesKids.observeAsState(emptyList())

    val maxMovies = 100  // Límite de películas
    val maxSeries = 100  // Límite de series

    val context = LocalContext.current
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    val sharedPreferencesManager = remember { SharedPreferencesManager(context) }

    var searchQuery by remember { mutableStateOf("") }
    var movieResults by remember { mutableStateOf<List<Peliculas>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var filterExpanded by remember { mutableStateOf(false) }
    var orderAscending by remember { mutableStateOf(false) }
    var orderDescending by remember { mutableStateOf(false) }
    var filterMovie by remember { mutableStateOf(false) }
    var filterSerie by remember { mutableStateOf(false) }
    val firestore = FirebaseFirestore.getInstance()
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

    println("lista peliculas: $movies")
    println("lista series: $series")

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

    // Ejecutar la comprobación de conexión cuando la pantalla se renderiza
    LaunchedEffect(key1 = navController) {
        conexionViewModel.isOnline()
        println("la conexion es: $hayConexion")
    }

    // Cargar películas y series iniciales cuando haya conexión
    LaunchedEffect(hayConexion) {
        if (hayConexion) {
            // Si no hay conexión, cargamos las películas y series desde la base de datos local
            viewModel.obtenerPeliculasPopulares()
            viewModel.obtenerPeliculasAccion()
            viewModel.obtenerPeliculasRomance()
            viewModel.obtenerPeliculasFamily()
            viewModel.obtenerPeliculasComedy()
            viewModel.obtenerPeliculasThriller()
            viewModel.obtenerPeliculasHorror()
            viewModel.obtenerPeliculasCienciaFiccion()

            seriesViewModel.obtenerSeriesPopulares()
            seriesViewModel.obtenerSeriesAccionAventura()
            seriesViewModel.obtenerSeriesAnimacion()
            seriesViewModel.obtenerSeriesComedia()
            seriesViewModel.obtenerSeriesCrimen()
            seriesViewModel.obtenerSeriesDrama()
            seriesViewModel.obtenerSeriesFamilia()
            seriesViewModel.obtenerSeriesKids()

        }
        else {
            // Si no hay conexión, carga las películas y series desde SharedPreferences
//            val (moviesFromPrefs, seriesFromPrefs) = sharedPreferencesManager.loadMoviesAndSeries()
//            viewModel.setPeliculas(moviesFromPrefs)
//            viewModel.setSeries(seriesFromPrefs)
        }
    }

    // Detectar cuando el usuario llega a la película 20, 40, 60, etc. y cargar más películas
    val movieListState = rememberLazyListState()
    val actionMovieListState = rememberLazyListState()
    val romanceMovieListState = rememberLazyListState()
    val familyMovieListState = rememberLazyListState()
    val comedyMovieListState = rememberLazyListState()
    val thrillerMovieListState = rememberLazyListState()
    val horrorMovieListState = rememberLazyListState()
    val sciencieFictionMovieListState = rememberLazyListState()

    val seriesListState = rememberLazyListState()
    val actionadventureSerieListState = rememberLazyListState()
    val animationSerieListState = rememberLazyListState()
    val comedySerieListState = rememberLazyListState()
    val crimeListState = rememberLazyListState()
    val dramaListState = rememberLazyListState()
    val familySerieListState = rememberLazyListState()
    val kidsSerieListState = rememberLazyListState()

    // Cargar más películas cuando llegas al final de la lista de películas
    LaunchedEffect(movieListState.firstVisibleItemIndex) {
        val threshold = 5  // Umbral de carga

        // Verificar si estamos cerca del final de la lista de películas y si no se ha alcanzado el límite de 100
        if (movieListState.firstVisibleItemIndex >= (movies.size - threshold) && !isLoading && movies.size < maxMovies && hayConexion) {
            viewModel.obtenerPeliculasPopulares()  // Cargar más películas
        }
    }
    LaunchedEffect(seriesListState.firstVisibleItemIndex) {
        val threshold = 10  // Umbral de carga

        // Verificar si estamos cerca del final de la lista de series y si no se ha alcanzado el límite de 100
        if (seriesListState.firstVisibleItemIndex >= (series.size - threshold) && !isSeriesLoading && series.size < maxSeries && hayConexion) {
            seriesViewModel.obtenerSeriesPopulares()
        }
    }
    LaunchedEffect(actionMovieListState.firstVisibleItemIndex) {
        val threshold = 10  // Umbral de carga

        // Verificar si estamos cerca del final de la lista de series y si no se ha alcanzado el límite de 100
        if (actionMovieListState.firstVisibleItemIndex >= (actionMovies.size - threshold) && !isActionLoading && actionMovies.size < maxMovies && hayConexion) {
            viewModel.obtenerPeliculasAccion()
        }
    }
    LaunchedEffect(romanceMovieListState.firstVisibleItemIndex) {
        val threshold = 10  // Umbral de carga

        // Verificar si estamos cerca del final de la lista de series y si no se ha alcanzado el límite de 100
        if (romanceMovieListState.firstVisibleItemIndex >= (romanceMovies.size - threshold) && !isRomanceMoviesLoading && romanceMovies.size < maxMovies && hayConexion) {
            viewModel.obtenerPeliculasRomance()
        }
    }
    LaunchedEffect(familyMovieListState.firstVisibleItemIndex) {
        val threshold = 10  // Umbral de carga

        // Verificar si estamos cerca del final de la lista de series y si no se ha alcanzado el límite de 100
        if (familyMovieListState.firstVisibleItemIndex >= (familyMovies.size - threshold) && !isFamilyMoviesLoading && familyMovies.size < maxMovies && hayConexion) {
            viewModel.obtenerPeliculasFamily()
        }
    }
    LaunchedEffect(comedyMovieListState.firstVisibleItemIndex) {
        val threshold = 10  // Umbral de carga

        // Verificar si estamos cerca del final de la lista de series y si no se ha alcanzado el límite de 100
        if (comedyMovieListState.firstVisibleItemIndex >= (comedyMovies.size - threshold) && !isComedyMoviesLoading && comedyMovies.size < maxMovies && hayConexion) {
            viewModel.obtenerPeliculasComedy()
        }
    }
    LaunchedEffect(thrillerMovieListState.firstVisibleItemIndex) {
        val threshold = 10  // Umbral de carga

        // Verificar si estamos cerca del final de la lista de series y si no se ha alcanzado el límite de 100
        if (thrillerMovieListState.firstVisibleItemIndex >= (thrillerMovies.size - threshold) && !isThrillerMoviesLoading && thrillerMovies.size < maxMovies && hayConexion) {
            viewModel.obtenerPeliculasThriller()
        }
    }
    LaunchedEffect(horrorMovieListState.firstVisibleItemIndex) {
        val threshold = 10  // Umbral de carga

        // Verificar si estamos cerca del final de la lista de series y si no se ha alcanzado el límite de 100
        if (horrorMovieListState.firstVisibleItemIndex >= (horrorMovies.size - threshold) && !isHorrorMoviesLoading && horrorMovies.size < maxMovies && hayConexion) {
            viewModel.obtenerPeliculasHorror()
        }
    }
    LaunchedEffect(sciencieFictionMovieListState.firstVisibleItemIndex) {
        val threshold = 10  // Umbral de carga

        // Verificar si estamos cerca del final de la lista de series y si no se ha alcanzado el límite de 100
        if (sciencieFictionMovieListState.firstVisibleItemIndex >= (scienceFictionMovies.size - threshold) && !isScienceFictionMoviesLoading && scienceFictionMovies.size < maxMovies && hayConexion) {
            viewModel.obtenerPeliculasCienciaFiccion()
        }
    }
    // Repetir la lógica para cada categoría de series
    LaunchedEffect(actionadventureSerieListState.firstVisibleItemIndex) {
        val threshold = 5  // Umbral de carga
        if (actionadventureSerieListState.firstVisibleItemIndex >= (actionAdventureSeries.size - threshold) && !isActionAdventureSeriesLoading && actionAdventureSeries.size < maxSeries && hayConexion) {
            seriesViewModel.obtenerSeriesAccionAventura()
        }
    }

    LaunchedEffect(animationSerieListState.firstVisibleItemIndex) {
        val threshold = 5  // Umbral de carga
        if (animationSerieListState.firstVisibleItemIndex >= (animationSeries.size - threshold) && !isAnimationSeriesLoading && animationSeries.size < maxSeries && hayConexion) {
            seriesViewModel.obtenerSeriesAnimacion()
        }
    }

    LaunchedEffect(comedySerieListState.firstVisibleItemIndex) {
        val threshold = 5  // Umbral de carga
        if (comedySerieListState.firstVisibleItemIndex >= (comedySeries.size - threshold) && !isComedySeriesLoading && comedySeries.size < maxSeries && hayConexion) {
            seriesViewModel.obtenerSeriesComedia()
        }
    }

    LaunchedEffect(crimeListState.firstVisibleItemIndex) {
        val threshold = 5  // Umbral de carga
        if (crimeListState.firstVisibleItemIndex >= (crimeSeries.size - threshold) && !isCrimeSeriesLoading && crimeSeries.size < maxSeries && hayConexion) {
            seriesViewModel.obtenerSeriesCrimen()
        }
    }

    LaunchedEffect(dramaListState.firstVisibleItemIndex) {
        val threshold = 5  // Umbral de carga
        if (dramaListState.firstVisibleItemIndex >= (dramaSeries.size - threshold) && !isDramaSeriesLoading && dramaSeries.size < maxSeries && hayConexion) {
            seriesViewModel.obtenerSeriesDrama()
        }
    }

    LaunchedEffect(familySerieListState.firstVisibleItemIndex) {
        val threshold = 5  // Umbral de carga
        if (familySerieListState.firstVisibleItemIndex >= (familySeries.size - threshold) && !isFamilySeriesLoading && familySeries.size < maxSeries && hayConexion) {
            seriesViewModel.obtenerSeriesFamilia()
        }
    }

    LaunchedEffect(kidsSerieListState.firstVisibleItemIndex) {
        val threshold = 5  // Umbral de carga
        if (kidsSerieListState.firstVisibleItemIndex >= (kidsSeries.size - threshold) && !isKidsSeriesLoading && kidsSeries.size < maxSeries && hayConexion) {
            seriesViewModel.obtenerSeriesKids()
        }
    }

    LaunchedEffect(movies) {
        println("Películas populares cargadas: ${movies.size}")
    }

    LaunchedEffect(series) {
        println("Series populares cargadas: ${series.size}")
    }

    Scaffold(
        bottomBar = {
            if (uid != null) {
                BottomNavigationBar(navController, uid)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween
            ) {
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
                                DropdownMenuItem(
                                    text = { Text("Filtrar por género") },
                                    onClick = {})

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    genreColumns.forEach { columnGenres ->
                                        Column {
                                            columnGenres.forEach { genre ->
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Checkbox(
                                                        checked = selectedGenre == genre,
                                                        onCheckedChange = { isChecked ->
                                                            if (isChecked) {
                                                                genresViewModel.obtenerIdGeneroPorNombre(
                                                                    genre
                                                                ) { genreId ->
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
                                        if (it) filterSerie =
                                            false // Si se selecciona película, desmarcar serie
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
                                        if (it) filterMovie =
                                            false // Si se selecciona serie, desmarcar película
                                    })
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Series")
                                }
                                Spacer(modifier = Modifier.height(40.dp))

                            }
                        }
                    }
                }
            }
            if (searchQuery.isNotEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize() // ✅ Define el tamaño del contenedor padre
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize() // ✅ Asegura que el LazyColumn tenga tamaño
                    ) {
                        items(movieResults.filter {
                            (filterMovie && !it.esSerie) || (filterSerie && it.esSerie) || (!filterMovie && !filterSerie)
                        }) { movie ->
                            MovieCard(movie = movie, navController = navController)
                        }
                    }
                }
            }
            else {
                // Si no buscamos nada, mostrar generos, populares, etc.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Botón de Películas
                    Box(
                        modifier = Modifier
                            .clickable { selectedTab = "Peliculas" }
                            .padding(16.dp)
                            .background(
                                color = if (selectedTab == "Peliculas") Color(0xFF6200EE) else Color.Transparent,
                                shape = MaterialTheme.shapes.medium
                            )
                    ) {
                        Text(
                            text = "Peliculas",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (selectedTab == "Peliculas") Color.White else Color.Black
                            ),
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    // Botón de Series
                    Box(
                        modifier = Modifier
                            .clickable { selectedTab = "Series" }
                            .padding(16.dp)
                            .background(
                                color = if (selectedTab == "Series") Color(0xFF6200EE) else Color.Transparent,
                                shape = MaterialTheme.shapes.medium
                            )
                    ) {
                        Text(
                            text = "Series",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (selectedTab == "Series") Color.White else Color.Black
                            ),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    val isLoadingExplore = movies.isEmpty()

                    // Mostrar indicador de carga si no hay películas ni series
                    if (isLoadingExplore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp), // Añadir padding para que haya espacio alrededor
                                contentAlignment = Alignment.Center // Centrar el indicador
                            ) {
                                Spacer(modifier = Modifier.height(250.dp))
                                CircularProgressIndicator() // Indicador de carga
                            }
                        }
                    }
                    else {
                        if (selectedTab == "Peliculas") {
                            if (movies.isNotEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(300.dp) // Ajustamos el tamaño para que el fondo sea más grande
                                            .padding(16.dp) // Añadimos padding para que haya espacio alrededor del contenido
                                    ) {
                                        // Fondo de la caja con drawable
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize() // Asegura que la imagen ocupe todo el tamaño del Box
                                        ) {
                                            Image(
                                                painter = painterResource(id = R.drawable.fondo_estrellas), // Aplica el fondo drawable
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .fillMaxSize() // La imagen se ajusta a toda la caja
                                                    .graphicsLayer(
                                                        scaleX = 1.2f, // Escalar un poco la imagen para que cubra más espacio
                                                        scaleY = 1.4f  // Escalar un poco la imagen para que cubra más espacio
                                                    )
                                            )
                                        }
                                        // Contenido sobre el fondo
                                        Column {
                                            Text(
                                                "Popular movies",
                                                style = MaterialTheme.typography.headlineMedium,
                                                color = Color.White
                                            )
                                            Spacer(modifier = Modifier.height(8.dp)) // Espacio entre el texto y la lista
                                            MovieList(
                                                movies,
                                                navController,
                                                movieListState,
                                                false
                                            ) // Lista de películas
                                        }
                                    }
                                }
                                item { Spacer(modifier = Modifier.height(25.dp)) }
                            }

                            if (actionMovies.isNotEmpty()) {
                                item {
                                    Text(
                                        "Action movies",
                                        style = MaterialTheme.typography.headlineMedium
                                    )
                                }
                                item { Spacer(modifier = Modifier.height(8.dp)) }
                                item {
                                    MovieList(
                                        actionMovies,
                                        navController,
                                        actionMovieListState,
                                        false
                                    )
                                }
                            }

                            if (romanceMovies.isNotEmpty()) {
                                item {
                                    Text(
                                        "Romance movies",
                                        style = MaterialTheme.typography.headlineMedium
                                    )
                                }
                                item { Spacer(modifier = Modifier.height(8.dp)) }
                                item {
                                    MovieList(
                                        romanceMovies,
                                        navController,
                                        romanceMovieListState,
                                        false
                                    )
                                }
                            }

                            if (familyMovies.isNotEmpty()) {
                                item {
                                    Text(
                                        "Family movies",
                                        style = MaterialTheme.typography.headlineMedium
                                    )
                                }
                                item { Spacer(modifier = Modifier.height(8.dp)) }
                                item {
                                    MovieList(
                                        familyMovies,
                                        navController,
                                        familyMovieListState,
                                        false
                                    )
                                }
                            }

                            if (comedyMovies.isNotEmpty()) {
                                item {
                                    Text(
                                        "Comedy movies",
                                        style = MaterialTheme.typography.headlineMedium
                                    )
                                }
                                item { Spacer(modifier = Modifier.height(8.dp)) }
                                item {
                                    MovieList(
                                        comedyMovies,
                                        navController,
                                        comedyMovieListState,
                                        false
                                    )
                                }
                            }

                            if (thrillerMovies.isNotEmpty()) {
                                item {
                                    Text(
                                        "Thriller movies",
                                        style = MaterialTheme.typography.headlineMedium
                                    )
                                }
                                item { Spacer(modifier = Modifier.height(8.dp)) }
                                item {
                                    MovieList(
                                        thrillerMovies,
                                        navController,
                                        thrillerMovieListState,
                                        false
                                    )
                                }
                            }

                            if (horrorMovies.isNotEmpty()) {
                                item {
                                    Text(
                                        "Horror movies",
                                        style = MaterialTheme.typography.headlineMedium
                                    )
                                }
                                item { Spacer(modifier = Modifier.height(8.dp)) }
                                item {
                                    MovieList(
                                        horrorMovies,
                                        navController,
                                        horrorMovieListState,
                                        false
                                    )
                                }
                            }

                            if (scienceFictionMovies.isNotEmpty()) {
                                item {
                                    Text(
                                        "Science Fiction movies",
                                        style = MaterialTheme.typography.headlineMedium
                                    )
                                }
                                item { Spacer(modifier = Modifier.height(8.dp)) }
                                item {
                                    MovieList(
                                        scienceFictionMovies,
                                        navController,
                                        sciencieFictionMovieListState,
                                        false
                                    )
                                }
                            }
                        } else {
                            if (series.isNotEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(300.dp) // Ajustamos el tamaño para que el fondo sea más grande
                                            .padding(16.dp) // Añadimos padding para que haya espacio alrededor del contenido
                                    ) {
                                        // Fondo de la caja con drawable
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize() // Asegura que la imagen ocupe todo el tamaño del Box
                                        ) {
                                            Image(
                                                painter = painterResource(id = R.drawable.fondo_estrellas), // Aplica el fondo drawable
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .fillMaxSize() // La imagen se ajusta a toda la caja
                                                    .graphicsLayer(
                                                        scaleX = 1.2f, // Escalar un poco la imagen para que cubra más espacio
                                                        scaleY = 1.4f  // Escalar un poco la imagen para que cubra más espacio
                                                    )
                                            )
                                        }
                                        // Contenido sobre el fondo
                                        Column {
                                            Text(
                                                "Popular series",
                                                style = MaterialTheme.typography.headlineMedium,
                                                color = Color.White
                                            )
                                            Spacer(modifier = Modifier.height(8.dp)) // Espacio entre el texto y la lista
                                            MovieList(series, navController, seriesListState, true)
                                        }
                                    }
                                }
                                item { Spacer(modifier = Modifier.height(25.dp)) }
                            }

                            if (actionAdventureSeries.isNotEmpty()) {
                                item {
                                    Text(
                                        "Action & Adventure series",
                                        style = MaterialTheme.typography.headlineMedium
                                    )
                                }
                                item { Spacer(modifier = Modifier.height(8.dp)) }
                                item {
                                    MovieList(
                                        actionAdventureSeries,
                                        navController,
                                        actionadventureSerieListState,
                                        true
                                    )
                                }
                            }

                            if (animationSeries.isNotEmpty()) {
                                item {
                                    Text(
                                        "Animation series",
                                        style = MaterialTheme.typography.headlineMedium
                                    )
                                }
                                item { Spacer(modifier = Modifier.height(8.dp)) }
                                item {
                                    MovieList(
                                        animationSeries,
                                        navController,
                                        animationSerieListState,
                                        true
                                    )
                                }
                            }

                            if (comedySeries.isNotEmpty()) {
                                item {
                                    Text(
                                        "Comedy series",
                                        style = MaterialTheme.typography.headlineMedium
                                    )
                                }
                                item { Spacer(modifier = Modifier.height(8.dp)) }
                                item {
                                    MovieList(
                                        comedySeries,
                                        navController,
                                        comedySerieListState,
                                        true
                                    )
                                }
                            }

                            if (crimeSeries.isNotEmpty()) {
                                item {
                                    Text(
                                        "Crime series",
                                        style = MaterialTheme.typography.headlineMedium
                                    )
                                }
                                item { Spacer(modifier = Modifier.height(8.dp)) }
                                item { MovieList(crimeSeries, navController, crimeListState, true) }
                            }

                            if (dramaSeries.isNotEmpty()) {
                                item {
                                    Text(
                                        "Drama series",
                                        style = MaterialTheme.typography.headlineMedium
                                    )
                                }
                                item { Spacer(modifier = Modifier.height(8.dp)) }
                                item { MovieList(dramaSeries, navController, dramaListState, true) }
                            }

                            if (familySeries.isNotEmpty()) {
                                item {
                                    Text(
                                        "Family series",
                                        style = MaterialTheme.typography.headlineMedium
                                    )
                                }
                                item { Spacer(modifier = Modifier.height(8.dp)) }
                                item {
                                    MovieList(
                                        familySeries,
                                        navController,
                                        familySerieListState,
                                        true
                                    )
                                }
                            }

                            if (kidsSeries.isNotEmpty()) {
                                item {
                                    Text(
                                        "Kids series",
                                        style = MaterialTheme.typography.headlineMedium
                                    )
                                }
                                item { Spacer(modifier = Modifier.height(8.dp)) }
                                item {
                                    MovieList(
                                        kidsSeries,
                                        navController,
                                        kidsSerieListState,
                                        true
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MovieList(
    movies: List<Peliculas>,
    navController: NavController,
    listState: LazyListState, // Añadido para pasar el state correspondiente
    isSerie: Boolean
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        state = listState, // Usar el state correspondiente para el desplazamiento
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(movies.filter { !contieneCaracteresNoLatinos(it.titulo) }) { movie ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(120.dp)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navController.navigate("detalles/${movie.id}/$isSerie")
                        },
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        ) {
                            val imageUrl = "https://image.tmdb.org/t/p/w500${movie.poster_path}"
                            Image(
                                painter = rememberAsyncImagePainter(imageUrl),
                                contentDescription = "Imagen de la película",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isSerie) Color(0xFF4DB6AC) else Color(0xFF42A5F5))
                                .padding(4.dp)
                        ) {
                            Text(
                                text = movie.titulo,
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    }
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
            containerColor = if (isSerie) Color(0xFF4DB6AC) else Color(0xFF42A5F5) // Cambia LightGray por el color que prefieras
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
                color = Color.Black
            )
        }
    }
}


