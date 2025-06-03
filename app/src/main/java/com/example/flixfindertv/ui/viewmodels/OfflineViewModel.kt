package com.example.flixfindertv.ui.viewmodels

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.flixfindertv.models.Peliculas
import com.example.flixfindertv.room.dao.MovieDao
import com.example.flixfindertv.room.database.AppDatabase
import com.example.flixfindertv.room.entities.*
import com.example.flixfindertv.room.repository.MovieRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class OfflineViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OfflineViewModel::class.java)) {
            return OfflineViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class OfflineViewModel(application: Application) : AndroidViewModel(application) {

    val repository: MovieRepository
    val genresViewModel: GenresViewModel

    private var _listaPeliculasGenero1 = MutableLiveData<List<Genero1MovieEntity>>(emptyList())
    val listaPeliculasGenero1: LiveData<List<Genero1MovieEntity>> = _listaPeliculasGenero1

    private var _listaPeliculasGenero2 = MutableLiveData<List<Genero2MovieEntity>>(emptyList())
    val listaPeliculasGenero2: LiveData<List<Genero2MovieEntity>> = _listaPeliculasGenero2

    private var _listaPeliculasProximas = MutableLiveData<List<ProximasMovieEntity>>(emptyList())
    val listaPeliculasProximas: LiveData<List<ProximasMovieEntity>> = _listaPeliculasProximas

    private val _peliculasPopulares = MutableLiveData<List<PeliculasEntity>>(emptyList())
    val peliculasPopulares: LiveData<List<PeliculasEntity>> = _peliculasPopulares

    private val _peliculasUltimosLanzamientos = MutableLiveData<List<PeliculasEntity>>(emptyList())
    val peliculasUltimosLanzamientos: LiveData<List<PeliculasEntity>> = _peliculasUltimosLanzamientos

    private val _peliculasAccion = MutableLiveData<List<PeliculasEntity>>(emptyList())
    val peliculasAccion: LiveData<List<PeliculasEntity>> = _peliculasAccion

    private val _peliculasRomance = MutableLiveData<List<PeliculasEntity>>(emptyList())
    val peliculasRomance: LiveData<List<PeliculasEntity>> = _peliculasRomance

    private val _peliculasFamilia = MutableLiveData<List<PeliculasEntity>>(emptyList())
    val peliculasFamilia: LiveData<List<PeliculasEntity>> = _peliculasFamilia

    private val _peliculasComedia = MutableLiveData<List<PeliculasEntity>>(emptyList())
    val peliculasComedia: LiveData<List<PeliculasEntity>> = _peliculasComedia

    private val _peliculasThriller = MutableLiveData<List<PeliculasEntity>>(emptyList())
    val peliculasThriller: LiveData<List<PeliculasEntity>> = _peliculasThriller

    private val _peliculasHorror = MutableLiveData<List<PeliculasEntity>>(emptyList())
    val peliculasHorror: LiveData<List<PeliculasEntity>> = _peliculasHorror

    private val _peliculasCienciaFiccion = MutableLiveData<List<PeliculasEntity>>(emptyList())
    val peliculasCienciaFiccion: LiveData<List<PeliculasEntity>> = _peliculasCienciaFiccion

    private val _seriesPopulares = MutableLiveData<List<PeliculasEntity>>(emptyList())
    val seriesPopulares: LiveData<List<PeliculasEntity>> = _seriesPopulares

    private val _seriesUltimosLanzamientos = MutableLiveData<List<PeliculasEntity>>(emptyList())
    val seriesUltimosLanzamientos: LiveData<List<PeliculasEntity>> = _seriesUltimosLanzamientos

    private val _seriesAccionAventura = MutableLiveData<List<PeliculasEntity>>(emptyList())
    val seriesAccionAventura: LiveData<List<PeliculasEntity>> = _seriesAccionAventura

    private val _seriesAnimacion = MutableLiveData<List<PeliculasEntity>>(emptyList())
    val seriesAnimacion: LiveData<List<PeliculasEntity>> = _seriesAnimacion

    private val _seriesComedia = MutableLiveData<List<PeliculasEntity>>(emptyList())
    val seriesComedia: LiveData<List<PeliculasEntity>> = _seriesComedia

    private val _seriesCrimen = MutableLiveData<List<PeliculasEntity>>(emptyList())
    val seriesCrimen: LiveData<List<PeliculasEntity>> = _seriesCrimen

    private val _seriesDrama = MutableLiveData<List<PeliculasEntity>>(emptyList())
    val seriesDrama: LiveData<List<PeliculasEntity>> = _seriesDrama

    private val _seriesFamilia = MutableLiveData<List<PeliculasEntity>>(emptyList())
    val seriesFamilia: LiveData<List<PeliculasEntity>> = _seriesFamilia

    private val _seriesKids = MutableLiveData<List<PeliculasEntity>>(emptyList())
    val seriesKids: LiveData<List<PeliculasEntity>> = _seriesKids

    private val movieDao: MovieDao = AppDatabase.getDatabase(application).movieDao()

    init {
        genresViewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
            .create(GenresViewModel::class.java)
        // Acceder al contexto de la aplicación
        val context = application.applicationContext
        // Inicializar el DAO usando el contexto
        val dao = AppDatabase.getDatabase(context).movieDao()
        // Crear el repositorio con el DAO
        repository = MovieRepository(dao)
    }

    val countGeneros1: StateFlow<Int> = movieDao.countGenero1Movies()
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val countGeneros2: StateFlow<Int> = movieDao.countGenero2Movies()
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val countProximas: StateFlow<Int> = movieDao.countProximasMovies()
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)





    fun guardarPeliculasEnRoom(
        genero1: List<Peliculas>,
        genero2: List<Peliculas>,
        peliculasProximas: List<Peliculas>
    ) {
        // Limpiar las tablas antes de insertar nuevas películas
        limpiarPeliculasGenero1()
        limpiarPeliculasGenero2()
        limpiarPeliculasProximas()

        // Insertar las primeras 10 películas de cada categoría
        insertPeliculasGenero1(
            genero1.take(10).map { Genero1MovieEntity(idMovieEntity = it.id, pelicula = it) }
        )

        insertPeliculasGenero2(
            genero2.take(10).map { Genero2MovieEntity(idMovieEntity = it.id, pelicula = it) }
        )

        insertPeliculasProximas(
            peliculasProximas.take(10).map { ProximasMovieEntity(idMovieEntity = it.id, pelicula = it) }
        )
    }


    suspend fun getAllMovies(): List<PeliculasEntity> {
        return repository.getAllMovies()
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

    fun limpiarPeliculas() {
        viewModelScope.launch {
            repository.deleteAllMovies()
        }
    }

    fun limpiarSeries() {
        viewModelScope.launch {
            repository.deleteAllSeries()
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

    fun insertPeliculasPopulares(movies: List<Peliculas>) {
        viewModelScope.launch {
            // Convierte las películas a PeliculasEntity con los géneros
            val peliculasConGeneros = movies.map { pelicula ->
                suspendCancellableCoroutine { continuation ->
                    genresViewModel.createPeliculaEntityWithGeneros(pelicula) { peliculaEntity ->
                        continuation.resume(peliculaEntity)
                    }
                }
            }

            // Inserta las películas con los géneros en la base de datos
            repository.insertMoviesPopulares(peliculasConGeneros)
        }
    }

    fun insertPeliculasUltimosLanzamientos(movies: List<Peliculas>) {
        viewModelScope.launch {
            // Convierte las películas a PeliculasEntity con los géneros
            val peliculasConGeneros = movies.map { pelicula ->
                suspendCancellableCoroutine { continuation ->
                    genresViewModel.createPeliculaEntityWithGeneros(pelicula) { peliculaEntity ->
                        continuation.resume(peliculaEntity)
                    }
                }
            }

            // Inserta las películas con los géneros en la base de datos
            repository.insertMoviesUltimosLanzamientos(peliculasConGeneros)
        }
    }

    fun insertPeliculasAccion(peliculas: List<Peliculas>) {
        viewModelScope.launch {
            // Convierte las películas a PeliculasEntity con los géneros
            val peliculasConGeneros = peliculas.map { pelicula ->
                suspendCancellableCoroutine { continuation ->
                    genresViewModel.createPeliculaEntityWithGeneros(pelicula) { peliculaEntity ->
                        continuation.resume(peliculaEntity)
                    }
                }
            }

            // Inserta las películas con los géneros en la base de datos
            repository.insertMoviesAccion(peliculasConGeneros)
        }
    }

    fun insertPeliculasRomance(peliculas: List<Peliculas>) {
        viewModelScope.launch {
            // Convierte las películas a PeliculasEntity antes de insertarlas
            val peliculasConGeneros = peliculas.map { pelicula ->
                suspendCancellableCoroutine { continuation ->
                    genresViewModel.createPeliculaEntityWithGeneros(pelicula) { peliculaEntity ->
                        continuation.resume(peliculaEntity)
                    }
                }
            }

            // Inserta las películas con los géneros en la base de datos
            repository.insertMoviesRomance(peliculasConGeneros)
        }
    }

    fun insertPeliculasFamilia(movies: List<Peliculas>) {
        viewModelScope.launch {
            // Convierte las películas a PeliculasEntity antes de insertarlas
            val peliculasConGeneros = movies.map { pelicula ->
                suspendCancellableCoroutine { continuation ->
                    genresViewModel.createPeliculaEntityWithGeneros(pelicula) { peliculaEntity ->
                        continuation.resume(peliculaEntity)
                    }
                }
            }

            // Inserta las películas con los géneros en la base de datos
            repository.insertMoviesFamilia(peliculasConGeneros)
        }
    }

    fun insertPeliculasComedia(movies: List<Peliculas>) {
        viewModelScope.launch {
            // Convierte las películas a PeliculasEntity con géneros
            val peliculasComedia = movies.map { pelicula ->
                suspendCancellableCoroutine { continuation ->
                    genresViewModel.createPeliculaEntityWithGeneros(pelicula) { peliculaEntity ->
                        continuation.resume(peliculaEntity)
                    }
                }
            }

            // Inserta las películas de comedia en la base de datos
            repository.insertMoviesComedia(peliculasComedia)
        }
    }

    fun insertPeliculasThriller(movies: List<Peliculas>) {
        viewModelScope.launch {
            // Convierte las películas a PeliculasEntity con géneros
            val peliculasThriller = movies.map { pelicula ->
                suspendCancellableCoroutine { continuation ->
                    genresViewModel.createPeliculaEntityWithGeneros(pelicula) { peliculaEntity ->
                        continuation.resume(peliculaEntity)
                    }
                }
            }

            // Inserta las películas de thriller en la base de datos
            repository.insertMoviesThriller(peliculasThriller)
        }
    }

    fun insertPeliculasHorror(movies: List<Peliculas>) {
        viewModelScope.launch {
            // Convierte las películas a PeliculasEntity con géneros
            val peliculasHorror = movies.map { pelicula ->
                suspendCancellableCoroutine { continuation ->
                    genresViewModel.createPeliculaEntityWithGeneros(pelicula) { peliculaEntity ->
                        continuation.resume(peliculaEntity)
                    }
                }
            }

            // Inserta las películas de horror en la base de datos
            repository.insertMoviesHorror(peliculasHorror)
        }
    }

    fun insertPeliculasCienciaFiccion(movies: List<Peliculas>) {
        viewModelScope.launch {
            // Convierte las películas a PeliculasEntity con géneros
            val peliculasCienciaFiccion = movies.map { pelicula ->
                suspendCancellableCoroutine { continuation ->
                    genresViewModel.createPeliculaEntityWithGeneros(pelicula) { peliculaEntity ->
                        continuation.resume(peliculaEntity)
                    }
                }
            }

            // Inserta las películas de ciencia ficción en la base de datos
            repository.insertMoviesCienciaFiccion(peliculasCienciaFiccion)
        }
    }

    // Métodos de inserción para series, pero usando PeliculasEntity
    fun insertSeriesPopulares(series: List<Peliculas>) {
        viewModelScope.launch {
            // Convierte las series a PeliculasEntity con géneros
            val peliculasPopulares = series.map { serie ->
                suspendCancellableCoroutine { continuation ->
                    genresViewModel.createPeliculaEntityWithGeneros(serie) { peliculaEntity ->
                        continuation.resume(peliculaEntity)
                    }
                }
            }

            // Inserta las series populares en la base de datos usando PeliculasEntity
            repository.insertSeriesPopulares(peliculasPopulares)
        }
    }

    fun insertSeriesUltimosLanzamientos(series: List<Peliculas>) {
        viewModelScope.launch {
            // Convierte las series a PeliculasEntity con géneros
            val peliculasUltimosLanzamientos = series.map { serie ->
                suspendCancellableCoroutine { continuation ->
                    genresViewModel.createPeliculaEntityWithGeneros(serie) { peliculaEntity ->
                        continuation.resume(peliculaEntity)
                    }
                }
            }

            // Inserta las series de últimos lanzamientos en la base de datos usando PeliculasEntity
            repository.insertSeriesUltimosLanzamientos(peliculasUltimosLanzamientos)
        }
    }

    fun insertSeriesAccionAventura(series: List<Peliculas>) {
        viewModelScope.launch {
            // Convierte las series a PeliculasEntity con géneros
            val peliculasAccionAventura = series.map { serie ->
                suspendCancellableCoroutine { continuation ->
                    genresViewModel.createPeliculaEntityWithGeneros(serie) { peliculaEntity ->
                        continuation.resume(peliculaEntity)
                    }
                }
            }

            // Inserta las series de acción y aventura en la base de datos usando PeliculasEntity
            repository.insertSeriesAccionAventura(peliculasAccionAventura)
        }
    }

    fun insertSeriesAnimacion(series: List<Peliculas>) {
        viewModelScope.launch {
            // Convierte las series a PeliculasEntity con géneros
            val peliculasAnimacion = series.map { serie ->
                suspendCancellableCoroutine { continuation ->
                    genresViewModel.createPeliculaEntityWithGeneros(serie) { peliculaEntity ->
                        continuation.resume(peliculaEntity)
                    }
                }
            }

            // Inserta las series de animación en la base de datos usando PeliculasEntity
            repository.insertSeriesAnimacion(peliculasAnimacion)
        }
    }

    fun insertSeriesComedia(series: List<Peliculas>) {
        viewModelScope.launch {
            // Convierte las series a PeliculasEntity con géneros
            val peliculasComedia = series.map { serie ->
                suspendCancellableCoroutine { continuation ->
                    genresViewModel.createPeliculaEntityWithGeneros(serie) { peliculaEntity ->
                        continuation.resume(peliculaEntity)
                    }
                }
            }

            // Inserta las series de comedia en la base de datos usando PeliculasEntity
            repository.insertSeriesComedia(peliculasComedia)
        }
    }

    fun insertSeriesCrimen(series: List<Peliculas>) {
        viewModelScope.launch {
            // Convierte las series a PeliculasEntity con géneros
            val peliculasCrimen = series.map { serie ->
                suspendCancellableCoroutine { continuation ->
                    genresViewModel.createPeliculaEntityWithGeneros(serie) { peliculaEntity ->
                        continuation.resume(peliculaEntity)
                    }
                }
            }

            // Inserta las series de crimen en la base de datos usando PeliculasEntity
            repository.insertSeriesCrimen(peliculasCrimen)
        }
    }

    fun insertSeriesDrama(series: List<Peliculas>) {
        viewModelScope.launch {
            // Convierte las series a PeliculasEntity con géneros
            val peliculasDrama = series.map { serie ->
                suspendCancellableCoroutine { continuation ->
                    genresViewModel.createPeliculaEntityWithGeneros(serie) { peliculaEntity ->
                        continuation.resume(peliculaEntity)
                    }
                }
            }

            // Inserta las series de drama en la base de datos usando PeliculasEntity
            repository.insertSeriesDrama(peliculasDrama)
        }
    }

    fun insertSeriesFamilia(series: List<Peliculas>) {
        viewModelScope.launch {
            // Convierte las series a PeliculasEntity con géneros
            val peliculasFamilia = series.map { serie ->
                suspendCancellableCoroutine { continuation ->
                    genresViewModel.createPeliculaEntityWithGeneros(serie) { peliculaEntity ->
                        continuation.resume(peliculaEntity)
                    }
                }
            }

            // Inserta las series de familia en la base de datos usando PeliculasEntity
            repository.insertSeriesFamilia(peliculasFamilia)
        }
    }

    fun insertSeriesKids(series: List<Peliculas>) {
        viewModelScope.launch {
            // Convierte las series a PeliculasEntity con géneros
            val peliculasKids = series.map { serie ->
                suspendCancellableCoroutine { continuation ->
                    genresViewModel.createPeliculaEntityWithGeneros(serie) { peliculaEntity ->
                        continuation.resume(peliculaEntity)
                    }
                }
            }

            // Inserta las series para niños en la base de datos usando PeliculasEntity
            repository.insertSeriesKids(peliculasKids)
        }
    }


}
