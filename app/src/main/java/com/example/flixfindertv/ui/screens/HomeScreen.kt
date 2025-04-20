package com.example.flixfindertv.ui.screens

import android.app.Activity
import android.app.Application
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.flixfindertv.ui.viewmodels.GenresViewModel
import com.example.flixfindertv.ui.viewmodels.MoviesViewModel
import com.example.flixfindertv.utils.ContentListExplore
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import com.example.flixfindertv.R
import com.example.flixfindertv.room.entities.Genero1MovieEntity
import com.example.flixfindertv.room.entities.Genero2MovieEntity
import com.example.flixfindertv.room.entities.ProximasMovieEntity
import com.example.flixfindertv.ui.viewmodels.ConexionViewModel
import com.example.flixfindertv.ui.viewmodels.OfflineViewModel
import com.example.flixfindertv.ui.viewmodels.OfflineViewModelFactory
import com.example.flixfindertv.utils.BottomNavigationBar
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    navController: NavController,
    moviesViewModel: MoviesViewModel,
    conexionViewModel: ConexionViewModel
) {
    val genresViewModel: GenresViewModel = viewModel()
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    val hayConexion by conexionViewModel.conexionEstablecida.collectAsState()
    var prevHayConexion by remember { mutableStateOf(hayConexion) }
    val context = LocalContext.current
    val activity = context as? Activity
    val offlineViewModel: OfflineViewModel = viewModel(
        factory = OfflineViewModelFactory(context.applicationContext as Application)
    )

    val maxMovies = 100
    var contenidoOfflineCargado by rememberSaveable { mutableStateOf(false) }

    val isLoadingGenero1 by genresViewModel.isLoadingGenero1.observeAsState(false)
    val isLoadingGenero2 by genresViewModel.isLoadingGenero2.observeAsState(false)
    val isLoadingPeliculasProximas by moviesViewModel.isLoadingProximas.observeAsState(false)

    // Listas de películas/series para los géneros
    val peliculasGenero1 by genresViewModel.peliculasGenero1.observeAsState(emptyList())
    val peliculasGenero2 by genresViewModel.peliculasGenero2.observeAsState(emptyList())
    val peliculasProximas by moviesViewModel.listaPeliculasProximas.observeAsState(emptyList())

    // Listas de películas/series para los géneros
    val peliculasGenero1Offline by offlineViewModel.listaPeliculasGenero1.observeAsState(emptyList())
    val peliculasGenero2Offline by offlineViewModel.listaPeliculasGenero2.observeAsState(emptyList())
    val peliculasProximasOffline by offlineViewModel.listaPeliculasProximas.observeAsState(emptyList())

    // Nombres de los géneros
    val nombreGenero1 = genresViewModel.nombreGenero1.value
    val nombreGenero2 = genresViewModel.nombreGenero2.value

    // LazyListState para manejar el estado de desplazamiento
    val listStateGenero1 = rememberLazyListState()
    val listStateGenero2 = rememberLazyListState()
    val listStatePeliculasProximas = rememberLazyListState()

    val prevGenero1 = remember { mutableStateOf(genresViewModel.nombreGenero1.value) }
    val prevGenero2 = remember { mutableStateOf(genresViewModel.nombreGenero2.value) }

    var apiKeyTmdb by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope() // Obtén el CoroutineScope

    println("MI ID ACTUAL ES: $uid")


    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val key = moviesViewModel.getTmdbApiKey()
            if (!key.isNullOrEmpty()) {
                apiKeyTmdb = key
                println("API Key obtenida correctamente: $apiKeyTmdb")
            }
        }
    }

    LaunchedEffect(Unit) {
        moviesViewModel.obtenerContenidoProximo()
    }

    LaunchedEffect(uid) {
        if (uid != null) {
            genresViewModel.cargarGenerosFavoritos(uid)
        }
    }

    // BackHandler para manejar el retroceso
    BackHandler {
        activity?.finish()
    }

    LaunchedEffect(key1 = genresViewModel.nombreGenero1.value) {
        val nuevoGenero = genresViewModel.nombreGenero1.value
        if (nuevoGenero != prevGenero1.value) {
            prevGenero1.value = nuevoGenero // Actualizamos el estado previo
            if (!nuevoGenero.isNullOrEmpty()) {
                genresViewModel.limpiarPeliculasGenero1()
                listStateGenero1.scrollToItem(0)
                if (uid != null) {
                    genresViewModel.obtenerPeliculasYSeriesGenero1(uid)
                }
            }
        }
    }

    LaunchedEffect(key1 = genresViewModel.nombreGenero2.value) {
        val nuevoGenero = genresViewModel.nombreGenero2.value
        if (nuevoGenero != prevGenero2.value) {
            prevGenero2.value = nuevoGenero // Actualizamos el estado previo
            if (!nuevoGenero.isNullOrEmpty()) {
                genresViewModel.limpiarPeliculasGenero2()
                listStateGenero2.scrollToItem(0)
                if (uid != null) {
                    genresViewModel.obtenerPeliculasYSeriesGenero2(uid)
                }
            }
        }
    }

    // Efectos de carga para manejar la carga incremental de las listas
    LaunchedEffect(listStateGenero1.firstVisibleItemIndex) {
        val threshold = 10  // Umbral de carga

        if (listStateGenero1.firstVisibleItemIndex >= (peliculasGenero1.size - threshold) && !isLoadingGenero1 && peliculasGenero1.size < maxMovies && hayConexion) {
            if (uid != null) {
                genresViewModel.obtenerPeliculasYSeriesGenero1(uid)
            }
        }
    }
    LaunchedEffect(listStateGenero2.firstVisibleItemIndex) {
        val threshold = 10  // Umbral de carga

        if (listStateGenero2.firstVisibleItemIndex >= (peliculasGenero2.size - threshold) && !isLoadingGenero2 && peliculasGenero2.size < maxMovies && hayConexion) {
            if (uid != null) {
                genresViewModel.obtenerPeliculasYSeriesGenero2(uid)
            }
        }
    }
    LaunchedEffect(listStatePeliculasProximas.firstVisibleItemIndex) {
        val threshold = 10  // Umbral de carga

        if (listStatePeliculasProximas.firstVisibleItemIndex >= (peliculasProximas.size - threshold) && !isLoadingPeliculasProximas && peliculasProximas.size < maxMovies && hayConexion) {
            if (uid != null) {
                moviesViewModel.obtenerContenidoProximo()
            }
        }
    }

    fun guardarPeliculasEnRoom() {
        // Limpiar las tablas antes de insertar nuevas películas
        offlineViewModel.limpiarPeliculasGenero1()
        offlineViewModel.limpiarPeliculasGenero2()
        offlineViewModel.limpiarPeliculasProximas()

        // Insertar las primeras 20 películas de cada categoría en las tablas correspondientes
        offlineViewModel.insertPeliculasGenero1(
            peliculasGenero1.take(10).map { Genero1MovieEntity(idMovieEntity = it.id, pelicula = it) }
        )

        offlineViewModel.insertPeliculasGenero2(
            peliculasGenero2.take(10).map { Genero2MovieEntity(idMovieEntity = it.id, pelicula = it) }
        )

        offlineViewModel.insertPeliculasProximas(
            peliculasProximas.take(10).map { ProximasMovieEntity(idMovieEntity = it.id, pelicula = it) }
        )
    }

    LaunchedEffect(hayConexion) {
        if (!contenidoOfflineCargado) {
            println("no hay conexion asi que accedemos a room")
            offlineViewModel.loadGenero1Movies()
            offlineViewModel.loadGenero2Movies()
            offlineViewModel.loadProximasMovies()
            contenidoOfflineCargado = true
        }
    }

    LaunchedEffect(hayConexion) {
        println("hayConexion home: $hayConexion, prevHayConexion home: $prevHayConexion")
        if (hayConexion != prevHayConexion) {
            listStateGenero1.scrollToItem(0)
            listStateGenero2.scrollToItem(0)
            listStatePeliculasProximas.scrollToItem(0)
            prevHayConexion = hayConexion
        }
    }

    // Efecto de guardado solo cuando las listas estén completas y no estén guardadas
    LaunchedEffect(peliculasGenero1.isNotEmpty() && peliculasGenero2.isNotEmpty() && peliculasProximas.isNotEmpty()) {
        if (peliculasGenero1.isNotEmpty() && peliculasGenero2.isNotEmpty() && peliculasProximas.isNotEmpty()) {
            println("Listas completas. Guardando en Room...")
            guardarPeliculasEnRoom()
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
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(paddingValues)
            ) {
                Text(
                    text = "You might be interested...",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 24.sp),
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .padding(top = 26.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    if (nombreGenero1.isNotEmpty() && nombreGenero2.isNotEmpty()) {
                        // Películas del primer género
                        if (peliculasGenero1.isNotEmpty() || peliculasGenero1Offline.isNotEmpty()) {
                            Text(
                                text = nombreGenero1,
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 20.sp),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            ContentListExplore(
                                movies = if (hayConexion) peliculasGenero1 else peliculasGenero1Offline.map { it.pelicula },
                                navController = navController,
                                listState = listStateGenero1
                            )
                        }

                        // Películas del segundo género
                        if (peliculasGenero2.isNotEmpty() || peliculasGenero2Offline.isNotEmpty()) {
                            Text(
                                text = nombreGenero2,
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 20.sp),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            ContentListExplore(
                                movies = if (hayConexion) peliculasGenero2 else peliculasGenero2Offline.map { it.pelicula },
                                navController = navController,
                                listState = listStateGenero2
                            )
                        }

                        // Próximas películas
                        if (peliculasProximas.isNotEmpty() || peliculasProximasOffline.isNotEmpty()) {
                            Text(
                                text = "Next Releases",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 20.sp),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            ContentListExplore(
                                movies = if (hayConexion) peliculasProximas else peliculasProximasOffline.map { it.pelicula },
                                navController = navController,
                                listState = listStatePeliculasProximas
                            )
                        }
                    }
                }
            }
        }
    }
}
