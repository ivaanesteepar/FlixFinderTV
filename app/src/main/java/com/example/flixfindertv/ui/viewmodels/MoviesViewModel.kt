package com.example.flixfindertv.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flixfindertv.models.Peliculas
import com.example.flixfindertv.network.RetrofitClient
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// ViewModel que maneja la obtención de las peliculas/series
class MoviesViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()  // Instancia de Firestore

    private var _listaPeliculas = MutableLiveData<List<Peliculas>>(emptyList())
    val listaPeliculas: LiveData<List<Peliculas>> = _listaPeliculas

    private var _listaPeliculasAccion = MutableLiveData<List<Peliculas>>(emptyList())
    val listaPeliculasAccion: LiveData<List<Peliculas>> = _listaPeliculasAccion

    private var _listaPeliculasRomance = MutableLiveData<List<Peliculas>>(emptyList())
    val listaPeliculasRomance: LiveData<List<Peliculas>> = _listaPeliculasRomance

    private var _listaPeliculasFamily = MutableLiveData<List<Peliculas>>(emptyList())
    val listaPeliculasFamily: LiveData<List<Peliculas>> = _listaPeliculasFamily

    private var _listaPeliculasComedy = MutableLiveData<List<Peliculas>>(emptyList())
    val listaPeliculasComedy: LiveData<List<Peliculas>> = _listaPeliculasComedy

    private var _listaPeliculasThriller = MutableLiveData<List<Peliculas>>(emptyList())
    val listaPeliculasThriller: LiveData<List<Peliculas>> = _listaPeliculasThriller

    private var _listaPeliculasHorror = MutableLiveData<List<Peliculas>>(emptyList())
    val listaPeliculasHorror: LiveData<List<Peliculas>> = _listaPeliculasHorror

    private var _listaPeliculasScienceFiction = MutableLiveData<List<Peliculas>>(emptyList())
    val listaPeliculasScienceFiction: LiveData<List<Peliculas>> = _listaPeliculasScienceFiction


    private var _isLoadingPeliculas = MutableLiveData<Boolean>(false)
    val isLoadingPeliculas: LiveData<Boolean> = _isLoadingPeliculas

    private var _isLoadingAction = MutableLiveData<Boolean>(false)
    val isLoadingAction: LiveData<Boolean> = _isLoadingAction

    private var _isLoadingRomance = MutableLiveData<Boolean>(false)
    val isLoadingRomance: LiveData<Boolean> = _isLoadingRomance

    private var _isLoadingFamily = MutableLiveData<Boolean>(false)
    val isLoadingFamily: LiveData<Boolean> = _isLoadingFamily

    private var _isLoadingComedy = MutableLiveData<Boolean>(false)
    val isLoadingComedy: LiveData<Boolean> = _isLoadingComedy

    private var _isLoadingThriller = MutableLiveData<Boolean>(false)
    val isLoadingThriller: LiveData<Boolean> = _isLoadingThriller

    private var _isLoadingHorror = MutableLiveData<Boolean>(false)
    val isLoadingHorror: LiveData<Boolean> = _isLoadingHorror

    private var _isLoadingScienceFiction = MutableLiveData<Boolean>(false)
    val isLoadingScienceFiction: LiveData<Boolean> = _isLoadingScienceFiction

    var lastVisiblePeliculas: DocumentSnapshot? = null
    var lastVisibleAction: DocumentSnapshot? = null
    var lastVisibleRomance: DocumentSnapshot? = null
    var lastVisibleFamily: DocumentSnapshot? = null
    var lastVisibleComedy: DocumentSnapshot? = null
    var lastVisibleThriller: DocumentSnapshot? = null
    var lastVisibleHorror: DocumentSnapshot? = null
    var lastVisibleScienceFiction: DocumentSnapshot? = null

    private val moviePages = mutableMapOf<Int, Int>() // Paginación por ID de película
    private val tvPages = mutableMapOf<Int, Int>() // Paginación por ID de serie
    private val RESULTS_PER_PAGE = 20 // Cantidad de resultados por página


    fun obtenerPeliculasPopulares() {
        if (isLoadingPeliculas.value == true) return

        _isLoadingPeliculas.value = true
        val peliculasList = mutableListOf<Peliculas>()

        viewModelScope.launch {
            try {
                var query = db.collection("peliculas")
                    .orderBy("popularity", com.google.firebase.firestore.Query.Direction.DESCENDING) // Ordenar por popularidad (de mayor a menor)
                    .limit(20)

                lastVisiblePeliculas?.let {
                    query = query.startAfter(it)
                }

                val querySnapshot = query.get().await()

                for (document in querySnapshot.documents) {
                    val pelicula = document.toObject(Peliculas::class.java)
                    pelicula?.let {
                        peliculasList.add(it)
                        println("Película obtenida: Título = ${it.titulo}, Id = ${it.id}, Portada = ${it.poster_path}")
                    }
                }

                if (querySnapshot.documents.isNotEmpty()) {
                    lastVisiblePeliculas = querySnapshot.documents.last()
                }

                if (_listaPeliculas.value.isNullOrEmpty()) {
                    _listaPeliculas.postValue(peliculasList)
                } else {
                    _listaPeliculas.value = _listaPeliculas.value.orEmpty() + peliculasList
                }

            } catch (e: Exception) {
                Log.e("Error", "Error al obtener las películas desde Firebase", e)
            } finally {
                _isLoadingPeliculas.postValue(false)
            }
        }
    }

    fun obtenerPeliculasAccion() {
        if (_isLoadingAction.value == true) return

        _isLoadingAction.value = true
        val listaAccion = mutableListOf<Peliculas>()

        viewModelScope.launch {
            try {
                // Obtener el ID del género "Action"
                val generoQuery = db.collection("generos")
                    .whereEqualTo("name", "Action") // Buscar el género por nombre
                    .get().await()
                if (generoQuery.documents.isEmpty()) {
                    Log.e("Error", "No se encontró el género 'Action'")
                    _isLoadingAction.postValue(false)
                    return@launch
                }
                val idGeneroAction = generoQuery.documents.first().getLong("id") ?: run {
                    Log.e("Error", "El ID del género 'Action' es nulo")
                    _isLoadingAction.postValue(false)
                    return@launch
                }
                // Consultar las películas que contienen este género en su lista de genre_ids
                var consulta = db.collection("peliculas")
                    .whereArrayContains("genre_ids", idGeneroAction) // Filtrar por ID del género "Action"
                    .limit(20)

                lastVisibleAction?.let {
                    consulta = consulta.startAfter(it)
                }

                val resultado = consulta.get().await()

                for (documento in resultado.documents) {
                    val pelicula = documento.toObject(Peliculas::class.java)
                    pelicula?.let {
                        listaAccion.add(it)
                        println("Película obtenida: Título = ${it.titulo}, Id = ${it.id}, Portada = ${it.poster_path}")
                    }
                }

                if (resultado.documents.isNotEmpty()) {
                    lastVisibleAction = resultado.documents.last()
                }

                if (_listaPeliculasAccion.value.isNullOrEmpty()) {
                    _listaPeliculasAccion.postValue(listaAccion)
                } else {
                    _listaPeliculasAccion.value = _listaPeliculasAccion.value.orEmpty() + listaAccion
                }

            } catch (e: Exception) {
                Log.e("Error", "Error al obtener las películas de acción desde Firebase", e)
            } finally {
                _isLoadingAction.postValue(false)
            }
        }
    }

    fun obtenerPeliculasRomance() {
        if (_isLoadingRomance.value == true) return

        _isLoadingRomance.value = true
        val listaRomance = mutableListOf<Peliculas>()

        viewModelScope.launch {
            try {
                // Obtener el ID del género "Action"
                val generoQuery = db.collection("generos")
                    .whereEqualTo("name", "Romance") // Buscar el género por nombre
                    .get().await()
                if (generoQuery.documents.isEmpty()) {
                    Log.e("Error", "No se encontró el género 'Romance'")
                    _isLoadingRomance.postValue(false)
                    return@launch
                }
                val idGeneroAction = generoQuery.documents.first().getLong("id") ?: run {
                    Log.e("Error", "El ID del género 'Romance' es nulo")
                    _isLoadingRomance.postValue(false)
                    return@launch
                }
                // Consultar las películas que contienen este género en su lista de genre_ids
                var consulta = db.collection("peliculas")
                    .whereArrayContains("genre_ids", idGeneroAction) // Filtrar por ID del género "Action"
                    .limit(20)

                lastVisibleRomance?.let {
                    consulta = consulta.startAfter(it)
                }

                val resultado = consulta.get().await()

                for (documento in resultado.documents) {
                    val pelicula = documento.toObject(Peliculas::class.java)
                    pelicula?.let {
                        listaRomance.add(it)
                        println("Película obtenida: Título = ${it.titulo}, Id = ${it.id}, Portada = ${it.poster_path}")
                    }
                }

                if (resultado.documents.isNotEmpty()) {
                    lastVisibleRomance = resultado.documents.last()
                }

                if (_listaPeliculasRomance.value.isNullOrEmpty()) {
                    _listaPeliculasRomance.postValue(listaRomance)
                } else {
                    _listaPeliculasRomance.value = _listaPeliculasRomance.value.orEmpty() + listaRomance
                }

            } catch (e: Exception) {
                Log.e("Error", "Error al obtener las películas de acción desde Firebase", e)
            } finally {
                _isLoadingRomance.postValue(false)
            }
        }
    }

    fun obtenerPeliculasFamily() {
        if (_isLoadingFamily.value == true) return

        _isLoadingFamily.value = true
        val listaFamily = mutableListOf<Peliculas>()

        viewModelScope.launch {
            try {
                // Obtener el ID del género "Family"
                val generoQuery = db.collection("generos")
                    .whereEqualTo("name", "Family") // Buscar el género por nombre
                    .get().await()
                if (generoQuery.documents.isEmpty()) {
                    Log.e("Error", "No se encontró el género 'Family'")
                    _isLoadingFamily.postValue(false)
                    return@launch
                }
                val idGeneroFamily = generoQuery.documents.first().getLong("id") ?: run {
                    Log.e("Error", "El ID del género 'Family' es nulo")
                    _isLoadingFamily.postValue(false)
                    return@launch
                }
                // Consultar las películas que contienen este género en su lista de genre_ids
                var consulta = db.collection("peliculas")
                    .whereArrayContains("genre_ids", idGeneroFamily) // Filtrar por ID del género "Family"
                    .limit(20)

                lastVisibleFamily?.let {
                    consulta = consulta.startAfter(it)
                }

                val resultado = consulta.get().await()

                for (documento in resultado.documents) {
                    val pelicula = documento.toObject(Peliculas::class.java)
                    pelicula?.let {
                        listaFamily.add(it)
                        println("Película obtenida: Título = ${it.titulo}, Id = ${it.id}, Portada = ${it.poster_path}")
                    }
                }

                if (resultado.documents.isNotEmpty()) {
                    lastVisibleFamily = resultado.documents.last()
                }

                if (_listaPeliculasFamily.value.isNullOrEmpty()) {
                    _listaPeliculasFamily.postValue(listaFamily)
                } else {
                    _listaPeliculasFamily.value = _listaPeliculasFamily.value.orEmpty() + listaFamily
                }

            } catch (e: Exception) {
                Log.e("Error", "Error al obtener las películas de 'Family' desde Firebase", e)
            } finally {
                _isLoadingFamily.postValue(false)
            }
        }
    }

    fun obtenerPeliculasComedy() {
        if (_isLoadingComedy.value == true) return

        _isLoadingComedy.value = true
        val listaComedy = mutableListOf<Peliculas>()

        viewModelScope.launch {
            try {
                // Obtener el ID del género "Comedy"
                val generoQuery = db.collection("generos")
                    .whereEqualTo("name", "Comedy") // Buscar el género por nombre
                    .get().await()
                if (generoQuery.documents.isEmpty()) {
                    Log.e("Error", "No se encontró el género 'Comedy'")
                    _isLoadingComedy.postValue(false)
                    return@launch
                }
                val idGeneroComedy = generoQuery.documents.first().getLong("id") ?: run {
                    Log.e("Error", "El ID del género 'Comedy' es nulo")
                    _isLoadingComedy.postValue(false)
                    return@launch
                }
                // Consultar las películas que contienen este género en su lista de genre_ids
                var consulta = db.collection("peliculas")
                    .whereArrayContains("genre_ids", idGeneroComedy) // Filtrar por ID del género "Comedy"
                    .limit(20)

                lastVisibleComedy?.let {
                    consulta = consulta.startAfter(it)
                }

                val resultado = consulta.get().await()

                for (documento in resultado.documents) {
                    val pelicula = documento.toObject(Peliculas::class.java)
                    pelicula?.let {
                        listaComedy.add(it)
                        println("Película obtenida: Título = ${it.titulo}, Id = ${it.id}, Portada = ${it.poster_path}")
                    }
                }

                if (resultado.documents.isNotEmpty()) {
                    lastVisibleComedy = resultado.documents.last()
                }

                if (_listaPeliculasComedy.value.isNullOrEmpty()) {
                    _listaPeliculasComedy.postValue(listaComedy)
                } else {
                    _listaPeliculasComedy.value = _listaPeliculasComedy.value.orEmpty() + listaComedy
                }

            } catch (e: Exception) {
                Log.e("Error", "Error al obtener las películas de 'Comedy' desde Firebase", e)
            } finally {
                _isLoadingComedy.postValue(false)
            }
        }
    }

    fun obtenerPeliculasThriller() {
        if (_isLoadingThriller.value == true) return

        _isLoadingThriller.value = true
        val listaThriller = mutableListOf<Peliculas>()

        viewModelScope.launch {
            try {
                // Obtener el ID del género "Thriller"
                val generoQuery = db.collection("generos")
                    .whereEqualTo("name", "Thriller") // Buscar el género por nombre
                    .get().await()
                if (generoQuery.documents.isEmpty()) {
                    Log.e("Error", "No se encontró el género 'Thriller'")
                    _isLoadingThriller.postValue(false)
                    return@launch
                }
                val idGeneroThriller = generoQuery.documents.first().getLong("id") ?: run {
                    Log.e("Error", "El ID del género 'Thriller' es nulo")
                    _isLoadingThriller.postValue(false)
                    return@launch
                }
                // Consultar las películas que contienen este género en su lista de genre_ids
                var consulta = db.collection("peliculas")
                    .whereArrayContains("genre_ids", idGeneroThriller) // Filtrar por ID del género "Thriller"
                    .limit(20)

                lastVisibleThriller?.let {
                    consulta = consulta.startAfter(it)
                }

                val resultado = consulta.get().await()

                for (documento in resultado.documents) {
                    val pelicula = documento.toObject(Peliculas::class.java)
                    pelicula?.let {
                        listaThriller.add(it)
                        println("Película obtenida: Título = ${it.titulo}, Id = ${it.id}, Portada = ${it.poster_path}")
                    }
                }

                if (resultado.documents.isNotEmpty()) {
                    lastVisibleThriller = resultado.documents.last()
                }

                if (_listaPeliculasThriller.value.isNullOrEmpty()) {
                    _listaPeliculasThriller.postValue(listaThriller)
                } else {
                    _listaPeliculasThriller.value = _listaPeliculasThriller.value.orEmpty() + listaThriller
                }

            } catch (e: Exception) {
                Log.e("Error", "Error al obtener las películas de 'Thriller' desde Firebase", e)
            } finally {
                _isLoadingThriller.postValue(false)
            }
        }
    }

    fun obtenerPeliculasHorror() {
        if (_isLoadingHorror.value == true) return

        _isLoadingHorror.value = true
        val listaHorror = mutableListOf<Peliculas>()

        viewModelScope.launch {
            try {
                // Obtener el ID del género "Horror"
                val generoQuery = db.collection("generos")
                    .whereEqualTo("name", "Horror") // Buscar el género por nombre
                    .get().await()
                if (generoQuery.documents.isEmpty()) {
                    Log.e("Error", "No se encontró el género 'Horror'")
                    _isLoadingHorror.postValue(false)
                    return@launch
                }
                val idGeneroHorror = generoQuery.documents.first().getLong("id") ?: run {
                    Log.e("Error", "El ID del género 'Horror' es nulo")
                    _isLoadingHorror.postValue(false)
                    return@launch
                }
                // Consultar las películas que contienen este género en su lista de genre_ids
                var consulta = db.collection("peliculas")
                    .whereArrayContains("genre_ids", idGeneroHorror) // Filtrar por ID del género "Horror"
                    .limit(20)

                lastVisibleHorror?.let {
                    consulta = consulta.startAfter(it)
                }

                val resultado = consulta.get().await()

                for (documento in resultado.documents) {
                    val pelicula = documento.toObject(Peliculas::class.java)
                    pelicula?.let {
                        listaHorror.add(it)
                        println("Película obtenida: Título = ${it.titulo}, Id = ${it.id}, Portada = ${it.poster_path}")
                    }
                }

                if (resultado.documents.isNotEmpty()) {
                    lastVisibleHorror = resultado.documents.last()
                }

                if (_listaPeliculasHorror.value.isNullOrEmpty()) {
                    _listaPeliculasHorror.postValue(listaHorror)
                } else {
                    _listaPeliculasHorror.value = _listaPeliculasHorror.value.orEmpty() + listaHorror
                }

            } catch (e: Exception) {
                Log.e("Error", "Error al obtener las películas de 'Horror' desde Firebase", e)
            } finally {
                _isLoadingHorror.postValue(false)
            }
        }
    }

    fun obtenerPeliculasCienciaFiccion() {
        if (_isLoadingScienceFiction.value == true) return

        _isLoadingScienceFiction.value = true
        val listaHorror = mutableListOf<Peliculas>()

        viewModelScope.launch {
            try {
                // Obtener el ID del género "Horror"
                val generoQuery = db.collection("generos")
                    .whereEqualTo("name", "Science Fiction") // Buscar el género por nombre
                    .get().await()
                if (generoQuery.documents.isEmpty()) {
                    Log.e("Error", "No se encontró el género 'Horror'")
                    _isLoadingScienceFiction.postValue(false)
                    return@launch
                }
                val idGeneroHorror = generoQuery.documents.first().getLong("id") ?: run {
                    Log.e("Error", "El ID del género 'Horror' es nulo")
                    _isLoadingScienceFiction.postValue(false)
                    return@launch
                }
                // Consultar las películas que contienen este género en su lista de genre_ids
                var consulta = db.collection("peliculas")
                    .whereArrayContains("genre_ids", idGeneroHorror) // Filtrar por ID del género "Horror"
                    .limit(20)

                lastVisibleScienceFiction?.let {
                    consulta = consulta.startAfter(it)
                }

                val resultado = consulta.get().await()

                for (documento in resultado.documents) {
                    val pelicula = documento.toObject(Peliculas::class.java)
                    pelicula?.let {
                        listaHorror.add(it)
                        println("Película obtenida: Título = ${it.titulo}, Id = ${it.id}, Portada = ${it.poster_path}")
                    }
                }

                if (resultado.documents.isNotEmpty()) {
                    lastVisibleScienceFiction = resultado.documents.last()
                }

                if (_listaPeliculasScienceFiction.value.isNullOrEmpty()) {
                    _listaPeliculasScienceFiction.postValue(listaHorror)
                } else {
                    _listaPeliculasScienceFiction.value = _listaPeliculasScienceFiction.value.orEmpty() + listaHorror
                }

            } catch (e: Exception) {
                Log.e("Error", "Error al obtener las películas de 'Horror' desde Firebase", e)
            } finally {
                _isLoadingScienceFiction.postValue(false)
            }
        }
    }

    fun searchMovie(firestore: FirebaseFirestore, query: String, onResult: (List<Peliculas>) -> Unit) {
        if (query.isNotEmpty()) {
            val peliculasRef = firestore.collection("peliculas")
            val seriesRef = firestore.collection("series")

            val peliculasQuery = peliculasRef
                .orderBy("title")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .get()

            val seriesQuery = seriesRef
                .orderBy("name")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .get()

            peliculasQuery.addOnSuccessListener { peliculasResult ->
                seriesQuery.addOnSuccessListener { seriesResult ->
                    val peliculas = peliculasResult.documents.mapNotNull { document ->
                        document.toObject(Peliculas::class.java)
                    }

                    val series = seriesResult.documents.mapNotNull { document ->
                        document.toObject(Peliculas::class.java)
                    }

                    val combinedResults = peliculas + series
                    onResult(combinedResults)
                }
            }
                .addOnFailureListener {
                    onResult(emptyList())
                }
        } else {
            onResult(emptyList())
        }
    }

    fun fetchNextSimilarMovies(apiKey: String, movieId: Int, callback: (List<Peliculas>) -> Unit) {
        val currentPage = moviePages.getOrDefault(movieId, 1) // Obtener la página actual

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.api.getSimilarMovies(movieId, apiKey, page = currentPage)
                val results = response.results.take(RESULTS_PER_PAGE) // Tomar solo 20 resultados

                if (results.isNotEmpty()) {
                    moviePages[movieId] = currentPage + 1 // Pasar a la siguiente página
                }

                callback(results) // Devolver los resultados
            } catch (e: Exception) {
                println("Error obteniendo películas similares: ${e.message}")
            }
        }
    }

    fun fetchNextSimilarSeries(apiKey: String, tvId: Int, callback: (List<Peliculas>) -> Unit) {
        val currentPage = tvPages.getOrDefault(tvId, 1) // Obtener la página actual

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.api.getSimilarSeries(tvId, apiKey, page = currentPage)
                val results = response.results.take(RESULTS_PER_PAGE) // Tomar solo 20 resultados

                if (results.isNotEmpty()) {
                    tvPages[tvId] = currentPage + 1 // Pasar a la siguiente página
                }

                callback(results) // Devolver los resultados
            } catch (e: Exception) {
                println("Error obteniendo series similares: ${e.message}")
            }
        }
    }
}
