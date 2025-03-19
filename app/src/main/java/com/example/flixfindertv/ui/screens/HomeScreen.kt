package com.example.flixfindertv.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.flixfindertv.ui.viewmodels.GenresViewModel
import com.example.flixfindertv.ui.viewmodels.MoviesViewModel
import com.example.flixfindertv.utils.MovieList
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.flixfindertv.ui.viewmodels.ConexionViewModel
import com.example.flixfindertv.utils.BottomNavigationBar

@Composable
fun HomeScreen(
    navController: NavHostController,
    moviesViewModel: MoviesViewModel,
    conexionViewModel: ConexionViewModel
) {
    val genresViewModel: GenresViewModel = viewModel()
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    val hayConexion by conexionViewModel.conexionEstablecida.collectAsState()

    val maxSeries = 100
    val maxMovies = 100

    val isLoadingGenero1 by genresViewModel.isLoadingGenero1.observeAsState(false)
    val isLoadingGenero2 by genresViewModel.isLoadingGenero2.observeAsState(false)

    // Listas de películas/series para los géneros
    val peliculasGenero1 by genresViewModel.peliculasGenero1.observeAsState(emptyList())
    val peliculasGenero2 by genresViewModel.peliculasGenero2.observeAsState(emptyList())

    // Nombres de los géneros
    val nombreGenero1 = rememberSaveable { genresViewModel.nombreGenero1 }
    val nombreGenero2 = rememberSaveable { genresViewModel.nombreGenero2 }

    // LazyListState para manejar el estado de desplazamiento
    val listStateGenero1 = rememberLazyListState()
    val listStateGenero2 = rememberLazyListState()

    // Obtener y observar los géneros favoritos
    LaunchedEffect(uid) {
        if (uid != null) {
            genresViewModel.obtenerGenerosFavoritos(uid) // Obtener los géneros favoritos del usuario
        }
    }

    LaunchedEffect(nombreGenero1) {
        if (nombreGenero1.value.isNotEmpty()) {
            genresViewModel.resetPeliculasGenero(1)  // Limpiar las películas del género 1
            if (uid != null) {
                genresViewModel.obtenerPeliculasYSeriesGenero1(uid)
            }
        }
    }

    LaunchedEffect(nombreGenero2) {
        if (nombreGenero2.value.isNotEmpty()) {
            genresViewModel.resetPeliculasGenero(2)  // Limpiar las películas del género 2
            if (uid != null) {
                genresViewModel.obtenerPeliculasYSeriesGenero2(uid)
            }
        }
    }

    println("genero1: $nombreGenero1")
    println("genero2: $nombreGenero2")


    // Efectos de carga para manejar la carga incremental de las listas
    LaunchedEffect(listStateGenero1.firstVisibleItemIndex) {
        val threshold = 10  // Umbral de carga

        if (listStateGenero1.firstVisibleItemIndex >= (peliculasGenero1.size - threshold) && !isLoadingGenero1 && peliculasGenero1.size < maxSeries && hayConexion) {
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

    Scaffold(
        bottomBar = {
            if (uid != null) {
                BottomNavigationBar(navController, uid)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(paddingValues)
        ) {
            Text(
                text = "You might be interested...",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 24.sp),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))

            // Mostrar películas y series del primer género
            if (peliculasGenero1.isNotEmpty() && nombreGenero1.value.isNotEmpty()) {
                Text(
                    text = nombreGenero1.value, // Usamos el nombre del género
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 20.sp),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Usamos LazyRow para mostrar las películas y series
                MovieList(
                    movies = peliculasGenero1,
                    navController = navController,
                    listState = listStateGenero1,
                    //isSerie = true // Asegúrate de pasar el valor adecuado de isSerie aquí
                )
            }

            // Mostrar películas y series del segundo género
            if (peliculasGenero2.isNotEmpty() && nombreGenero2.value.isNotEmpty()) {
                Text(
                    text = nombreGenero2.value, // Usamos el nombre del género
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 20.sp),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Usamos LazyRow para mostrar las películas y series
                MovieList(
                    movies = peliculasGenero2,
                    navController = navController,
                    listState = listStateGenero2,
                    //isSerie = true // Asegúrate de pasar el valor adecuado de isSerie aquí
                )
            }
        }
    }
}
