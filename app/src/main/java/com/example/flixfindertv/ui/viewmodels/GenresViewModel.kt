package com.example.flixfindertv.ui.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flixfindertv.models.Generos
import com.example.flixfindertv.models.Peliculas
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class GenresViewModel : ViewModel() {
    val db = FirebaseFirestore.getInstance()

    val nombreGenero1 = mutableStateOf("")
    val nombreGenero2 = mutableStateOf("")

    private val _peliculasGenero1 = MutableLiveData<List<Peliculas>>()
    val peliculasGenero1: LiveData<List<Peliculas>> get() = _peliculasGenero1

    private val _peliculasGenero2 = MutableLiveData<List<Peliculas>>()
    val peliculasGenero2: LiveData<List<Peliculas>> get() = _peliculasGenero2

    val peliculasPorGenero = MutableLiveData<List<Peliculas>>()

    private val _isLoadingGenero1 = MutableLiveData(false)
    val isLoadingGenero1: LiveData<Boolean> get() = _isLoadingGenero1

    private val _isLoadingGenero2 = MutableLiveData(false)
    val isLoadingGenero2: LiveData<Boolean> get() = _isLoadingGenero2

    var lastVisibleGenero1: DocumentSnapshot? = null
    var lastVisibleGenero2: DocumentSnapshot? = null


    fun limpiarPeliculasGenero1() {
        _peliculasGenero1.postValue(emptyList()) // Vacía la lista antes de actualizarla
    }

    fun limpiarPeliculasGenero2() {
        _peliculasGenero2.postValue(emptyList()) // Vacía la lista del segundo género
    }


    private suspend fun getMoviesAndSeriesByGenreId(genreId: Int) {
        val peliculasList = mutableListOf<Peliculas>()

        try {
            // Obtener películas
            val peliculasSnapshot = db.collection("peliculas")
                .whereArrayContains("genre_ids", genreId)
                .limit(50)
                .get()
                .await()

            peliculasSnapshot.documents.mapNotNullTo(peliculasList) { it.toObject(Peliculas::class.java) }

            // Obtener series
            val seriesSnapshot = db.collection("series")
                .whereArrayContains("genre_ids", genreId)
                .limit(50)
                .get()
                .await()

            seriesSnapshot.documents.mapNotNullTo(peliculasList) { it.toObject(Peliculas::class.java) }

            // Guardar los resultados en la variable pública
            peliculasPorGenero.value = peliculasList

        } catch (e: Exception) {
            e.printStackTrace()
            // Si hay un error, podrías manejarlo, por ejemplo, con un mensaje de error
            peliculasPorGenero.value = emptyList()  // O devolver una lista vacía en caso de error
        }
    }

    // Llamar a esta función para obtener las películas y series por un genreId
    fun obtenerPeliculasSeriesPorGenero(genreId: Int) {
        viewModelScope.launch {
            getMoviesAndSeriesByGenreId(genreId)
        }
    }

    // Función que obtiene el id del género basado en su nombre
    fun obtenerIdGeneroPorNombre(genreName: String, callback: (Int?) -> Unit) {
        db.collection("generos")
            .whereEqualTo("name", genreName)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    callback(null)  // Si no hay resultados, devolvemos null
                } else {
                    val genre = querySnapshot.documents[0].toObject(Generos::class.java)
                    callback(genre?.id)  // Llamamos al callback con el id del género encontrado
                }
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
                callback(null)  // En caso de error, devolvemos null
            }
    }

    // Obtiene los géneros favoritos del usuario
    fun obtenerGenerosFavoritos(userId: String) {
        obtenerGenerosFavoritos(userId) { generos ->
            if (generos.size >= 2) {
                nombreGenero1.value = generos[0]
                nombreGenero2.value = generos[1]
            }
        }
    }

    // Obtiene los géneros favoritos del usuario
    fun obtenerGenerosFavoritos(userId: String, callback: (List<String>) -> Unit) {
        viewModelScope.launch {
            try {
                val doc = db.collection("usuarios").document(userId).get().await()
                val generosFavoritos = doc.get("generosFavoritos") as? Map<String, Long> ?: emptyMap()
                val generosOrdenados = generosFavoritos.keys.toList()

                if (generosOrdenados.size >= 2) {
                    callback(listOf(generosOrdenados[0], generosOrdenados[1]))
                } else {
                    callback(emptyList())  // Si no hay suficientes géneros, devolvemos lista vacía
                }
            } catch (e: Exception) {
                e.printStackTrace()
                callback(emptyList())
            }
        }
    }

    fun obtenerPeliculasYSeriesGenero1(userId: String) {
        if (_isLoadingGenero1.value == true) return

        _isLoadingGenero1.value = true
        obtenerGenerosFavoritos(userId) { generos ->
            if (generos.isNotEmpty()) {
                obtenerIdGeneroPorNombre(generos[0]) { idGenero ->
                    if (idGenero != null) {
                        viewModelScope.launch {
                            try {
                                val peliculasList = mutableListOf<Peliculas>()

                                // Obtener películas
                                var query = db.collection("peliculas")
                                    .whereArrayContains("genre_ids", idGenero)
                                    .limit(20)

                                lastVisibleGenero1?.let {
                                    query = query.startAfter(it)
                                }

                                val peliculasSnapshot = query.get().await()
                                peliculasSnapshot.documents.mapNotNullTo(peliculasList) { it.toObject(Peliculas::class.java) }

                                // Obtener series
                                var querySeries = db.collection("series")
                                    .whereArrayContains("genre_ids", idGenero)
                                    .limit(20)

                                lastVisibleGenero1?.let {
                                    querySeries = querySeries.startAfter(it)
                                }

                                val seriesSnapshot = querySeries.get().await()
                                seriesSnapshot.documents.mapNotNullTo(peliculasList) { it.toObject(Peliculas::class.java) }

                                if (peliculasSnapshot.documents.isNotEmpty()) {
                                    lastVisibleGenero1 = peliculasSnapshot.documents.last()
                                }
                                if (seriesSnapshot.documents.isNotEmpty()) {
                                    lastVisibleGenero1 = seriesSnapshot.documents.last()
                                }

                                // Mezclar películas y series
                                val mezclada = mutableListOf<Peliculas>()
                                val peliculasIterator = peliculasList.filter { !it.esSerie }.iterator()
                                val seriesIterator = peliculasList.filter { it.esSerie }.iterator()

                                // Alternar entre películas y series
                                while (peliculasIterator.hasNext() || seriesIterator.hasNext()) {
                                    if (peliculasIterator.hasNext()) {
                                        mezclada.add(peliculasIterator.next())
                                    }
                                    if (seriesIterator.hasNext()) {
                                        mezclada.add(seriesIterator.next())
                                    }
                                }

                                _peliculasGenero1.value = _peliculasGenero1.value.orEmpty() + mezclada
                            } catch (e: Exception) {
                                e.printStackTrace()
                            } finally {
                                _isLoadingGenero1.value = false
                            }
                        }
                    }
                }
            }
        }
    }


    fun obtenerPeliculasYSeriesGenero2(userId: String) {
        if (_isLoadingGenero2.value == true) return

        _isLoadingGenero2.value = true
        obtenerGenerosFavoritos(userId) { generos ->
            if (generos.size >= 2) {
                obtenerIdGeneroPorNombre(generos[1]) { idGenero ->
                    if (idGenero != null) {
                        viewModelScope.launch {
                            try {
                                val peliculasList = mutableListOf<Peliculas>()

                                // Obtener películas
                                var query = db.collection("peliculas")
                                    .whereArrayContains("genre_ids", idGenero)
                                    .limit(20)

                                lastVisibleGenero2?.let {
                                    query = query.startAfter(it)
                                }

                                val peliculasSnapshot = query.get().await()
                                peliculasSnapshot.documents.mapNotNullTo(peliculasList) { it.toObject(Peliculas::class.java) }

                                // Obtener series
                                var querySeries = db.collection("series")
                                    .whereArrayContains("genre_ids", idGenero)
                                    .limit(20)

                                lastVisibleGenero2?.let {
                                    querySeries = querySeries.startAfter(it)
                                }

                                val seriesSnapshot = querySeries.get().await()
                                seriesSnapshot.documents.mapNotNullTo(peliculasList) { it.toObject(Peliculas::class.java) }

                                if (peliculasSnapshot.documents.isNotEmpty()) {
                                    lastVisibleGenero2 = peliculasSnapshot.documents.last()
                                }
                                if (seriesSnapshot.documents.isNotEmpty()) {
                                    lastVisibleGenero2 = seriesSnapshot.documents.last()
                                }

                                // Mezclar películas y series
                                val mezclada = mutableListOf<Peliculas>()
                                val peliculasIterator = peliculasList.filter { !it.esSerie }.iterator()
                                val seriesIterator = peliculasList.filter { it.esSerie }.iterator()

                                // Alternar entre películas y series
                                while (peliculasIterator.hasNext() || seriesIterator.hasNext()) {
                                    if (peliculasIterator.hasNext()) {
                                        mezclada.add(peliculasIterator.next())
                                    }
                                    if (seriesIterator.hasNext()) {
                                        mezclada.add(seriesIterator.next())
                                    }
                                }

                                _peliculasGenero2.value = _peliculasGenero2.value.orEmpty() + mezclada
                            } catch (e: Exception) {
                                e.printStackTrace()
                            } finally {
                                _isLoadingGenero2.value = false
                            }
                        }
                    }
                }
            }
        }
    }
}
