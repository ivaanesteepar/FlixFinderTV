package com.example.flixfindertv.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flixfindertv.models.Generos
import com.example.flixfindertv.network.RetrofitClient
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// ViewModel que maneja los géneros
class GenresViewModel : ViewModel() {

    private val _listaGeneros = MutableLiveData<List<Generos>>(emptyList())  // Lista de géneros
    val listaGeneros: LiveData<List<Generos>> = _listaGeneros  // Exponerlo como LiveData

    private val _isLoading = MutableLiveData<Boolean>(false)  // Estado de carga
    val isLoading: LiveData<Boolean> = _isLoading  // Exponerlo como LiveData

    fun obtenerGenerosPeliculas(apiKey: String, language: String) {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                // Llamada a la API para obtener los géneros
                val response = RetrofitClient.webService.getMoviesGenres(apiKey, language)
                println("respuesta es: $response")

                if (response.isSuccessful) {
                    response.body()?.let { genreResponse ->
                        _listaGeneros.postValue(genreResponse.genres) // Actualiza el LiveData directamente
                        Log.d("MoviesViewModel", "Géneros obtenidos: ${genreResponse.genres}")
                    }
                } else {
                    Log.e("MoviesViewModel", "Error al obtener géneros: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("MoviesViewModel", "Error al obtener géneros: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }


    fun obtenerGenerosSeries(apiKey: String, language: String) {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                // Llamada a la API para obtener los géneros
                val response = RetrofitClient.webService.getTVShowsGenres(apiKey, language)
                println("respuesta es: $response")

                if (response.isSuccessful) {
                    response.body()?.let { genreResponse ->
                        _listaGeneros.postValue(genreResponse.genres) // Actualiza el LiveData directamente
                        Log.d("MoviesViewModel", "Géneros obtenidos: ${genreResponse.genres}")
                    }
                } else {
                    Log.e("MoviesViewModel", "Error al obtener géneros: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("MoviesViewModel", "Error al obtener géneros: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }


    fun almacenarGenerosEnFirestore(generos: List<Generos>) {
        val db = FirebaseFirestore.getInstance()

        // Recorremos la lista de géneros y los almacenamos en Firestore
        generos.forEach { genero ->
            val generoRef = db.collection("generos").document(genero.id.toString())

            val generoData = hashMapOf(
                "id" to genero.id,
                "name" to genero.name
            )

            // Guardamos el género en Firestore con el id como nombre del documento
            generoRef.set(generoData)
                .addOnSuccessListener {
                    Log.d("Firestore", "Género ${genero.name} almacenado correctamente.")
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error al almacenar género: ${e.message}")
                }
        }
    }

    // Función que obtiene el id del género basado en su nombre
    fun obtenerIdGeneroPorNombre(genreName: String, callback: (Int?) -> Unit) {
        val db = FirebaseFirestore.getInstance()

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
}
