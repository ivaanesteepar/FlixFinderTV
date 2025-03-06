package com.example.flixfindertv.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flixfindertv.models.Comentario
import com.example.flixfindertv.models.Peliculas
import com.example.flixfindertv.network.RetrofitClient
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// ViewModel que maneja la obtención de las peliculas/series
class MoviesViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()  // Instancia de Firestore

    private var _listaPeliculas = MutableLiveData<List<Peliculas>>(emptyList())
    val listaPeliculas: LiveData<List<Peliculas>> = _listaPeliculas

    private var _listaSeries = MutableLiveData<List<Peliculas>>(emptyList())
    val listaSeries: LiveData<List<Peliculas>> = _listaSeries

    private var _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private var _isLoadingSeries = MutableLiveData<Boolean>(false)
    val isLoadingSeries: LiveData<Boolean> = _isLoadingSeries

    var lastVisible: DocumentSnapshot? = null
    var lastVisible2: DocumentSnapshot? = null


    fun obtenerPeliculasPopularesLocal() {
        if (isLoading.value == true) return

        _isLoading.value = true
        val peliculasList = mutableListOf<Peliculas>()

        viewModelScope.launch {
            try {
                var query = db.collection("peliculas")
                    .orderBy("popularity", com.google.firebase.firestore.Query.Direction.DESCENDING) // Ordenar por popularidad (de mayor a menor)
                    .limit(20)

                lastVisible?.let {
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
                    lastVisible = querySnapshot.documents.last()
                }

                if (_listaPeliculas.value.isNullOrEmpty()) {
                    _listaPeliculas.postValue(peliculasList)
                } else {
                    _listaPeliculas.value = _listaPeliculas.value.orEmpty() + peliculasList
                }

            } catch (e: Exception) {
                Log.e("Error", "Error al obtener las películas desde Firebase", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }


    fun obtenerSeriesPopularesLocal() {
        if (isLoadingSeries.value == true) return

        _isLoadingSeries.value = true
        val seriesList = mutableListOf<Peliculas>()

        viewModelScope.launch {
            try {
                var query = db.collection("series")
                    .orderBy("popularity", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .limit(20)

                lastVisible2?.let {
                    query = query.startAfter(it)
                }

                val querySnapshot = query.get().await()

                for (document in querySnapshot.documents) {
                    val serie = document.toObject(Peliculas::class.java)
                    serie?.let {
                        seriesList.add(it)
                    }
                }

                if (querySnapshot.documents.isNotEmpty()) {
                    lastVisible2 = querySnapshot.documents.last()
                }

                if (_listaSeries.value.isNullOrEmpty()) {
                    _listaSeries.postValue(seriesList)
                } else {
                    _listaSeries.value = _listaSeries.value.orEmpty() + seriesList
                }

            } catch (e: Exception) {
                Log.e("Error", "Error al obtener las series desde Firebase", e)
            } finally {
                _isLoadingSeries.postValue(false)
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
}
