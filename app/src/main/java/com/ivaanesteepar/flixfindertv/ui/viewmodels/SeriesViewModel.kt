package com.ivaanesteepar.flixfindertv.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivaanesteepar.flixfindertv.models.Peliculas
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class SeriesViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private var _listaSeries = MutableLiveData<List<Peliculas>>(emptyList())
    val listaSeries: LiveData<List<Peliculas>> = _listaSeries

    private var _listaSeriesActionAdventure = MutableLiveData<List<Peliculas>>(emptyList())
    val listaSeriesAccionAventura: LiveData<List<Peliculas>> = _listaSeriesActionAdventure

    private var _listaSeriesAnimation = MutableLiveData<List<Peliculas>>(emptyList())
    val listaSeriesAnimacion: LiveData<List<Peliculas>> = _listaSeriesAnimation

    private var _listaSeriesComedy = MutableLiveData<List<Peliculas>>(emptyList())
    val listaSeriesComedia: LiveData<List<Peliculas>> = _listaSeriesComedy

    private var _listaSeriesCrime = MutableLiveData<List<Peliculas>>(emptyList())
    val listaSeriesCrimen: LiveData<List<Peliculas>> = _listaSeriesCrime

    private var _listaSeriesDrama = MutableLiveData<List<Peliculas>>(emptyList())
    val listaSeriesDrama: LiveData<List<Peliculas>> = _listaSeriesDrama

    private var _listaSeriesFamily = MutableLiveData<List<Peliculas>>(emptyList())
    val listaSeriesFamily: LiveData<List<Peliculas>> = _listaSeriesFamily

    private var _listaSeriesKids = MutableLiveData<List<Peliculas>>(emptyList())
    val listaSeriesKids: LiveData<List<Peliculas>> = _listaSeriesKids

    private var _listaSeriesRecientes = MutableLiveData<List<Peliculas>>(emptyList())
    val listaSeriesRecientes: LiveData<List<Peliculas>> = _listaSeriesRecientes


    private var _isLoadingSeries = MutableLiveData<Boolean>(false)
    val isLoadingSeries: LiveData<Boolean> = _isLoadingSeries

    private var _isLoadingActionAdventure = MutableLiveData<Boolean>(false)
    val isLoadingActionAdventure: LiveData<Boolean> = _isLoadingActionAdventure

    private var _isLoadingAnimation = MutableLiveData<Boolean>(false)
    val isLoadingAnimation: LiveData<Boolean> = _isLoadingAnimation

    private var _isLoadingComedySeries = MutableLiveData<Boolean>(false)
    val isLoadingComedySeries: LiveData<Boolean> = _isLoadingComedySeries

    private var _isLoadingCrime = MutableLiveData<Boolean>(false)
    val isLoadingCrime: LiveData<Boolean> = _isLoadingCrime

    private var _isLoadingDrama = MutableLiveData<Boolean>(false)
    val isLoadingDrama: LiveData<Boolean> = _isLoadingDrama

    private var _isLoadingFamilySeries = MutableLiveData<Boolean>(false)
    val isLoadingFamilySeries: LiveData<Boolean> = _isLoadingFamilySeries

    private var _isLoadingKids = MutableLiveData<Boolean>(false)
    val isLoadingKidsSeries: LiveData<Boolean> = _isLoadingKids

    private val _isLoadingRecentSeries = MutableLiveData(false)
    val isLoadingRecentSeries: LiveData<Boolean> = _isLoadingRecentSeries

    private var lastVisibleSeries: DocumentSnapshot? = null
    private var lastVisibleActionAdventure: DocumentSnapshot? = null
    private var lastVisibleAnimation: DocumentSnapshot? = null
    private var lastVisibleComedySeries: DocumentSnapshot? = null
    private var lastVisibleCrime: DocumentSnapshot? = null
    private var lastVisibleDrama: DocumentSnapshot? = null
    private var lastVisibleFamilySeries: DocumentSnapshot? = null
    private var lastVisibleKids: DocumentSnapshot? = null
    private var lastVisibleRecentSeries: DocumentSnapshot? = null


    fun obtenerSeriesMasRecientes() {
        if (_isLoadingRecentSeries.value == true) return

        _isLoadingRecentSeries.value = true
        val listaSeriesRecientes = mutableListOf<Peliculas>()

        viewModelScope.launch {
            try {
                // Obtener la fecha actual en formato String (por ejemplo: "2024-04-10")
                val currentDateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
                    Date()
                )

                // Consultar las películas más recientes hasta la fecha actual, ordenadas por fecha de lanzamiento
                var consulta = db.collection("series")
                    .whereLessThanOrEqualTo("release_date_series", currentDateString) // Filtrar por películas cuyo release_date sea <= a la fecha actual
                    .orderBy("release_date_series", Query.Direction.DESCENDING) // Ordenar por fecha de lanzamiento
                    .limit(20)

                lastVisibleRecentSeries?.let {
                    consulta = consulta.startAfter(it)
                }

                val resultado = consulta.get().await()

                for (documento in resultado.documents) {
                    val pelicula = documento.toObject(Peliculas::class.java)
                    pelicula?.let {
                        listaSeriesRecientes.add(it)
                        println("Serie obtenida: Título = ${it.titulo}, Id = ${it.id}, Portada = ${it.poster_path}")
                    }
                }

                if (resultado.documents.isNotEmpty()) {
                    lastVisibleRecentSeries = resultado.documents.last()
                }

                if (_listaSeriesRecientes.value.isNullOrEmpty()) {
                    _listaSeriesRecientes.postValue(listaSeriesRecientes)
                } else {
                    _listaSeriesRecientes.value = _listaSeriesRecientes.value.orEmpty() + listaSeriesRecientes
                }

            } catch (e: Exception) {
                Log.e("Error", "Error al obtener las series más recientes hasta la fecha actual desde Firebase", e)
            } finally {
                _isLoadingRecentSeries.postValue(false)
            }
        }
    }

    fun obtenerSeriesPopulares() {
        if (isLoadingSeries.value == true) return

        _isLoadingSeries.value = true
        val seriesList = mutableListOf<Peliculas>()

        viewModelScope.launch {
            try {
                var query = db.collection("series")
                    .orderBy("popularity", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .limit(20)

                lastVisibleSeries?.let {
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
                    lastVisibleSeries = querySnapshot.documents.last()
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

    fun obtenerSeriesAccionAventura() {
        if (_isLoadingActionAdventure.value == true) return

        _isLoadingActionAdventure.value = true
        val listaAccion = mutableListOf<Peliculas>()

        viewModelScope.launch {
            try {
                // Obtener el ID del género "Action"
                val generoQuery = db.collection("generos")
                    .whereEqualTo("name", "Action & Adventure") // Buscar el género por nombre
                    .get().await()
                if (generoQuery.documents.isEmpty()) {
                    Log.e("Error", "No se encontró el género 'Action'")
                    _isLoadingActionAdventure.postValue(false)
                    return@launch
                }
                val idGeneroAction = generoQuery.documents.first().getLong("id") ?: run {
                    Log.e("Error", "El ID del género 'Action' es nulo")
                    _isLoadingActionAdventure.postValue(false)
                    return@launch
                }
                // Consultar las películas que contienen este género en su lista de genre_ids
                var consulta = db.collection("series")
                    .whereArrayContains("genre_ids", idGeneroAction) // Filtrar por ID del género "Action"
                    .limit(20)

                lastVisibleActionAdventure?.let {
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
                    lastVisibleActionAdventure = resultado.documents.last()
                }

                if (_listaSeriesActionAdventure.value.isNullOrEmpty()) {
                    _listaSeriesActionAdventure.postValue(listaAccion)
                } else {
                    _listaSeriesActionAdventure.value = _listaSeriesActionAdventure.value.orEmpty() + listaAccion
                }

            } catch (e: Exception) {
                Log.e("Error", "Error al obtener las películas de acción desde Firebase", e)
            } finally {
                _isLoadingActionAdventure.postValue(false)
            }
        }
    }

    fun obtenerSeriesAnimacion() {
        if (_isLoadingAnimation.value == true) return

        _isLoadingAnimation.value = true
        val listaAccion = mutableListOf<Peliculas>()

        viewModelScope.launch {
            try {
                // Obtener el ID del género "Action"
                val generoQuery = db.collection("generos")
                    .whereEqualTo("name", "Animation") // Buscar el género por nombre
                    .get().await()
                if (generoQuery.documents.isEmpty()) {
                    Log.e("Error", "No se encontró el género 'Action'")
                    _isLoadingAnimation.postValue(false)
                    return@launch
                }
                val idGeneroAction = generoQuery.documents.first().getLong("id") ?: run {
                    Log.e("Error", "El ID del género 'Action' es nulo")
                    _isLoadingAnimation.postValue(false)
                    return@launch
                }
                // Consultar las películas que contienen este género en su lista de genre_ids
                var consulta = db.collection("series")
                    .whereArrayContains("genre_ids", idGeneroAction) // Filtrar por ID del género "Action"
                    .limit(20)

                lastVisibleAnimation?.let {
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
                    lastVisibleAnimation = resultado.documents.last()
                }

                if (_listaSeriesAnimation.value.isNullOrEmpty()) {
                    _listaSeriesAnimation.postValue(listaAccion)
                } else {
                    _listaSeriesAnimation.value = _listaSeriesAnimation.value.orEmpty() + listaAccion
                }

            } catch (e: Exception) {
                Log.e("Error", "Error al obtener las películas de acción desde Firebase", e)
            } finally {
                _isLoadingAnimation.postValue(false)
            }
        }
    }

    fun obtenerSeriesComedia() {
        if (_isLoadingComedySeries.value == true) return

        _isLoadingComedySeries.value = true
        val listaComedia = mutableListOf<Peliculas>()

        viewModelScope.launch {
            try {
                val generoQuery = db.collection("generos")
                    .whereEqualTo("name", "Comedy")
                    .get().await()

                if (generoQuery.documents.isEmpty()) {
                    Log.e("Error", "No se encontró el género 'Comedy'")
                    _isLoadingComedySeries.postValue(false)
                    return@launch
                }

                val idGenero = generoQuery.documents.first().getLong("id") ?: run {
                    Log.e("Error", "El ID del género 'Comedy' es nulo")
                    _isLoadingComedySeries.postValue(false)
                    return@launch
                }

                var consulta = db.collection("series")
                    .whereArrayContains("genre_ids", idGenero)
                    .limit(20)

                lastVisibleComedySeries?.let {
                    consulta = consulta.startAfter(it)
                }

                val resultado = consulta.get().await()

                for (documento in resultado.documents) {
                    val pelicula = documento.toObject(Peliculas::class.java)
                    pelicula?.let {
                        listaComedia.add(it)
                    }
                }

                if (resultado.documents.isNotEmpty()) {
                    lastVisibleComedySeries = resultado.documents.last()
                }

                _listaSeriesComedy.postValue(_listaSeriesComedy.value.orEmpty() + listaComedia)

            } catch (e: Exception) {
                Log.e("Error", "Error al obtener las series de comedia desde Firebase", e)
            } finally {
                _isLoadingComedySeries.postValue(false)
            }
        }
    }

    fun obtenerSeriesCrimen() {
        if (_isLoadingCrime.value == true) return

        _isLoadingCrime.value = true
        val listaCrimen = mutableListOf<Peliculas>()

        viewModelScope.launch {
            try {
                val generoQuery = db.collection("generos")
                    .whereEqualTo("name", "Crime")
                    .get().await()

                if (generoQuery.documents.isEmpty()) {
                    Log.e("Error", "No se encontró el género 'Crime'")
                    _isLoadingCrime.postValue(false)
                    return@launch
                }

                val idGenero = generoQuery.documents.first().getLong("id") ?: run {
                    Log.e("Error", "El ID del género 'Crime' es nulo")
                    _isLoadingCrime.postValue(false)
                    return@launch
                }

                var consulta = db.collection("series")
                    .whereArrayContains("genre_ids", idGenero)
                    .limit(20)

                lastVisibleCrime?.let {
                    consulta = consulta.startAfter(it)
                }

                val resultado = consulta.get().await()

                for (documento in resultado.documents) {
                    val pelicula = documento.toObject(Peliculas::class.java)
                    pelicula?.let {
                        listaCrimen.add(it)
                    }
                }

                if (resultado.documents.isNotEmpty()) {
                    lastVisibleCrime = resultado.documents.last()
                }

                _listaSeriesCrime.postValue(_listaSeriesCrime.value.orEmpty() + listaCrimen)

            } catch (e: Exception) {
                Log.e("Error", "Error al obtener las series de crimen desde Firebase", e)
            } finally {
                _isLoadingCrime.postValue(false)
            }
        }
    }

    fun obtenerSeriesDrama() {
        if (_isLoadingDrama.value == true) return

        _isLoadingDrama.value = true
        val listaDrama = mutableListOf<Peliculas>()

        viewModelScope.launch {
            try {
                val generoQuery = db.collection("generos")
                    .whereEqualTo("name", "Drama")
                    .get().await()

                if (generoQuery.documents.isEmpty()) {
                    Log.e("Error", "No se encontró el género 'Drama'")
                    _isLoadingDrama.postValue(false)
                    return@launch
                }

                val idGenero = generoQuery.documents.first().getLong("id") ?: run {
                    Log.e("Error", "El ID del género 'Drama' es nulo")
                    _isLoadingDrama.postValue(false)
                    return@launch
                }

                var consulta = db.collection("series")
                    .whereArrayContains("genre_ids", idGenero)
                    .limit(20)

                lastVisibleDrama?.let {
                    consulta = consulta.startAfter(it)
                }

                val resultado = consulta.get().await()

                for (documento in resultado.documents) {
                    val pelicula = documento.toObject(Peliculas::class.java)
                    pelicula?.let {
                        listaDrama.add(it)
                    }
                }

                if (resultado.documents.isNotEmpty()) {
                    lastVisibleDrama = resultado.documents.last()
                }

                _listaSeriesDrama.postValue(_listaSeriesDrama.value.orEmpty() + listaDrama)

            } catch (e: Exception) {
                Log.e("Error", "Error al obtener las series de drama desde Firebase", e)
            } finally {
                _isLoadingDrama.postValue(false)
            }
        }
    }

    fun obtenerSeriesFamilia() {
        if (_isLoadingFamilySeries.value == true) return

        _isLoadingFamilySeries.value = true
        val listaFamilia = mutableListOf<Peliculas>()

        viewModelScope.launch {
            try {
                val generoQuery = db.collection("generos")
                    .whereEqualTo("name", "Family")
                    .get().await()

                if (generoQuery.documents.isEmpty()) {
                    Log.e("Error", "No se encontró el género 'Family'")
                    _isLoadingFamilySeries.postValue(false)
                    return@launch
                }

                val idGenero = generoQuery.documents.first().getLong("id") ?: run {
                    Log.e("Error", "El ID del género 'Family' es nulo")
                    _isLoadingFamilySeries.postValue(false)
                    return@launch
                }

                var consulta = db.collection("series")
                    .whereArrayContains("genre_ids", idGenero)
                    .limit(20)

                lastVisibleFamilySeries?.let {
                    consulta = consulta.startAfter(it)
                }

                val resultado = consulta.get().await()

                for (documento in resultado.documents) {
                    val pelicula = documento.toObject(Peliculas::class.java)
                    pelicula?.let {
                        listaFamilia.add(it)
                    }
                }

                if (resultado.documents.isNotEmpty()) {
                    lastVisibleFamilySeries = resultado.documents.last()
                }

                _listaSeriesFamily.postValue(_listaSeriesFamily.value.orEmpty() + listaFamilia)

            } catch (e: Exception) {
                Log.e("Error", "Error al obtener las series de familia desde Firebase", e)
            } finally {
                _isLoadingFamilySeries.postValue(false)
            }
        }
    }

    fun obtenerSeriesKids() {
        if (_isLoadingKids.value == true) return

        _isLoadingKids.value = true
        val listaKids = mutableListOf<Peliculas>()

        viewModelScope.launch {
            try {
                val generoQuery = db.collection("generos")
                    .whereEqualTo("name", "Kids")
                    .get().await()

                if (generoQuery.documents.isEmpty()) {
                    Log.e("Error", "No se encontró el género 'Drama'")
                    _isLoadingKids.postValue(false)
                    return@launch
                }

                val idGenero = generoQuery.documents.first().getLong("id") ?: run {
                    Log.e("Error", "El ID del género 'Drama' es nulo")
                    _isLoadingKids.postValue(false)
                    return@launch
                }

                var consulta = db.collection("series")
                    .whereArrayContains("genre_ids", idGenero)
                    .limit(20)

                lastVisibleKids?.let {
                    consulta = consulta.startAfter(it)
                }

                val resultado = consulta.get().await()

                for (documento in resultado.documents) {
                    val pelicula = documento.toObject(Peliculas::class.java)
                    pelicula?.let {
                        listaKids.add(it)
                    }
                }

                if (resultado.documents.isNotEmpty()) {
                    lastVisibleKids = resultado.documents.last()
                }

                _listaSeriesKids.postValue(_listaSeriesKids.value.orEmpty() + listaKids)

            } catch (e: Exception) {
                Log.e("Error", "Error al obtener las series de drama desde Firebase", e)
            } finally {
                _isLoadingKids.postValue(false)
            }
        }
    }
}