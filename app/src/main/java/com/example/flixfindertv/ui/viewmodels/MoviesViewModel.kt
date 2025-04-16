package com.example.flixfindertv.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flixfindertv.models.Peliculas
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.example.flixfindertv.room.repository.MovieRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


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

    private var _listaPeliculasRecientes = MutableLiveData<List<Peliculas>>(emptyList())
    val listaPeliculasRecientes: LiveData<List<Peliculas>> = _listaPeliculasRecientes

    private var _listaPeliculasProximas = MutableLiveData<List<Peliculas>>(emptyList())
    val listaPeliculasProximas: LiveData<List<Peliculas>> = _listaPeliculasProximas


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

    private val _isLoadingSimilar = MutableLiveData(false)
    val isLoadingSimilar: LiveData<Boolean> = _isLoadingSimilar

    private val _isLoadingRecientes = MutableLiveData(false)
    val isLoadingRecientes: LiveData<Boolean> = _isLoadingSimilar

    private val _isLoadingProximas = MutableLiveData(false)
    val isLoadingProximas: LiveData<Boolean> = _isLoadingProximas

    var lastVisiblePeliculas: DocumentSnapshot? = null
    var lastVisibleAction: DocumentSnapshot? = null
    var lastVisibleRomance: DocumentSnapshot? = null
    var lastVisibleFamily: DocumentSnapshot? = null
    var lastVisibleComedy: DocumentSnapshot? = null
    var lastVisibleThriller: DocumentSnapshot? = null
    var lastVisibleHorror: DocumentSnapshot? = null
    var lastVisibleScienceFiction: DocumentSnapshot? = null
    var lastVisibleRecientes: DocumentSnapshot? = null
    var lastVisibleProximas: DocumentSnapshot? = null

    private val _voteAverage = MutableStateFlow(0.0)
    val voteAverage = _voteAverage.asStateFlow()

    private val _popularity = MutableStateFlow(0.0)
    val popularity = _popularity.asStateFlow()

    private val _voteCount = MutableStateFlow("")
    val voteCount = _voteCount.asStateFlow()

    suspend fun getTmdbApiKey(): String {
        return try {
            val db = FirebaseFirestore.getInstance()
            val document = db.collection("apiKeys").document("tmdbApiKey").get().await()
            document.getString("key") ?: "" // Si es null, devolvemos una cadena vacía
        } catch (e: Exception) {
            e.printStackTrace()
            "" // En caso de error, devolvemos una cadena vacía en lugar de null
        }
    }

    fun incrementUserCommentCount(userId: String) {
        val userRef = FirebaseFirestore.getInstance().collection("usuarios").document(userId)

        FirebaseFirestore.getInstance().runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val currentCount = snapshot.getLong("numComentarios")?.toInt() ?: 0 // Convertir a Int
            transaction.update(userRef, "numComentarios", currentCount + 1)
        }.addOnFailureListener {
            Log.e("Firestore", "Error incrementando numComentarios en usuarios", it)
        }
    }


    fun observeMovieDetails(movieId: String) {
        if (movieId.isBlank()) return // Evita errores si el ID es vacío o nulo

        // Primero intenta buscar en la colección "peliculas"
        db.collection("peliculas").document(movieId)
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let {
                    if (it.exists()) { // Verifica que el documento exista
                        // Manejo correcto del vote_average (puede ser String en Firestore)
                        val voteAverage = when (val vote = it.get("vote_average")) {
                            is String -> vote.toDoubleOrNull() ?: 0.0
                            is Number -> vote.toDouble()
                            else -> 0.0
                        }
                        _voteAverage.value = voteAverage

                        // Manejo correcto de popularity (es Double en Firestore)
                        val popularity = it.getDouble("popularity") ?: 0.0
                        _popularity.value = popularity

                        // Manejo correcto de vote_count (es String en Firestore)
                        val voteCountStr = it.getString("vote_count") ?: "0"
                        val voteCount = voteCountStr.toLongOrNull() ?: 0L  // Convertimos el String a Long, si no puede se usa 0L

                        _voteCount.value = voteCount.toString() // Convertimos a String para mostrar en la UI
                    } else {
                        // Si no se encuentra en "peliculas", busca en "series"
                        db.collection("series").document(movieId)
                            .addSnapshotListener { seriesSnapshot, _ ->
                                seriesSnapshot?.let {
                                    if (it.exists()) { // Verifica que el documento exista en "series"
                                        // Manejo correcto del vote_average (puede ser String en Firestore)
                                        val voteAverage = when (val vote = it.get("vote_average")) {
                                            is String -> vote.toDoubleOrNull() ?: 0.0
                                            is Number -> vote.toDouble()
                                            else -> 0.0
                                        }
                                        _voteAverage.value = voteAverage

                                        // Manejo correcto de popularity (es Double en Firestore)
                                        val popularity = it.getDouble("popularity") ?: 0.0
                                        _popularity.value = popularity

                                        // Manejo correcto de vote_count (es String en Firestore)
                                        val voteCountStr = it.getString("vote_count") ?: "0"
                                        val voteCount = voteCountStr.toLongOrNull() ?: 0L  // Convertimos el String a Long, si no puede se usa 0L

                                        _voteCount.value = voteCount.toString() // Convertimos a String para mostrar en la UI
                                    } else {
                                        // Si tampoco se encuentra en "series", aseguramos que los valores sean 0
                                        _voteAverage.value = 0.0
                                        _popularity.value = 0.0
                                        _voteCount.value = "0"
                                    }
                                }
                            }
                    }
                }
            }
    }

    private fun getVoteCountFromFirebase(movieId: String, callback: (String?) -> Unit) {
        val db = FirebaseFirestore.getInstance()

        // Primero intenta buscar en la colección "peliculas"
        db.collection("peliculas")
            .document(movieId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // Si encuentra el documento en "peliculas", obtiene el valor de "vote_count"
                    val voteCountString = documentSnapshot.getString("vote_count")
                    callback(voteCountString)  // Pasamos el valor al callback como String
                } else {
                    // Si no lo encuentra en "peliculas", busca en "series"
                    db.collection("series")
                        .document(movieId)
                        .get()
                        .addOnSuccessListener { seriesDocument ->
                            if (seriesDocument.exists()) {
                                // Si encuentra el documento en "series", obtiene el valor de "vote_count"
                                val voteCountString = seriesDocument.getString("vote_count")
                                callback(voteCountString)  // Pasamos el valor al callback como String
                            } else {
                                // Si no se encuentra en ninguna de las dos colecciones, pasa null
                                callback(null)
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.e("Firebase", "Error al obtener el contador de votos en series: ${exception.message}")
                            callback(null)  // Pasamos null en caso de error en "series"
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firebase", "Error al obtener el contador de votos en peliculas: ${exception.message}")
                callback(null)  // Pasamos null en caso de error en "peliculas"
            }
    }

    private fun getVoteAverageFromFirebase(movieId: String, callback: (String?) -> Unit) {
        val db = FirebaseFirestore.getInstance()

        // Primero intenta buscar en la colección "peliculas"
        db.collection("peliculas")
            .document(movieId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // Si encuentra el documento en "peliculas", obtiene el valor de "vote_average"
                    val voteAverageValue = documentSnapshot.get("vote_average")?.toString()
                    callback(voteAverageValue)  // Pasamos el valor al callback como String
                } else {
                    // Si no lo encuentra en "peliculas", busca en "series"
                    db.collection("series")
                        .document(movieId)
                        .get()
                        .addOnSuccessListener { seriesDocument ->
                            if (seriesDocument.exists()) {
                                // Si encuentra el documento en "series", obtiene el valor de "vote_average"
                                val voteAverageValue = seriesDocument.get("vote_average")?.toString()
                                callback(voteAverageValue)  // Pasamos el valor al callback como String
                            } else {
                                // Si no se encuentra en ninguna de las dos colecciones, pasa null
                                callback(null)
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.e("Firebase", "Error al obtener el promedio de votos en series: ${exception.message}")
                            callback(null)  // Pasamos null en caso de error en "series"
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firebase", "Error al obtener el promedio de votos en peliculas: ${exception.message}")
                callback(null)  // Pasamos null en caso de error en "peliculas"
            }
    }


    private fun getPopularityFromFirebase(movieId: String, callback: (Double?) -> Unit) {
        val db = FirebaseFirestore.getInstance()

        // Primero intenta buscar en la colección "peliculas"
        db.collection("peliculas")
            .document(movieId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    println("el documento existe pero es una serie con id $movieId")
                    // Si se encuentra en "peliculas", obtiene la popularidad
                    val popularityValue = documentSnapshot.get("popularity") as? Double
                    callback(popularityValue)  // Llama al callback con el valor de popularidad
                } else {
                    println("si no esta en peliculas, buscamos en series")
                    // Si no se encuentra en "peliculas", busca en "series"
                    db.collection("series")
                        .document(movieId)
                        .get()
                        .addOnSuccessListener { seriesDocument ->
                            if (seriesDocument.exists()) {
                                // Si se encuentra en "series", obtiene la popularidad
                                val popularityValue = seriesDocument.get("popularity") as? Double
                                callback(popularityValue)  // Llama al callback con el valor de popularidad
                            } else {
                                // Si no se encuentra ni en "peliculas" ni en "series", pasa null
                                callback(null)
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.e("Firebase", "Error al obtener la popularidad de series: ${exception.message}")
                            callback(null)  // Pasamos null en caso de error en series
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firebase", "Error al obtener la popularidad de peliculas: ${exception.message}")
                callback(null)  // Pasamos null en caso de error en peliculas
            }
    }


    fun calculateNewVoteAverage(peliculaId: String, newRating: Int, callback: (String?) -> Unit) {
        // Primero obtenemos el contador de votos
        getVoteCountFromFirebase(peliculaId) { currentVoteCountString ->
            if (currentVoteCountString != null) {
                val currentVoteCount = currentVoteCountString.toIntOrNull() ?: 0

                // Luego obtenemos el promedio de votos actual
                getVoteAverageFromFirebase(peliculaId) { currentAverageString ->
                    if (currentAverageString != null) {
                        val currentAverage = currentAverageString.toFloatOrNull()

                        if (currentAverage != null) {
                            // Calculamos el nuevo promedio usando la fórmula adecuada
                            val newAverage = ((currentAverage * currentVoteCount) + newRating) / (currentVoteCount + 1)
                            callback(newAverage.toString())
                        } else {
                            callback(null)
                        }
                    } else {
                        callback(null)
                    }
                }
            } else {
                callback(null)
            }
        }
    }

    fun calculateNewPopularity(movieId: String, callback: (Double?) -> Unit) {
        getPopularityFromFirebase(movieId) { currentPopularity ->
            if (currentPopularity != null) {
                val newPopularity = currentPopularity + 1
                callback(newPopularity)
            } else {
                callback(null)
            }
        }
    }

    fun updateVoteAverageInFirebase(movieId: String, newVoteAverage: Float) {
        val db = FirebaseFirestore.getInstance()

        // Primero obtenemos el contador de votos
        getVoteCountFromFirebase(movieId) { currentVoteCountString ->
            if (currentVoteCountString != null) {
                val currentVoteCount = currentVoteCountString.toIntOrNull() ?: 0

                // Intentamos obtener el documento desde "peliculas"
                db.collection("peliculas")
                    .document(movieId)
                    .get()
                    .addOnSuccessListener { documentSnapshot ->
                        if (documentSnapshot.exists()) {
                            // Si encontramos el documento en "peliculas", obtenemos el promedio de votos
                            val currentVoteAverage = documentSnapshot.getString("vote_average")?.toFloatOrNull()

                            // Si el promedio de votos ha cambiado, lo actualizamos
                            if (currentVoteAverage != newVoteAverage) {
                                val newVoteAverageString = newVoteAverage.toString()

                                // Incrementamos el contador de votos
                                val newVoteCount = currentVoteCount + 1
                                val newVoteCountString = newVoteCount.toString() // Convertimos el nuevo conteo a String

                                // Realizamos la actualización en Firebase
                                db.collection("peliculas")
                                    .document(movieId)
                                    .update(
                                        "vote_average", newVoteAverageString,
                                        "vote_count", newVoteCountString
                                    )
                                    .addOnSuccessListener {
                                        Log.d("Firebase", "El voto promedio y el contador de votos se actualizaron correctamente en peliculas.")
                                    }
                                    .addOnFailureListener { exception ->
                                        Log.e("Firebase", "Error al actualizar el voto promedio y el contador de votos en peliculas: ${exception.message}")
                                    }
                            }
                        } else {
                            // Si no se encuentra en "peliculas", busca en "series"
                            db.collection("series")
                                .document(movieId)
                                .get()
                                .addOnSuccessListener { seriesDocumentSnapshot ->
                                    if (seriesDocumentSnapshot.exists()) {
                                        // Si encontramos el documento en "series", obtenemos el promedio de votos
                                        val currentVoteAverage = seriesDocumentSnapshot.getString("vote_average")?.toFloatOrNull()

                                        // Si el promedio de votos ha cambiado, lo actualizamos
                                        if (currentVoteAverage != newVoteAverage) {
                                            val newVoteAverageString = newVoteAverage.toString()

                                            // Incrementamos el contador de votos
                                            val newVoteCount = currentVoteCount + 1
                                            val newVoteCountString = newVoteCount.toString() // Convertimos el nuevo conteo a String

                                            // Realizamos la actualización en Firebase
                                            db.collection("series")
                                                .document(movieId)
                                                .update(
                                                    "vote_average", newVoteAverageString,
                                                    "vote_count", newVoteCountString
                                                )
                                                .addOnSuccessListener {
                                                    Log.d("Firebase", "El voto promedio y el contador de votos se actualizaron correctamente en series.")
                                                }
                                                .addOnFailureListener { exception ->
                                                    Log.e("Firebase", "Error al actualizar el voto promedio y el contador de votos en series: ${exception.message}")
                                                }
                                        }
                                    } else {
                                        Log.e("Firebase", "No se encontró ni en peliculas ni en series.")
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    Log.e("Firebase", "Error al obtener el voto promedio actual de series: ${exception.message}")
                                }
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e("Firebase", "Error al obtener el voto promedio actual de peliculas: ${exception.message}")
                    }
            } else {
                Log.e("Firebase", "Error al obtener el contador de votos.")
            }
        }
    }

    fun updatePopularityInFirebase(movieId: String, newPopularity: Double) {
        val db = FirebaseFirestore.getInstance()

        // Primero intentamos actualizar en la colección "peliculas"
        db.collection("peliculas")
            .document(movieId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // Si se encuentra el documento en "peliculas", lo actualizamos
                    db.collection("peliculas")
                        .document(movieId)
                        .update("popularity", newPopularity)
                        .addOnSuccessListener {
                            Log.d("Firebase", "La popularidad se actualizó correctamente en peliculas.")
                        }
                        .addOnFailureListener { exception ->
                            Log.e("Firebase", "Error al actualizar la popularidad en peliculas: ${exception.message}")
                        }
                } else {
                    // Si no se encuentra en "peliculas", intentamos en "series"
                    db.collection("series")
                        .document(movieId)
                        .get()
                        .addOnSuccessListener { seriesDocumentSnapshot ->
                            if (seriesDocumentSnapshot.exists()) {
                                // Si se encuentra el documento en "series", lo actualizamos
                                db.collection("series")
                                    .document(movieId)
                                    .update("popularity", newPopularity)
                                    .addOnSuccessListener {
                                        Log.d("Firebase", "La popularidad se actualizó correctamente en series.")
                                    }
                                    .addOnFailureListener { exception ->
                                        Log.e("Firebase", "Error al actualizar la popularidad en series: ${exception.message}")
                                    }
                            } else {
                                Log.e("Firebase", "No se encontró ni en peliculas ni en series.")
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.e("Firebase", "Error al obtener el documento de series: ${exception.message}")
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firebase", "Error al obtener el documento de peliculas: ${exception.message}")
            }
    }

    fun obtenerPeliculasMasRecientes() {
        if (_isLoadingRecientes.value == true) return

        _isLoadingRecientes.value = true
        val listaRecientes = mutableListOf<Peliculas>()

        viewModelScope.launch {
            try {
                // Obtener la fecha actual en formato String (por ejemplo: "2024-04-10")
                val currentDateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
                    Date()
                )

                // Consultar las películas más recientes hasta la fecha actual, ordenadas por fecha de lanzamiento
                var consulta = db.collection("peliculas")
                    .whereLessThanOrEqualTo("release_date", currentDateString) // Filtrar por películas cuyo release_date sea <= a la fecha actual
                    .orderBy("release_date", Query.Direction.DESCENDING) // Ordenar por fecha de lanzamiento
                    .limit(20)

                lastVisibleRecientes?.let {
                    consulta = consulta.startAfter(it)
                }

                val resultado = consulta.get().await()

                // Verificar la cantidad de resultados obtenidos
                Log.d("Debug", "Total movies retrieved: ${resultado.size()}")

                for (documento in resultado.documents) {
                    val pelicula = documento.toObject(Peliculas::class.java)
                    pelicula?.let {
                        listaRecientes.add(it)
                        println("Película obtenida: Título = ${it.titulo}, Id = ${it.id}, Portada = ${it.poster_path}")
                    }
                }

                if (resultado.documents.isNotEmpty()) {
                    lastVisibleRecientes = resultado.documents.last()
                }

                if (_listaPeliculasRecientes.value.isNullOrEmpty()) {
                    _listaPeliculasRecientes.postValue(listaRecientes)
                } else {
                    _listaPeliculasRecientes.value = _listaPeliculasRecientes.value.orEmpty() + listaRecientes
                }

            } catch (e: Exception) {
                Log.e("Error", "Error al obtener las películas más recientes hasta la fecha actual desde Firebase", e)
            } finally {
                _isLoadingRecientes.postValue(false)
            }
        }
    }

    fun obtenerContenidoProximo() {
        if (_isLoadingProximas.value == true) return

        _isLoadingProximas.value = true
        val listaProximos = mutableListOf<Peliculas>()

        viewModelScope.launch {
            try {
                val currentDateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                // Consultas a ambas colecciones (películas y series)
                val peliculasTask = db.collection("peliculas")
                    .whereGreaterThan("release_date", currentDateString)
                    .orderBy("release_date", Query.Direction.ASCENDING)
                    .limit(10)
                    .get()

                val seriesTask = db.collection("series")
                    .whereGreaterThan("release_date_series", currentDateString)
                    .orderBy("release_date_series", Query.Direction.ASCENDING)
                    .limit(10)
                    .get()

                // Esperar ambos resultados
                val peliculasResult = peliculasTask.await()
                val seriesResult = seriesTask.await()

                Log.d("Debug", "Total series obtenidas: ${seriesResult.size()}")

                // Convertir documentos a objetos de tipo Peliculas (ya que las series también tienen el mismo modelo)
                val peliculas = peliculasResult.documents.mapNotNull { doc ->
                    doc.toObject(Peliculas::class.java)
                }

                val series = seriesResult.documents.mapNotNull { doc ->
                    doc.toObject(Peliculas::class.java)
                }

                series.forEach {
                    Log.d("Debug", "Serie obtenida: Título = ${it.titulo} | Fecha de lanzamiento = ${it.release_date_series}")
                }

                // Mezclar ambos y ordenarlos por fecha de lanzamiento
                listaProximos.addAll(peliculas)
                listaProximos.addAll(series)

                // Ordenamos todos los contenidos por fecha de lanzamiento, considerando release_date para películas y release_date_series para series
                listaProximos.sortBy {
                    // Usamos release_date si es una película, sino usamos release_date_series para series
                    it.release_date ?: it.release_date_series
                }

                // Log para verificar
                Log.d("Debug", "Total contenido próximo: ${listaProximos.size}")
                listaProximos.forEach {
                    Log.d("Debug", "→ ${it.titulo} | ${it.release_date ?: it.release_date_series}")
                }

                _listaPeliculasProximas.postValue(listaProximos)

            } catch (e: Exception) {
                Log.e("Error", "Error al obtener próximos estrenos de películas y series", e)
            } finally {
                _isLoadingProximas.postValue(false)
            }
        }
    }





    fun obtenerPeliculasPopulares() {
        if (isLoadingPeliculas.value == true) return

        _isLoadingPeliculas.value = true
        val peliculasList = mutableListOf<Peliculas>()

        viewModelScope.launch {
            try {
                var query = db.collection("peliculas")
                    .orderBy("popularity", Query.Direction.DESCENDING) // Ordenar por popularidad (de mayor a menor)
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

}
