package com.example.flixfindertv.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.flixfindertv.room.database.AppDatabase
import com.example.flixfindertv.room.entities.*
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

    private val _peliculasPopulares = MutableLiveData<List<PeliculasPopularesEntity>>(emptyList())
    val peliculasPopulares: LiveData<List<PeliculasPopularesEntity>> = _peliculasPopulares

    private val _peliculasUltimosLanzamientos = MutableLiveData<List<UltimosLanzamientosMovieEntity>>(emptyList())
    val peliculasUltimosLanzamientos: LiveData<List<UltimosLanzamientosMovieEntity>> = _peliculasUltimosLanzamientos

    private val _peliculasAccion = MutableLiveData<List<AccionMovieEntity>>(emptyList())
    val peliculasAccion: LiveData<List<AccionMovieEntity>> = _peliculasAccion

    private val _peliculasRomance = MutableLiveData<List<RomanceMovieEntity>>(emptyList())
    val peliculasRomance: LiveData<List<RomanceMovieEntity>> = _peliculasRomance

    private val _peliculasFamilia = MutableLiveData<List<FamiliaMovieEntity>>(emptyList())
    val peliculasFamilia: LiveData<List<FamiliaMovieEntity>> = _peliculasFamilia

    private val _peliculasComedia = MutableLiveData<List<ComediaMovieEntity>>(emptyList())
    val peliculasComedia: LiveData<List<ComediaMovieEntity>> = _peliculasComedia

    private val _peliculasThriller = MutableLiveData<List<ThrillerMovieEntity>>(emptyList())
    val peliculasThriller: LiveData<List<ThrillerMovieEntity>> = _peliculasThriller

    private val _peliculasHorror = MutableLiveData<List<HorrorMovieEntity>>(emptyList())
    val peliculasHorror: LiveData<List<HorrorMovieEntity>> = _peliculasHorror

    private val _peliculasCienciaFiccion = MutableLiveData<List<CienciaFiccionMovieEntity>>(emptyList())
    val peliculasCienciaFiccion: LiveData<List<CienciaFiccionMovieEntity>> = _peliculasCienciaFiccion

    private val _seriesPopulares = MutableLiveData<List<SeriesPopularesEntity>>(emptyList())
    val seriesPopulares: LiveData<List<SeriesPopularesEntity>> = _seriesPopulares

    private val _seriesUltimosLanzamientos = MutableLiveData<List<UltimosLanzamientosSeriesEntity>>(emptyList())
    val seriesUltimosLanzamientos: LiveData<List<UltimosLanzamientosSeriesEntity>> = _seriesUltimosLanzamientos

    private val _seriesAccionAventura = MutableLiveData<List<AccionAventuraSerieEntity>>(emptyList())
    val seriesAccionAventura: LiveData<List<AccionAventuraSerieEntity>> = _seriesAccionAventura

    private val _seriesAnimacion = MutableLiveData<List<AnimacionSerieEntity>>(emptyList())
    val seriesAnimacion: LiveData<List<AnimacionSerieEntity>> = _seriesAnimacion

    private val _seriesComedia = MutableLiveData<List<ComediaSerieEntity>>(emptyList())
    val seriesComedia: LiveData<List<ComediaSerieEntity>> = _seriesComedia

    private val _seriesCrimen = MutableLiveData<List<CrimenSerieEntity>>(emptyList())
    val seriesCrimen: LiveData<List<CrimenSerieEntity>> = _seriesCrimen

    private val _seriesDrama = MutableLiveData<List<DramaSerieEntity>>(emptyList())
    val seriesDrama: LiveData<List<DramaSerieEntity>> = _seriesDrama

    private val _seriesFamilia = MutableLiveData<List<FamiliaSerieEntity>>(emptyList())
    val seriesFamilia: LiveData<List<FamiliaSerieEntity>> = _seriesFamilia

    private val _seriesKids = MutableLiveData<List<KidsSerieEntity>>(emptyList())
    val seriesKids: LiveData<List<KidsSerieEntity>> = _seriesKids

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

    fun limpiarPeliculasPopulares() {
        viewModelScope.launch {
            repository.deleteAllMoviesPopulares()
        }
    }

    fun limpiarPeliculasUltimosLanzamientos() {
        viewModelScope.launch {
            repository.deleteAllMoviesUltimosLanzamientos()
        }
    }

    fun limpiarPeliculasAccion() {
        viewModelScope.launch {
            repository.deleteAllMoviesAccion()
        }
    }

    fun limpiarPeliculasRomance() {
        viewModelScope.launch {
            repository.deleteAllMoviesRomance()
        }
    }

    fun limpiarPeliculasFamilia() {
        viewModelScope.launch {
            repository.deleteAllMoviesFamilia()
        }
    }

    fun limpiarPeliculasComedia() {
        viewModelScope.launch {
            repository.deleteAllMoviesComedia()
        }
    }

    fun limpiarPeliculasThriller() {
        viewModelScope.launch {
            repository.deleteAllMoviesThriller()
        }
    }

    fun limpiarPeliculasHorror() {
        viewModelScope.launch {
            repository.deleteAllMoviesHorror()
        }
    }

    fun limpiarPeliculasCienciaFiccion() {
        viewModelScope.launch {
            repository.deleteAllMoviesCienciaFiccion()
        }
    }

    fun limpiarSeriesPopulares() {
        viewModelScope.launch {
            repository.deleteAllSeriesPopulares()
        }
    }

    fun limpiarSeriesUltimosLanzamientos() {
        viewModelScope.launch {
            repository.deleteAllSeriesUltimosLanzamientos()
        }
    }

    fun limpiarSeriesAccionAventura() {
        viewModelScope.launch {
            repository.deleteAllSeriesAccionAventura()
        }
    }

    fun limpiarSeriesAnimacion() {
        viewModelScope.launch {
            repository.deleteAllSeriesAnimacion()
        }
    }

    fun limpiarSeriesComedia() {
        viewModelScope.launch {
            repository.deleteAllSeriesComedia()
        }
    }

    fun limpiarSeriesCrimen() {
        viewModelScope.launch {
            repository.deleteAllSeriesCrimen()
        }
    }

    fun limpiarSeriesDrama() {
        viewModelScope.launch {
            repository.deleteAllSeriesDrama()
        }
    }

    fun limpiarSeriesFamilia() {
        viewModelScope.launch {
            repository.deleteAllSeriesFamilia()
        }
    }

    fun limpiarSeriesKids() {
        viewModelScope.launch {
            repository.deleteAllSeriesKids()
        }
    }

    fun loadGenero1Movies() {
        viewModelScope.launch {
            val movies = repository.getAllMoviesGenero1()
            _listaPeliculasGenero1.postValue(movies)
        }
    }

    fun loadGenero2Movies() {
        viewModelScope.launch {
            val movies = repository.getAllMoviesGenero2()
            _listaPeliculasGenero2.postValue(movies)
        }
    }

    fun loadProximasMovies() {
        viewModelScope.launch {
            val movies = repository.getAllMoviesProximas()
            _listaPeliculasProximas.postValue(movies)
        }
    }

    fun loadPeliculasPopulares() {
        viewModelScope.launch {
            val movies = repository.getAllMoviesPopulares()
            println("las movies en offline son: $movies")
            _peliculasPopulares.postValue(movies)
        }
    }

    fun loadPeliculasUltimosLanzamientos() {
        viewModelScope.launch {
            val movies = repository.getAllMoviesUltimosLanzamientos()
            _peliculasUltimosLanzamientos.postValue(movies)
        }
    }

    fun loadPeliculasAccion() {
        viewModelScope.launch {
            val movies = repository.getAllMoviesAccion()
            _peliculasAccion.postValue(movies)
        }
    }

    fun loadPeliculasRomance() {
        viewModelScope.launch {
            val movies = repository.getAllMoviesRomance()
            _peliculasRomance.postValue(movies)
        }
    }

    fun loadPeliculasFamilia() {
        viewModelScope.launch {
            val movies = repository.getAllMoviesFamilia()
            _peliculasFamilia.postValue(movies)
        }
    }

    fun loadPeliculasComedia() {
        viewModelScope.launch {
            val movies = repository.getAllMoviesComedia()
            _peliculasComedia.postValue(movies)
        }
    }

    fun loadPeliculasThriller() {
        viewModelScope.launch {
            val movies = repository.getAllMoviesThriller()
            _peliculasThriller.postValue(movies)
        }
    }

    fun loadPeliculasHorror() {
        viewModelScope.launch {
            val movies = repository.getAllMoviesHorror()
            _peliculasHorror.postValue(movies)
        }
    }

    fun loadPeliculasCienciaFiccion() {
        viewModelScope.launch {
            val movies = repository.getAllMoviesCienciaFiccion()
            _peliculasCienciaFiccion.postValue(movies)
        }
    }

    fun loadSeriesPopulares() {
        viewModelScope.launch {
            val series = repository.getAllSeriesPopulares()
            _seriesPopulares.postValue(series)
        }
    }

    fun loadSeriesUltimosLanzamientos() {
        viewModelScope.launch {
            val series = repository.getAllSeriesUltimosLanzamientos()
            _seriesUltimosLanzamientos.postValue(series)
        }
    }

    fun loadSeriesAccionAventura() {
        viewModelScope.launch {
            val series = repository.getAllSeriesAccionAventura()
            _seriesAccionAventura.postValue(series)
        }
    }

    fun loadSeriesAnimacion() {
        viewModelScope.launch {
            val series = repository.getAllSeriesAnimacion()
            _seriesAnimacion.postValue(series)
        }
    }

    fun loadSeriesComedia() {
        viewModelScope.launch {
            val series = repository.getAllSeriesComedia()
            _seriesComedia.postValue(series)
        }
    }

    fun loadSeriesCrimen() {
        viewModelScope.launch {
            val series = repository.getAllSeriesCrimen()
            _seriesCrimen.postValue(series)
        }
    }

    fun loadSeriesDrama() {
        viewModelScope.launch {
            val series = repository.getAllSeriesDrama()
            _seriesDrama.postValue(series)
        }
    }

    fun loadSeriesFamilia() {
        viewModelScope.launch {
            val series = repository.getAllSeriesFamilia()
            _seriesFamilia.postValue(series)
        }
    }

    fun loadSeriesKids() {
        viewModelScope.launch {
            val series = repository.getAllSeriesKids()
            _seriesKids.postValue(series)
        }
    }

    // Métodos de inserción para películas
    fun insertPeliculasGenero1(movies: List<Genero1MovieEntity>) {
        viewModelScope.launch {
            repository.insertMoviesGenero1(movies)
        }
    }

    fun insertPeliculasGenero2(movies: List<Genero2MovieEntity>) {
        viewModelScope.launch {
            repository.insertMoviesGenero2(movies)
        }
    }

    fun insertPeliculasProximas(movies: List<ProximasMovieEntity>) {
        viewModelScope.launch {
            repository.insertMoviesProximas(movies)
        }
    }

    fun insertPeliculasPopulares(movies: List<PeliculasPopularesEntity>) {
        viewModelScope.launch {
            repository.insertMoviesPopulares(movies)
        }
    }

    fun insertPeliculasUltimosLanzamientos(movies: List<UltimosLanzamientosMovieEntity>) {
        viewModelScope.launch {
            repository.insertMoviesUltimosLanzamientos(movies)
        }
    }

    fun insertPeliculasAccion(movies: List<AccionMovieEntity>) {
        viewModelScope.launch {
            repository.insertMoviesAccion(movies)
        }
    }

    fun insertPeliculasRomance(movies: List<RomanceMovieEntity>) {
        viewModelScope.launch {
            repository.insertMoviesRomance(movies)
        }
    }

    fun insertPeliculasFamilia(movies: List<FamiliaMovieEntity>) {
        viewModelScope.launch {
            repository.insertMoviesFamilia(movies)
        }
    }

    fun insertPeliculasComedia(movies: List<ComediaMovieEntity>) {
        viewModelScope.launch {
            repository.insertMoviesComedia(movies)
        }
    }

    fun insertPeliculasThriller(movies: List<ThrillerMovieEntity>) {
        viewModelScope.launch {
            repository.insertMoviesThriller(movies)
        }
    }

    fun insertPeliculasHorror(movies: List<HorrorMovieEntity>) {
        viewModelScope.launch {
            repository.insertMoviesHorror(movies)
        }
    }

    fun insertPeliculasCienciaFiccion(movies: List<CienciaFiccionMovieEntity>) {
        viewModelScope.launch {
            repository.insertMoviesCienciaFiccion(movies)
        }
    }

    // Métodos de inserción para series
    fun insertSeriesPopulares(series: List<SeriesPopularesEntity>) {
        viewModelScope.launch {
            repository.insertSeriesPopulares(series)
        }
    }

    fun insertSeriesUltimosLanzamientos(series: List<UltimosLanzamientosSeriesEntity>) {
        viewModelScope.launch {
            repository.insertSeriesUltimosLanzamientos(series)
        }
    }

    fun insertSeriesAccionAventura(series: List<AccionAventuraSerieEntity>) {
        viewModelScope.launch {
            repository.insertSeriesAccionAventura(series)
        }
    }

    fun insertSeriesAnimacion(series: List<AnimacionSerieEntity>) {
        viewModelScope.launch {
            repository.insertSeriesAnimacion(series)
        }
    }

    fun insertSeriesComedia(series: List<ComediaSerieEntity>) {
        viewModelScope.launch {
            repository.insertSeriesComedia(series)
        }
    }

    fun insertSeriesCrimen(series: List<CrimenSerieEntity>) {
        viewModelScope.launch {
            repository.insertSeriesCrimen(series)
        }
    }

    fun insertSeriesDrama(series: List<DramaSerieEntity>) {
        viewModelScope.launch {
            repository.insertSeriesDrama(series)
        }
    }

    fun insertSeriesFamilia(series: List<FamiliaSerieEntity>) {
        viewModelScope.launch {
            repository.insertSeriesFamilia(series)
        }
    }

    fun insertSeriesKids(series: List<KidsSerieEntity>) {
        viewModelScope.launch {
            repository.insertSeriesKids(series)
        }
    }
}
