package com.example.flixfindertv.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.flixfindertv.models.Peliculas
import com.example.flixfindertv.ui.viewmodels.ConexionViewModel
import com.example.flixfindertv.ui.viewmodels.MoviesViewModel
import com.example.flixfindertv.ui.viewmodels.SeriesViewModel
import com.example.flixfindertv.utils.BottomNavigationBar
import com.example.flixfindertv.utils.ScreenRecharge
import com.example.flixfindertv.utils.SharedPreferencesManager
import java.io.IOException
import java.net.SocketTimeoutException

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
    val sharedPreferencesManager = remember { SharedPreferencesManager(context) }

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
        bottomBar = { BottomNavigationBar(navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Menú de selección de Películas o Series
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

            if (selectedTab == "Peliculas") {
                // Mostrar películas
                if (movies.isNotEmpty()) {
                    Text("Popular movies", style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    MovieList(movies, navController, movieListState, false)
                }

                // Películas por categoría (Acción, Romance, etc.)
                if (actionMovies.isNotEmpty()) {
                    Text("Action movies", style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    MovieList(actionMovies, navController, actionMovieListState, false)
                }

                if (romanceMovies.isNotEmpty()) {
                    Text("Romance movies", style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    MovieList(romanceMovies, navController, romanceMovieListState, false)
                }

                if (familyMovies.isNotEmpty()) {
                    Text("Family movies", style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    MovieList(familyMovies, navController, familyMovieListState, false)
                }

                if (comedyMovies.isNotEmpty()) {
                    Text("Comedy movies", style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    MovieList(comedyMovies, navController, comedyMovieListState, false)
                }

                if (thrillerMovies.isNotEmpty()) {
                    Text("Thriller movies", style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    MovieList(thrillerMovies, navController, thrillerMovieListState, false)
                }

                if (horrorMovies.isNotEmpty()) {
                    Text("Horror movies", style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    MovieList(horrorMovies, navController, horrorMovieListState, false)
                }
                if (scienceFictionMovies.isNotEmpty()) {
                    Text("Science Fiction movies", style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    MovieList(scienceFictionMovies, navController, sciencieFictionMovieListState, false)
                }
            } else {
                // Mostrar series
                if (series.isNotEmpty()) {
                    Text("Popular series", style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    MovieList(series, navController, seriesListState, true)
                }

                if (actionAdventureSeries.isNotEmpty()) {
                    Text("Action & Adventure series", style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    MovieList(actionAdventureSeries, navController, actionadventureSerieListState, true)
                }

                if (animationSeries.isNotEmpty()) {
                    Text("Animation series", style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    MovieList(animationSeries, navController, animationSerieListState, true)
                }

                if (comedySeries.isNotEmpty()) {
                    Text("Comedy series", style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    MovieList(comedySeries, navController, comedySerieListState, true)
                }

                if (crimeSeries.isNotEmpty()) {
                    Text("Crime series", style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    MovieList(crimeSeries, navController, crimeListState, true)
                }

                if (dramaSeries.isNotEmpty()) {
                    Text("Drama series", style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    MovieList(dramaSeries, navController, dramaListState, true)
                }

                if (familySeries.isNotEmpty()) {
                    Text("Family series", style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    MovieList(familySeries, navController, familySerieListState, true)
                }

                if (kidsSeries.isNotEmpty()) {
                    Text("Kids series", style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    MovieList(kidsSeries, navController, kidsSerieListState, true)
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
                                .background(Color(0xFF6200EE))
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


