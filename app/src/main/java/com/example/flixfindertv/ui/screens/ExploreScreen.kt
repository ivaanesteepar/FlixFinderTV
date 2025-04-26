package com.example.flixfindertv.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.flixfindertv.R
import com.example.flixfindertv.models.Peliculas
import com.example.flixfindertv.room.entities.*
import com.example.flixfindertv.ui.viewmodels.ConexionViewModel
import com.example.flixfindertv.ui.viewmodels.GenresViewModel
import com.example.flixfindertv.ui.viewmodels.MoviesViewModel
import com.example.flixfindertv.ui.viewmodels.OfflineViewModel
import com.example.flixfindertv.ui.viewmodels.SeriesViewModel
import com.example.flixfindertv.utils.BottomNavigationBar
import com.example.flixfindertv.utils.ContentListSearch
import com.example.flixfindertv.utils.ContentListExplore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay

fun contieneCaracteresNoLatinos(titulo: String): Boolean {
    // Expresión regular para detectar caracteres en chino, japonés, coreano o ruso
    val regex = "[\\u4E00-\\u9FFF\\u3040-\\u30FF\\uAC00-\\uD7AF\\u0400-\\u04FF]".toRegex()
    return regex.containsMatchIn(titulo)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(navController: NavHostController, viewModel: MoviesViewModel) {
    val seriesViewModel: SeriesViewModel = viewModel()
    val conexionViewModel: ConexionViewModel = viewModel()
    val genresViewModel: GenresViewModel = viewModel()
    val offlineViewModel: OfflineViewModel = viewModel()
    val context = LocalContext.current
    val activity = context as? Activity
    var listasGuardadas by rememberSaveable { mutableStateOf(false) }

    var expanded by remember { mutableStateOf(false) }
    var selectedTab by rememberSaveable { mutableStateOf("Movies") }
    val tabs = listOf("Movies", "TV Shows")
    val movies by viewModel.listaPeliculas.observeAsState(emptyList())
    val series by seriesViewModel.listaSeries.observeAsState(emptyList())
    val isSeriesLoading by seriesViewModel.isLoadingSeries.observeAsState(false)
    val isLoading by viewModel.isLoadingPeliculas.observeAsState(false)
    val isRecentLoading by viewModel.isLoadingRecientes.observeAsState(false)
    val isActionLoading by viewModel.isLoadingAction.observeAsState(false)
    val isRomanceMoviesLoading by viewModel.isLoadingRomance.observeAsState(false)
    val isFamilyMoviesLoading by viewModel.isLoadingFamily.observeAsState(false)
    val isComedyMoviesLoading by viewModel.isLoadingComedy.observeAsState(false)
    val isThrillerMoviesLoading by viewModel.isLoadingThriller.observeAsState(false)
    val isHorrorMoviesLoading by viewModel.isLoadingHorror.observeAsState(false)
    val isScienceFictionMoviesLoading by viewModel.isLoadingScienceFiction.observeAsState(false)

    val isActionAdventureSeriesLoading by seriesViewModel.isLoadingActionAdventure.observeAsState(
        false
    )
    val isAnimationSeriesLoading by seriesViewModel.isLoadingAnimation.observeAsState(false)
    val isComedySeriesLoading by seriesViewModel.isLoadingComedySeries.observeAsState(false)
    val isCrimeSeriesLoading by seriesViewModel.isLoadingCrime.observeAsState(false)
    val isDramaSeriesLoading by seriesViewModel.isLoadingDrama.observeAsState(false)
    val isFamilySeriesLoading by seriesViewModel.isLoadingFamilySeries.observeAsState(false)
    val isKidsSeriesLoading by seriesViewModel.isLoadingKidsSeries.observeAsState(false)
    val isRecentSeriesLoading by seriesViewModel.isLoadingRecentSeries.observeAsState(false)

    val hayConexion by conexionViewModel.conexionEstablecida.collectAsState()

    val actionMovies by viewModel.listaPeliculasAccion.observeAsState(emptyList())
    val romanceMovies by viewModel.listaPeliculasRomance.observeAsState(emptyList())
    val familyMovies by viewModel.listaPeliculasFamily.observeAsState(emptyList())
    val comedyMovies by viewModel.listaPeliculasComedy.observeAsState(emptyList())
    val thrillerMovies by viewModel.listaPeliculasThriller.observeAsState(emptyList())
    val horrorMovies by viewModel.listaPeliculasHorror.observeAsState(emptyList())
    val scienceFictionMovies by viewModel.listaPeliculasScienceFiction.observeAsState(emptyList())
    val recentMovies by viewModel.listaPeliculasRecientes.observeAsState(emptyList())

    val actionAdventureSeries by seriesViewModel.listaSeriesAccionAventura.observeAsState(emptyList())
    val animationSeries by seriesViewModel.listaSeriesAnimacion.observeAsState(emptyList())
    val comedySeries by seriesViewModel.listaSeriesComedia.observeAsState(emptyList())
    val crimeSeries by seriesViewModel.listaSeriesCrimen.observeAsState(emptyList())
    val dramaSeries by seriesViewModel.listaSeriesDrama.observeAsState(emptyList())
    val familySeries by seriesViewModel.listaSeriesFamily.observeAsState(emptyList())
    val kidsSeries by seriesViewModel.listaSeriesKids.observeAsState(emptyList())
    val recentSeries by seriesViewModel.listaSeriesRecientes.observeAsState(emptyList())

    val maxMovies = 100  // Límite de películas
    val maxSeries = 100  // Límite de series

    val uid = FirebaseAuth.getInstance().currentUser?.uid
    val contenidoPorGenero = genresViewModel.peliculasPorGenero.observeAsState(emptyList())
    val listState = rememberLazyListState()
    val movieTabListState = rememberLazyListState()
    val seriesTabListState = rememberLazyListState()


    var searchQuery by rememberSaveable { mutableStateOf("") }
    var movieResults by rememberSaveable { mutableStateOf<List<Peliculas>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var filterExpanded by remember { mutableStateOf(false) }
    var filterMovie by remember { mutableStateOf(false) }
    var filterSerie by remember { mutableStateOf(false) }
    val firestore = FirebaseFirestore.getInstance()

    var selectedGenre by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedGenreId by rememberSaveable { mutableStateOf<Int?>(null) }
    val options = listOf("Unordered", "Ascending", "Descending")
    val previousGenreId = remember { mutableStateOf(selectedGenreId) }

    println("selected genre id: $selectedGenreId")

    val genres = listOf(
        "Music", "Romance", "Family", "War", "Action & Adventure", "Kids", "News",
        "Reality", "Sci-Fi & Fantasy", "Soap", "Talk", "War & Politics", "TV Movie",
        "Adventure", "Fantasy", "Animation", "Drama", "Horror", "Action", "Comedy",
        "History", "Western", "Thriller", "Crime", "Science Fiction", "Mystery", "Documentary"
    )

    val genreColumns = genres.chunked(9) // Divide la lista en 3 columnas de 9 elementos

    // Observando las listas de películas/series almacenadas localmente en Room
    val peliculasPopularesOffline by offlineViewModel.peliculasPopulares.observeAsState(emptyList())
    val peliculasUltimosLanzamientosOffline by offlineViewModel.peliculasUltimosLanzamientos.observeAsState(emptyList())
    val peliculasAccionOffline by offlineViewModel.peliculasAccion.observeAsState(emptyList())
    val peliculasRomanceOffline by offlineViewModel.peliculasRomance.observeAsState(emptyList())
    val peliculasFamiliaOffline by offlineViewModel.peliculasFamilia.observeAsState(emptyList())
    val peliculasComediaOffline by offlineViewModel.peliculasComedia.observeAsState(emptyList())
    val peliculasThrillerOffline by offlineViewModel.peliculasThriller.observeAsState(emptyList())
    val peliculasHorrorOffline by offlineViewModel.peliculasHorror.observeAsState(emptyList())
    val peliculasCienciaFiccionOffline by offlineViewModel.peliculasCienciaFiccion.observeAsState(emptyList())

    // Observando las listas de series almacenadas localmente en Room
    val seriesPopularesOffline by offlineViewModel.seriesPopulares.observeAsState(emptyList())
    val seriesUltimosLanzamientosOffline by offlineViewModel.seriesUltimosLanzamientos.observeAsState(emptyList())
    val seriesAccionAventuraOffline by offlineViewModel.seriesAccionAventura.observeAsState(emptyList())
    val seriesAnimacionOffline by offlineViewModel.seriesAnimacion.observeAsState(emptyList())
    val seriesComediaOffline by offlineViewModel.seriesComedia.observeAsState(emptyList())
    val seriesCrimenOffline by offlineViewModel.seriesCrimen.observeAsState(emptyList())
    val seriesDramaOffline by offlineViewModel.seriesDrama.observeAsState(emptyList())
    val seriesFamiliaOffline by offlineViewModel.seriesFamilia.observeAsState(emptyList())
    val seriesKidsOffline by offlineViewModel.seriesKids.observeAsState(emptyList())

    val peliculas = remember { mutableStateOf<List<PeliculasEntity>>(emptyList()) }

    LaunchedEffect(Unit) {
        peliculas.value = offlineViewModel.getAllMovies()
    }


    // BackHandler para manejar el retroceso
    BackHandler {
        activity?.finish()
    }

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

    fun guardarPeliculasEnRoom() {
        // Limpiar las tablas antes de insertar nuevas películas
        offlineViewModel.limpiarPeliculas()
        offlineViewModel.limpiarSeries()

        // Insertar las primeras 10 películas de cada categoría en la base de datos
        offlineViewModel.insertPeliculasPopulares(movies.take(10))
        offlineViewModel.insertPeliculasUltimosLanzamientos(movies.take(10))
        offlineViewModel.insertPeliculasAccion(actionMovies.take(10))
        offlineViewModel.insertPeliculasRomance(romanceMovies.take(10))
        offlineViewModel.insertPeliculasFamilia(familyMovies.take(10))
        offlineViewModel.insertPeliculasComedia(comedyMovies.take(10))
        offlineViewModel.insertPeliculasThriller(thrillerMovies.take(10))
        offlineViewModel.insertPeliculasHorror(horrorMovies.take(10))
        offlineViewModel.insertPeliculasCienciaFiccion(scienceFictionMovies.take(10))

        // Insertar las primeras 10 series de cada categoría en la base de datos
        offlineViewModel.insertSeriesPopulares(series.take(10))
        offlineViewModel.insertSeriesUltimosLanzamientos(recentSeries.take(10))
        offlineViewModel.insertSeriesAccionAventura(actionAdventureSeries.take(10))
        offlineViewModel.insertSeriesAnimacion(animationSeries.take(10))
        offlineViewModel.insertSeriesComedia(comedySeries.take(10))
        offlineViewModel.insertSeriesCrimen(crimeSeries.take(10))
        offlineViewModel.insertSeriesDrama(dramaSeries.take(10))
        offlineViewModel.insertSeriesFamilia(familySeries.take(10))
        offlineViewModel.insertSeriesKids(kidsSeries.take(10))

    }

    LaunchedEffect(hayConexion) {
        if (hayConexion) {
            if (movies.size < 20 || series.size < 20) {
                if (movies.size < 20) {
                    viewModel.obtenerPeliculasPopulares()
                    viewModel.obtenerPeliculasMasRecientes()
                    viewModel.obtenerPeliculasAccion()
                    viewModel.obtenerPeliculasRomance()
                    viewModel.obtenerPeliculasFamily()
                    viewModel.obtenerPeliculasComedy()
                    viewModel.obtenerPeliculasThriller()
                    viewModel.obtenerPeliculasHorror()
                    viewModel.obtenerPeliculasCienciaFiccion()
                }

                if (series.size < 20) {
                    seriesViewModel.obtenerSeriesPopulares()
                    seriesViewModel.obtenerSeriesMasRecientes()
                    seriesViewModel.obtenerSeriesAccionAventura()
                    seriesViewModel.obtenerSeriesAnimacion()
                    seriesViewModel.obtenerSeriesComedia()
                    seriesViewModel.obtenerSeriesCrimen()
                    seriesViewModel.obtenerSeriesDrama()
                    seriesViewModel.obtenerSeriesFamilia()
                    seriesViewModel.obtenerSeriesKids()
                }
            } else {
                println("Ya hay 20 o más películas y series. No se cargarán más.")
            }
        } else {
            println("No hay conexión")
        }
    }

    val peliculasListas = movies.isNotEmpty() &&
            actionMovies.isNotEmpty() &&
            romanceMovies.isNotEmpty() &&
            familyMovies.isNotEmpty() &&
            comedyMovies.isNotEmpty() &&
            thrillerMovies.isNotEmpty() &&
            horrorMovies.isNotEmpty() &&
            scienceFictionMovies.isNotEmpty()

    val seriesListas = series.isNotEmpty() &&
            actionAdventureSeries.isNotEmpty() &&
            animationSeries.isNotEmpty() &&
            comedySeries.isNotEmpty() &&
            crimeSeries.isNotEmpty() &&
            dramaSeries.isNotEmpty() &&
            familySeries.isNotEmpty() &&
            kidsSeries.isNotEmpty()

    LaunchedEffect(peliculasListas, seriesListas) {
        if (peliculasListas && seriesListas && !listasGuardadas) {
            println("Listas completas. Guardando en Room...")
            guardarPeliculasEnRoom()
            listasGuardadas = true
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
    val recentMovieListState = rememberLazyListState()

    val seriesListState = rememberLazyListState()
    val actionadventureSerieListState = rememberLazyListState()
    val animationSerieListState = rememberLazyListState()
    val comedySerieListState = rememberLazyListState()
    val crimeListState = rememberLazyListState()
    val dramaListState = rememberLazyListState()
    val familySerieListState = rememberLazyListState()
    val kidsSerieListState = rememberLazyListState()
    val recentSerieListState = rememberLazyListState()

    // Cargar más películas cuando llegas al final de la lista de películas
    LaunchedEffect(movieListState.firstVisibleItemIndex) {
        val threshold = 5  // Umbral de carga

        // Verificar si estamos cerca del final de la lista de películas y si no se ha alcanzado el límite de 100
        if (movieListState.firstVisibleItemIndex >= (movies.size - threshold) && !isLoading && movies.size < maxMovies && hayConexion) {
            viewModel.obtenerPeliculasPopulares()  // Cargar más películas
        }
    }
    // Cargar más películas cuando llegas al final de la lista de películas
    LaunchedEffect(recentMovieListState.firstVisibleItemIndex) {
        val threshold = 10  // Umbral de carga

        // Verificar si estamos cerca del final de la lista de películas y si no se ha alcanzado el límite de 100
        if (recentMovieListState.firstVisibleItemIndex >= (recentMovies.size - threshold) && !isRecentLoading && recentMovies.size < maxMovies && hayConexion) {
            viewModel.obtenerPeliculasMasRecientes()  // Cargar más películas
        }
    }
    // Cargar más películas cuando llegas al final de la lista de películas
    LaunchedEffect(recentSerieListState.firstVisibleItemIndex) {
        val threshold = 10  // Umbral de carga

        // Verificar si estamos cerca del final de la lista de películas y si no se ha alcanzado el límite de 100
        if (recentSerieListState.firstVisibleItemIndex >= (recentSeries.size - threshold) && !isRecentSeriesLoading && recentSeries.size < maxSeries && hayConexion) {
            seriesViewModel.obtenerSeriesMasRecientes()  // Cargar más películas
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

    LaunchedEffect(hayConexion,
        actionMovies.isEmpty(),
        romanceMovies.isEmpty(),
        familyMovies.isEmpty(),
        comedyMovies.isEmpty(),
        thrillerMovies.isEmpty(),
        horrorMovies.isEmpty(),
        scienceFictionMovies.isEmpty(),
        recentMovies.isEmpty(),
        actionAdventureSeries.isEmpty(),
        animationSeries.isEmpty(),
        comedySeries.isEmpty(),
        crimeSeries.isEmpty(),
        dramaSeries.isEmpty(),
        familySeries.isEmpty(),
        kidsSeries.isEmpty(),
        recentSeries.isEmpty()) {

        if (!hayConexion){
            println("no hay conexion asi que accedemos a room en explore")
            offlineViewModel.loadPeliculasPopulares()
            offlineViewModel.loadPeliculasUltimosLanzamientos()
            offlineViewModel.loadPeliculasAccion()
            offlineViewModel.loadPeliculasRomance()
            offlineViewModel.loadPeliculasFamilia()
            offlineViewModel.loadPeliculasComedia()
            offlineViewModel.loadPeliculasThriller()
            offlineViewModel.loadPeliculasHorror()
            offlineViewModel.loadPeliculasCienciaFiccion()

            offlineViewModel.loadSeriesPopulares()
            offlineViewModel.loadSeriesUltimosLanzamientos()
            offlineViewModel.loadSeriesAccionAventura()
            offlineViewModel.loadSeriesAnimacion()
            offlineViewModel.loadSeriesComedia()
            offlineViewModel.loadSeriesCrimen()
            offlineViewModel.loadSeriesDrama()
            offlineViewModel.loadSeriesFamilia()
            offlineViewModel.loadSeriesKids()
        }
    }



    Scaffold(
        bottomBar = {
            if (uid != null) {
                BottomNavigationBar(navController, uid)
            }
        }
    ) { paddingValues ->
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
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = {
                            Text(
                                "Search for movies or series",
                                color = Color.White // Texto del label en blanco (opcional)
                            )
                        },
                        modifier = Modifier.weight(1f),
                        textStyle = LocalTextStyle.current.copy(color = Color.White), // Texto en blanco
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White,    // Borde blanco al estar seleccionado
                            unfocusedBorderColor = Color.White, // Borde blanco en estado normal
                            focusedTextColor = Color.White,      // Texto en blanco
                            unfocusedTextColor = Color.White,
                            cursorColor = Color.White,           // Cursor blanco
                            focusedTrailingIconColor = Color.White, // Ícono de búsqueda blanco
                            unfocusedTrailingIconColor = Color.White
                        ),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            capitalization = KeyboardCapitalization.Words // Esto asegura que las primeras letras de cada palabra sean mayúsculas
                        ),
                        trailingIcon = {
                            if (isSearching) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White // ProgressIndicator blanco
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Buscar"
                                )
                            }
                        }
                    )
                    if (hayConexion) {
                        IconButton(onClick = { filterExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filtrar",
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }

                    Box(modifier = Modifier.offset(x = (-16).dp, y = (46).dp)) {
                        DropdownMenu(
                            expanded = filterExpanded,
                            onDismissRequest = { filterExpanded = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(550.dp) // Mantener la caja gris con altura fija de 550.dp
                            ) {
                                // Column con scroll, ajustamos el padding inferior para evitar que se solape con el botón
                                Column(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .verticalScroll(rememberScrollState())  // Permite el desplazamiento vertical
                                        .padding(8.dp)  // Añadir padding si es necesario
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Filter by genre") },
                                        onClick = {}
                                    )
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
                                                                    // Obtener el id del género
                                                                    genresViewModel.obtenerIdGeneroPorNombre(
                                                                        genre
                                                                    ) { genreId ->
                                                                        selectedGenre = genre
                                                                        selectedGenreId =
                                                                            genreId
                                                                        println("Seleccionado: $genre, ID: $genreId")

                                                                        // Llamar a la función para obtener las películas y series para el género
                                                                        genreId?.let {
                                                                            genresViewModel.obtenerPeliculasSeriesPorGenero(
                                                                                genreId
                                                                            )
                                                                        }
                                                                    }
                                                                } else {
                                                                    selectedGenre = null
                                                                    selectedGenreId = null
                                                                }
                                                            }
                                                        )

                                                        // Mostrar el nombre del género con salto de línea solo en la visualización
                                                        Text(
                                                            text = buildAnnotatedString {
                                                                genre.split("&")
                                                                    .forEachIndexed { index, part ->
                                                                        append(part)
                                                                        if (index < genre.split(
                                                                                "&"
                                                                            ).size - 1
                                                                        ) {
                                                                            append("\n&") // Agregar salto de línea antes del "&" si es necesario
                                                                        }
                                                                    }
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    // Mostrar el filtro por tipo solo si hay un género seleccionado
                                    if (selectedGenreId != null) {
                                        HorizontalDivider()
                                        DropdownMenuItem(
                                            text = { Text("Filter by type") },
                                            onClick = {}
                                        )

                                        // Filtro para "Películas"
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
                                            Text("Movies")
                                        }

                                        // Filtro para "Series"
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
                                            Text("TV shows")
                                        }

                                    }
                                }
                            }
                        }
                    }
                }
                if (searchQuery.isNotEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Variables de estado para el menú desplegable
                        var expandedSearch by remember { mutableStateOf(false) }
                        val selectedTextSearch by rememberSaveable { mutableStateOf("Unordered") }

                        if (!hayConexion) {  // Verifica si no hay conexión
                            val filteredRoomMovies = peliculas.value.filter { movieEntity ->
                                val movie = movieEntity.pelicula  // Accedemos al Peliculas dentro de PeliculasEntity

                                val matchesType = if (filterMovie || filterSerie) {
                                    (filterMovie && !movie.esSerie) || (filterSerie && movie.esSerie)
                                } else {
                                    true
                                }

                                val matchesQuery = if (searchQuery.isNullOrEmpty()) {
                                    true
                                } else {
                                    (movie.title?.contains(searchQuery, ignoreCase = true) == true) ||
                                            (movie.name?.contains(searchQuery, ignoreCase = true) == true)
                                }

                                val matchesGenre = if (selectedGenreId == null) {
                                    true
                                } else {
                                    movie.genre_ids.contains(selectedGenreId)
                                }

                                matchesType && matchesQuery && matchesGenre
                            }

                            val sortedRoomMovies = when (selectedTextSearch) {
                                "Ascending" -> filteredRoomMovies.sortedBy { it.pelicula.popularity }
                                "Descending" -> filteredRoomMovies.sortedByDescending { it.pelicula.popularity }
                                else -> filteredRoomMovies
                            }

                            // Mostrar mensaje si no se encuentran resultados y no se está buscando
                            if (sortedRoomMovies.isEmpty() && !isSearching) {
                                Text(
                                    text = "No results found",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            } else {
                                LazyColumn(
                                    modifier = Modifier.padding(top = 120.dp)
                                ) {
                                    items(sortedRoomMovies) { movieEntity ->
                                        ContentListSearch(movie = movieEntity.pelicula, navController = navController)
                                    }
                                }
                            }
                        } else {
                            // Si hay conexión, realiza la búsqueda
                            val filteredSearchResults = movieResults.filter { movie ->
                                // Filtro por tipo: película o serie
                                val matchesType = if (filterMovie || filterSerie) {
                                    (filterMovie && !movie.esSerie) || (filterSerie && movie.esSerie)
                                } else {
                                    true // Si no se aplica filtro de tipo, permitimos cualquier tipo (película o serie)
                                }

                                // Filtro por búsqueda de título: si searchQuery está vacío, no aplicamos filtro
                                val matchesQuery = if (searchQuery.isNullOrEmpty()) {
                                    true // No filtramos por título si la búsqueda está vacía
                                } else {
                                    (movie.title?.contains(searchQuery, ignoreCase = true) == true) ||
                                            (movie.name?.contains(searchQuery, ignoreCase = true) == true)
                                }

                                // Filtro por género: si no hay género seleccionado, no filtramos por género
                                val matchesGenre = if (selectedGenreId == null) {
                                    true // Si no se ha seleccionado un género, no se filtra por género
                                } else {
                                    movie.genre_ids.contains(selectedGenreId) // Si se seleccionó un género, lo filtramos
                                }

                                matchesType && matchesQuery && matchesGenre
                            }

                            val sortedSearchResults = when (selectedTextSearch) {
                                "Ascending" -> filteredSearchResults.sortedBy { it.popularity }
                                "Descending" -> filteredSearchResults.sortedByDescending { it.popularity }
                                else -> filteredSearchResults
                            }

                            // Mostrar mensaje si no se encuentran resultados y no se está buscando
                            if (sortedSearchResults.isEmpty() && !isSearching) {
                                Text(
                                    text = "No results found",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            } else {
                                LazyColumn(
                                    modifier = Modifier.padding(top = 120.dp),
                                ) {
                                    items(sortedSearchResults) { movie ->
                                        ContentListSearch(movie = movie, navController = navController)
                                    }
                                }
                            }
                        }
                    }
                }
                else if (selectedGenreId != null) {
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        var selectedText by rememberSaveable { mutableStateOf("Unordered") } // Valor inicial
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .align(Alignment.TopCenter)
                        ) {
                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = {
                                    expanded = !expanded
                                } // Se encarga de abrir y cerrar el menú
                            ) {
                                OutlinedTextField(
                                    value = selectedText,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = {
                                        Text(
                                            "Sort by popularity",
                                            color = Color.White // Texto del label en blanco
                                        )
                                    },
                                    textStyle = LocalTextStyle.current.copy(color = Color.White),
                                    shape = MaterialTheme.shapes.large,
                                    colors = TextFieldDefaults.outlinedTextFieldColors(
                                        focusedBorderColor = Color.White,    // Borde blanco cuando está enfocado
                                        unfocusedBorderColor = Color.White,  // Borde blanco cuando no está enfocado
                                    ),
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            tint = Color.White,
                                            contentDescription = "Expandir"
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                )

                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    options.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option) },
                                            onClick = {
                                                selectedText = option
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Filtramos los elementos según el género y tipo (película o serie)
                        val filteredItems = contenidoPorGenero.value.filter {
                            (filterMovie && !it.esSerie) || (filterSerie && it.esSerie) || (!filterMovie && !filterSerie)
                        }

                        // Ordenamos los elementos según la opción seleccionada en el menú desplegable
                        val sortedItems = when (selectedText) {
                            "Ascending" -> filteredItems.sortedBy { it.popularity }
                            "Descending" -> filteredItems.sortedByDescending { it.popularity }
                            else -> filteredItems // Sin orden
                        }

                        if (sortedItems.isEmpty()) {
                            val noResultsText = when {
                                filterMovie -> "No movies of this genre were found"
                                filterSerie -> "No TV shows of this genre were found"
                                else -> "No results were found"
                            }
                            Box(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = noResultsText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontSize = 16.sp,
                                    color = Color.White,
                                    modifier = Modifier.align(Alignment.Center) // Alineamos el texto al centro dentro del Box
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.padding(top = 120.dp),
                                state = listState
                            ) {
                                items(sortedItems) { movie ->
                                    ContentListSearch(
                                        movie = movie,
                                        navController = navController
                                    )
                                }
                            }

                            // Desplazar al principio cuando se seleccione un nuevo género
                            LaunchedEffect(selectedGenreId) {
                                if (selectedGenreId != previousGenreId.value) {
                                    listState.scrollToItem(0)
                                }
                                previousGenreId.value = selectedGenreId
                            }
                        }
                    }
                } else {
                    TabRow(
                        selectedTabIndex = if (selectedTab == "Movies") 0 else 1, // Cambia el índice dependiendo de la pestaña seleccionada
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = Color.Transparent,
                        contentColor = Color.White, // Color del texto y del indicador
                        indicator = { tabPositions ->
                            SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[if (selectedTab == "Movies") 0 else 1]),
                                color = Color.White // Color del indicador (línea inferior)
                            )
                        }
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == title,
                                onClick = {
                                    selectedTab = title // Cambiar la pestaña seleccionada
                                },
                                text = {
                                    Text(
                                        title,
                                        color = Color.White // Color del texto explícito
                                    )
                                },
                                selectedContentColor = Color.White, // Color cuando está seleccionado
                                unselectedContentColor = Color.White.copy(alpha = 0.7f) // Color cuando no está seleccionado (ligeramente transparente)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    val listStateTab = if (selectedTab == "Movies") movieTabListState else seriesTabListState
                    LazyColumn(
                        state = listStateTab,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        val isLoadingExplore = movies.isEmpty() && peliculasPopularesOffline.isEmpty()

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
                        } else {
                            println("selectedTab: $selectedTab")
                            if (selectedTab == "Movies") {
                                if (movies.isNotEmpty() || peliculasPopularesOffline.isNotEmpty()) {
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
                                                    .fillMaxSize()
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
                                                ContentListExplore(
                                                    if (hayConexion) movies else peliculasPopularesOffline.map { it.pelicula },
                                                    navController,
                                                    movieListState,
                                                )
                                            }
                                        }
                                    }
                                    item { Spacer(modifier = Modifier.height(25.dp)) }
                                }

                                if (recentMovies.isNotEmpty() || peliculasUltimosLanzamientosOffline.isNotEmpty()) {
                                    item {
                                        Text(
                                            "Latest Releases",
                                            color = Color.White,
                                            style = MaterialTheme.typography.headlineMedium
                                        )
                                    }
                                    item { Spacer(modifier = Modifier.height(8.dp)) }
                                    item {
                                        ContentListExplore(
                                            if (hayConexion) recentMovies else peliculasUltimosLanzamientosOffline.map { it.pelicula },
                                            navController,
                                            recentMovieListState,
                                        )
                                    }
                                }

                                if (actionMovies.isNotEmpty() || peliculasAccionOffline.isNotEmpty()) {
                                    item {
                                        Text(
                                            "Action movies",
                                            color = Color.White,
                                            style = MaterialTheme.typography.headlineMedium
                                        )
                                    }
                                    item { Spacer(modifier = Modifier.height(8.dp)) }
                                    item {
                                        ContentListExplore(
                                            if (hayConexion) actionMovies else peliculasAccionOffline.map { it.pelicula },
                                            navController,
                                            actionMovieListState,
                                        )
                                    }
                                }

                                if (romanceMovies.isNotEmpty() || peliculasRomanceOffline.isNotEmpty()) {
                                    item {
                                        Text(
                                            "Romance movies",
                                            color = Color.White,
                                            style = MaterialTheme.typography.headlineMedium
                                        )
                                    }
                                    item { Spacer(modifier = Modifier.height(8.dp)) }
                                    item {
                                        ContentListExplore(
                                            if (hayConexion) romanceMovies else peliculasRomanceOffline.map { it.pelicula },
                                            navController,
                                            romanceMovieListState,
                                        )
                                    }
                                }

                                if (familyMovies.isNotEmpty() || peliculasFamiliaOffline.isNotEmpty()) {
                                    item {
                                        Text(
                                            "Family movies",
                                            color = Color.White,
                                            style = MaterialTheme.typography.headlineMedium
                                        )
                                    }
                                    item { Spacer(modifier = Modifier.height(8.dp)) }
                                    item {
                                        ContentListExplore(
                                            if (hayConexion) familyMovies else peliculasFamiliaOffline.map { it.pelicula },
                                            navController,
                                            familyMovieListState,
                                        )
                                    }
                                }

                                if (comedyMovies.isNotEmpty() || peliculasComediaOffline.isNotEmpty()) {
                                    item {
                                        Text(
                                            "Comedy movies",
                                            color = Color.White,
                                            style = MaterialTheme.typography.headlineMedium
                                        )
                                    }
                                    item { Spacer(modifier = Modifier.height(8.dp)) }
                                    item {
                                        ContentListExplore(
                                            if (hayConexion) comedyMovies else peliculasComediaOffline.map { it.pelicula },
                                            navController,
                                            comedyMovieListState,
                                        )
                                    }
                                }

                                if (thrillerMovies.isNotEmpty() || peliculasThrillerOffline.isNotEmpty()) {
                                    item {
                                        Text(
                                            "Thriller movies",
                                            color = Color.White,
                                            style = MaterialTheme.typography.headlineMedium
                                        )
                                    }
                                    item { Spacer(modifier = Modifier.height(8.dp)) }
                                    item {
                                        ContentListExplore(
                                            if (hayConexion) thrillerMovies else peliculasThrillerOffline.map { it.pelicula },
                                            navController,
                                            thrillerMovieListState,
                                        )
                                    }
                                }

                                if (horrorMovies.isNotEmpty() || peliculasHorrorOffline.isNotEmpty()) {
                                    item {
                                        Text(
                                            "Horror movies",
                                            color = Color.White,
                                            style = MaterialTheme.typography.headlineMedium
                                        )
                                    }
                                    item { Spacer(modifier = Modifier.height(8.dp)) }
                                    item {
                                        ContentListExplore(
                                            if (hayConexion) horrorMovies else peliculasHorrorOffline.map { it.pelicula },
                                            navController,
                                            horrorMovieListState,
                                        )
                                    }
                                }

                                if (scienceFictionMovies.isNotEmpty() || peliculasCienciaFiccionOffline.isNotEmpty()) {
                                    item {
                                        Text(
                                            "Science Fiction movies",
                                            color = Color.White,
                                            style = MaterialTheme.typography.headlineMedium
                                        )
                                    }
                                    item { Spacer(modifier = Modifier.height(8.dp)) }
                                    item {
                                        ContentListExplore(
                                            if (hayConexion) scienceFictionMovies else peliculasCienciaFiccionOffline.map { it.pelicula },
                                            navController,
                                            sciencieFictionMovieListState,
                                        )
                                    }
                                }
                            } else {
                                if (series.isNotEmpty() || seriesPopularesOffline.isNotEmpty()) {
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
                                                    .fillMaxSize()
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
                                                ContentListExplore(
                                                    if (hayConexion) series else seriesPopularesOffline.map { it.pelicula },
                                                    navController,
                                                    seriesListState,
                                                )
                                            }
                                        }
                                    }
                                    item { Spacer(modifier = Modifier.height(25.dp)) }
                                }
                                if (recentSeries.isNotEmpty() || seriesUltimosLanzamientosOffline.isNotEmpty()) {
                                    item {
                                        Text(
                                            "Latest Releases",
                                            color = Color.White,
                                            style = MaterialTheme.typography.headlineMedium
                                        )
                                    }
                                    item { Spacer(modifier = Modifier.height(8.dp)) }
                                    item {
                                        ContentListExplore(
                                            if (hayConexion) recentSeries else seriesUltimosLanzamientosOffline.map { it.pelicula },
                                            navController,
                                            recentSerieListState,
                                        )
                                    }
                                }

                                if (actionAdventureSeries.isNotEmpty() || seriesAccionAventuraOffline.isNotEmpty()) {
                                    item {
                                        Text(
                                            "Action & Adventure series",
                                            color = Color.White,
                                            style = MaterialTheme.typography.headlineMedium
                                        )
                                    }
                                    item { Spacer(modifier = Modifier.height(8.dp)) }
                                    item {
                                        ContentListExplore(
                                            if (hayConexion) actionAdventureSeries else seriesAccionAventuraOffline.map { it.pelicula },
                                            navController,
                                            actionadventureSerieListState,
                                        )
                                    }
                                }

                                if (animationSeries.isNotEmpty() || seriesAnimacionOffline.isNotEmpty()) {
                                    item {
                                        Text(
                                            "Animation series",
                                            color = Color.White,
                                            style = MaterialTheme.typography.headlineMedium
                                        )
                                    }
                                    item { Spacer(modifier = Modifier.height(8.dp)) }
                                    item {
                                        ContentListExplore(
                                            if (hayConexion) animationSeries else seriesAnimacionOffline.map { it.pelicula },
                                            navController,
                                            animationSerieListState,
                                        )
                                    }
                                }

                                if (comedySeries.isNotEmpty() || seriesComediaOffline.isNotEmpty()) {
                                    item {
                                        Text(
                                            "Comedy series",
                                            color = Color.White,
                                            style = MaterialTheme.typography.headlineMedium
                                        )
                                    }
                                    item { Spacer(modifier = Modifier.height(8.dp)) }
                                    item {
                                        ContentListExplore(
                                            if (hayConexion) comedySeries else seriesComediaOffline.map { it.pelicula },
                                            navController,
                                            comedySerieListState,
                                        )
                                    }
                                }

                                if (crimeSeries.isNotEmpty() || seriesCrimenOffline.isNotEmpty()) {
                                    item {
                                        Text(
                                            "Crime series",
                                            color = Color.White,
                                            style = MaterialTheme.typography.headlineMedium
                                        )
                                    }
                                    item { Spacer(modifier = Modifier.height(8.dp)) }
                                    item {
                                        ContentListExplore(
                                            if (hayConexion) crimeSeries else seriesCrimenOffline.map { it.pelicula },
                                            navController,
                                            crimeListState,
                                        )
                                    }
                                }

                                if (dramaSeries.isNotEmpty() || seriesDramaOffline.isNotEmpty()) {
                                    item {
                                        Text(
                                            "Drama series",
                                            color = Color.White,
                                            style = MaterialTheme.typography.headlineMedium
                                        )
                                    }
                                    item { Spacer(modifier = Modifier.height(8.dp)) }
                                    item {
                                        ContentListExplore(
                                            if (hayConexion) dramaSeries else seriesDramaOffline.map { it.pelicula },
                                            navController,
                                            dramaListState,
                                        )
                                    }
                                }

                                if (familySeries.isNotEmpty() || seriesFamiliaOffline.isNotEmpty()) {
                                    item {
                                        Text(
                                            "Family series",
                                            color = Color.White,
                                            style = MaterialTheme.typography.headlineMedium
                                        )
                                    }
                                    item { Spacer(modifier = Modifier.height(8.dp)) }
                                    item {
                                        ContentListExplore(
                                            if (hayConexion) familySeries else seriesFamiliaOffline.map { it.pelicula },
                                            navController,
                                            familySerieListState,
                                        )
                                    }
                                }

                                if (kidsSeries.isNotEmpty() || seriesKidsOffline.isNotEmpty()) {
                                    item {
                                        Text(
                                            "Kids series",
                                            color = Color.White,
                                            style = MaterialTheme.typography.headlineMedium
                                        )
                                    }
                                    item { Spacer(modifier = Modifier.height(8.dp)) }
                                    item {
                                        ContentListExplore(
                                            if (hayConexion) kidsSeries else seriesKidsOffline.map { it.pelicula },
                                            navController,
                                            kidsSerieListState,
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
}