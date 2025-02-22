package com.example.flixfindertv.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flixfindertv.models.Peliculas
import com.example.flixfindertv.network.RetrofitClient
import kotlinx.coroutines.launch

class MoviesViewModel : ViewModel() {

    private var _listaPeliculas = MutableLiveData<List<Peliculas>>(emptyList())
    val listaPeliculas: LiveData<List<Peliculas>> = _listaPeliculas

    private var _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    // Función para obtener todas las películas populares
    fun obtenerTodasLasPeliculas(apiKey: String, language: String) {
        _isLoading.value = true
        var page = 1
        val totalPagesToLoad = 50  // Limitar a 50 páginas (1000 peliculas)
        val allMovies = mutableListOf<Peliculas>()

        // Iniciar una corutina
        viewModelScope.launch {
            while (page <= totalPagesToLoad) {
                // Llamamos a la función suspendida dentro de la corutina
                val response = RetrofitClient.webService.getPopularMovies(apiKey, language, page)

                if (response.isSuccessful) {
                    val movieResponse = response.body()
                    movieResponse?.let {
                        allMovies.addAll(it.resultados)  // Añadir las películas de la página actual
                    }
                }
                page++  // Incrementamos el número de página
            }

            // Actualizamos los datos después de cargar todas las películas
            _listaPeliculas.postValue(allMovies)  // Actualizamos la lista con las películas
            _isLoading.postValue(false)  // Indicamos que hemos terminado de cargar
        }
    }
}

