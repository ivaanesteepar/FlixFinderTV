package com.example.flixfindertv.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flixfindertv.models.Peliculas
import com.example.flixfindertv.network.RetrofitClient
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class MoviesViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()  // Instancia de Firestore

    private var _listaPeliculas = MutableLiveData<List<Peliculas>>(emptyList())
    val listaPeliculas: LiveData<List<Peliculas>> = _listaPeliculas

    private var _listaSeries = MutableLiveData<List<Peliculas>>(emptyList())
    val listaSeries: LiveData<List<Peliculas>> = _listaSeries

    private var _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    // Función para guardar las películas en Firestore
    private fun saveMoviesToFirestore(movies: List<Peliculas>) {
        // Obtener la referencia a la colección de Firestore
        val moviesRef = db.collection("peliculas")

        // Guardar cada película en Firestore
        for (movie in movies) {
            val movieData = hashMapOf(
                "id" to movie.id,
                "tituloOriginal" to movie.tituloOriginal,
                "nombreAlternativo" to movie.nombreAlternativo,
                "descripcion" to movie.descripcion,
                "fecha" to movie.fecha,
                "portada" to movie.portada,
                "votoPromedio" to movie.votoPromedio,
                "numVotos" to movie.numVotos,
                "generos" to movie.generos,
                "esAdulto" to movie.esAdulto,
                "banner" to movie.banner
            )

            // Comprobar si la película ya existe en Firestore
            moviesRef.document(movie.id).get()
                .addOnSuccessListener { document ->
                    if (!document.exists()) {
                        // Si no existe, se guarda la película
                        moviesRef.document(movie.id).set(movieData)
                            .addOnSuccessListener {
                                println("Película guardada con éxito: ${movie.titulo}")
                            }
                            .addOnFailureListener { e ->
                                println("Error al guardar la película: ${e.message}")
                            }
                    } else {
                        // Si ya existe, no se hace nada
                        println("La película ya está en la base de datos: ${movie.titulo}")
                    }
                }
                .addOnFailureListener { e ->
                    println("Error al comprobar si la película existe: ${e.message}")
                }
        }
    }

    // Función para guardar las series en Firestore
    private fun saveSeriesToFirestore(series: List<Peliculas>) {
        // Obtener la referencia a la colección de Firestore
        val seriesRef = db.collection("series")

        // Guardar cada serie en Firestore
        for (serie in series) {
            val serieData = hashMapOf(
                "id" to serie.id,
                "tituloOriginal" to serie.tituloOriginal,
                "nombreAlternativo" to serie.nombreAlternativo,
                "descripcion" to serie.descripcion,
                "fecha" to serie.fecha,
                "portada" to serie.portada,
                "votoPromedio" to serie.votoPromedio,
                "numVotos" to serie.numVotos,
                "generos" to serie.generos,
                "esAdulto" to serie.esAdulto,
                "banner" to serie.banner
            )

            // Comprobar si la serie ya existe en Firestore
            seriesRef.document(serie.id).get()
                .addOnSuccessListener { document ->
                    if (!document.exists()) {
                        // Si no existe, se guarda la serie
                        seriesRef.document(serie.id).set(serieData)
                            .addOnSuccessListener {
                                println("Serie guardada con éxito: ${serie.titulo}")
                            }
                            .addOnFailureListener { e ->
                                println("Error al guardar la serie: ${e.message}")
                            }
                    } else {
                        // Si ya existe, no se hace nada
                        println("La serie ya está en la base de datos: ${serie.titulo}")
                    }
                }
                .addOnFailureListener { e ->
                    println("Error al comprobar si la serie existe: ${e.message}")
                }
        }
    }



    // Función para obtener todas las películas populares
    fun obtenerPeliculasPopulares(apiKey: String, language: String) {
        _isLoading.value = true
        var page = 1
        val totalPagesToLoad = 5  // Limitar a 5 páginas (100 películas)
        val allMovies = mutableListOf<Peliculas>()

        viewModelScope.launch {
            while (page <= totalPagesToLoad) {
                val response = RetrofitClient.webService.getPopularMovies(apiKey, language, page)

                if (response.isSuccessful) {
                    val movieResponse = response.body()
                    movieResponse?.let {
                        allMovies.addAll(it.resultados)  // Añadir películas de la página actual
                        saveMoviesToFirestore(it.resultados)
                    }
                }
                page++
            }

            _listaPeliculas.postValue(allMovies)  // Actualizamos la lista con las películas
            _isLoading.postValue(false)  // Indicamos que hemos terminado de cargar
        }
    }

    // Función para obtener todas las series populares
    fun obtenerSeriesPopulares(apiKey: String, language: String) {
        _isLoading.value = true
        var page = 1
        val totalPagesToLoad = 5  // Limitar a 5 páginas (100 series)
        val allSeries = mutableListOf<Peliculas>()

        viewModelScope.launch {
            while (page <= totalPagesToLoad) {
                val response = RetrofitClient.webService.getPopularTVShows(apiKey, language, page)

                if (response.isSuccessful) {
                    val seriesResponse = response.body()
                    println("Series Response: $seriesResponse")  // Imprimir la respuesta
                    seriesResponse?.let {
                        allSeries.addAll(it.resultados)  // Añadir series de la página actual
                        saveSeriesToFirestore(it.resultados)  // Guardar las series en Firestore
                    }
                }
                page++
            }

            _listaSeries.postValue(allSeries)  // Actualizamos la lista con las series
            _isLoading.postValue(false)  // Indicamos que hemos terminado de cargar
        }
    }
}
