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
