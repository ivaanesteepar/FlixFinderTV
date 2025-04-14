package com.example.flixfindertv.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.flixfindertv.room.database.AppDatabase
import com.example.flixfindertv.room.entities.Genero1MovieEntity
import com.example.flixfindertv.room.entities.Genero2MovieEntity
import com.example.flixfindertv.room.entities.ProximasMovieEntity
import com.example.flixfindertv.room.repository.MovieRepository
import kotlinx.coroutines.launch

class OfflineViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OfflineViewModel::class.java)) {
            return OfflineViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


class OfflineViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MovieRepository

    private var _listaPeliculasGenero1 = MutableLiveData<List<Genero1MovieEntity>>(emptyList())
    val listaPeliculasGenero1: LiveData<List<Genero1MovieEntity>> = _listaPeliculasGenero1

    private var _listaPeliculasGenero2 = MutableLiveData<List<Genero2MovieEntity>>(emptyList())
    val listaPeliculasGenero2: LiveData<List<Genero2MovieEntity>> = _listaPeliculasGenero2

    private var _listaPeliculasProximas = MutableLiveData<List<ProximasMovieEntity>>(emptyList())
    val listaPeliculasProximas: LiveData<List<ProximasMovieEntity>> = _listaPeliculasProximas

    init {
        // Acceder al contexto de la aplicación
        val context = application.applicationContext
        // Inicializar el DAO usando el contexto
        val dao = AppDatabase.getDatabase(context).movieDao()
        // Crear el repositorio con el DAO
        repository = MovieRepository(dao)
    }

    fun limpiarPeliculasGenero1() {
        viewModelScope.launch {
            repository.deleteAllMoviesGenero1()
        }
    }

    fun limpiarPeliculasGenero2() {
        viewModelScope.launch {
            repository.deleteAllMoviesGenero2()
        }
    }

    fun limpiarPeliculasProximas() {
        viewModelScope.launch {
            repository.deleteAllMoviesProximas()
        }
    }

    // Función para cargar las películas del género 1
    fun loadGenero1Movies() {
        viewModelScope.launch {
            val movies = repository.getAllMoviesGenero1()
            _listaPeliculasGenero1.postValue(movies)
        }
    }

    // Función para cargar las películas del género 2
    fun loadGenero2Movies() {
        viewModelScope.launch {
            val movies = repository.getAllMoviesGenero2()
            _listaPeliculasGenero2.postValue(movies)
        }
    }

    // Función para cargar las películas próximas
    fun loadProximasMovies() {
        viewModelScope.launch {
            val movies = repository.getAllMoviesProximas()
            _listaPeliculasProximas.postValue(movies)
        }
    }

    // Función para insertar las películas del género 1
    fun insertMoviesGenero1(movies: List<Genero1MovieEntity>) {
        viewModelScope.launch {
            repository.insertMoviesGenero1(movies)
        }
    }

    // Función para insertar las películas del género 2
    fun insertMoviesGenero2(movies: List<Genero2MovieEntity>) {
        viewModelScope.launch {
            repository.insertMoviesGenero2(movies)
        }
    }

    // Función para insertar las películas próximas
    fun insertMoviesProximas(movies: List<ProximasMovieEntity>) {
        viewModelScope.launch {
            repository.insertMoviesProximas(movies)
        }
    }
}
