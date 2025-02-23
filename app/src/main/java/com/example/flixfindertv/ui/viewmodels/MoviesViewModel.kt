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

    private var _listaSeries = MutableLiveData<List<Peliculas>>(emptyList())
    val listaSeries: LiveData<List<Peliculas>> = _listaSeries

    private var _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

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
                    }
                }
                page++
            }

            _listaSeries.postValue(allSeries)  // Actualizamos la lista con las series
            _isLoading.postValue(false)  // Indicamos que hemos terminado de cargar
        }
    }
}
