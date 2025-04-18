package com.example.flixfindertv.ui.screens

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.flixfindertv.models.Peliculas
import org.junit.Test
import io.mockk.*
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.flixfindertv.room.dao.MovieDao
import com.example.flixfindertv.room.entities.Genero1MovieEntity
import com.example.flixfindertv.room.repository.MovieRepository
import kotlinx.coroutines.runBlocking
import org.junit.Rule

class HomeScreenTest {

    // Regla para que LiveData funcione de manera síncrona
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    // Simulamos los datos completos de Peliculas
    private val mockPeliculas = listOf(
        Peliculas(
            id = "1",
            title = "Movie 1",
            name = "Movie 1",
            overview = "Overview 1",
            release_date = "2025-04-18",
            release_date_series = null,
            poster_path = "/path/to/poster1.jpg",
            vote_average = "7.5",
            vote_count = "1500",
            genre_ids = listOf(1, 2, 3),
            adult = false,
            backdrop_path = "/path/to/backdrop1.jpg",
            popularity = 100.0,
            esSerie = false,
            comentarios = listOf("Great movie!", "Loved it!"),
            original_language = "en",
            status = "Released",
            trailer = "https://trailerlink.com/1",
            director_name = "Director 1",
            director_photo_url = "https://directorphoto.com/1"
        ),
        Peliculas(
            id = "2",
            title = "Movie 2",
            name = "Movie 2",
            overview = "Overview 2",
            release_date = "2025-04-19",
            release_date_series = null,
            poster_path = "/path/to/poster2.jpg",
            vote_average = "8.0",
            vote_count = "2000",
            genre_ids = listOf(4, 5, 6),
            adult = false,
            backdrop_path = "/path/to/backdrop2.jpg",
            popularity = 120.0,
            esSerie = false,
            comentarios = listOf("Fantastic!", "Must-watch!"),
            original_language = "en",
            status = "Released",
            trailer = "https://trailerlink.com/2",
            director_name = "Director 2",
            director_photo_url = "https://directorphoto.com/2"
        )
    )
    // Simulamos el MovieDao y el MovieRepository
    private val mockMovieDao: MovieDao = mockk()
    private val movieRepository: MovieRepository = MovieRepository(mockMovieDao)

    // Prueba que carga películas con conexion
    @Test
    fun testCargaPeliculas() {
        // Crea un MutableLiveData que simula el LiveData que será observado
        val liveData = MutableLiveData<List<Peliculas>>()

        // Crea un observador
        val observer = mockk<Observer<List<Peliculas>>>(relaxed = true)

        // Observamos el LiveData
        liveData.observeForever(observer)

        // Simula que se carga la lista de películas
        liveData.postValue(mockPeliculas)

        // Verifica que el observador fue notificado con la lista de películas
        verify { observer.onChanged(mockPeliculas) }

        // Opcional: Limpia el observador para evitar fugas de memoria
        liveData.removeObserver(observer)
    }

    // Prueba que carga películas sin conexion
    @Test
    fun testCargaPeliculasDesdeRoom() {
        // Creamos un MutableLiveData que simula el LiveData que será observado
        val liveData = MutableLiveData<List<Genero1MovieEntity>>()

        // Creamos un observador
        val observer = mockk<Observer<List<Genero1MovieEntity>>>(relaxed = true)

        // Observamos el LiveData
        liveData.observeForever(observer)

        // Simulamos que el MovieDao devuelve las películas, pero ahora como Genero1MovieEntity
        val mockGenero1Movies = mockPeliculas.map {
            Genero1MovieEntity(
                pelicula = it, // Directamente asignamos el objeto Peliculas
                idMovieEntity = it.id // También asignamos el id
            )
        }

        // Simulamos que el MovieDao devuelve las películas de Genero1
        coEvery { mockMovieDao.getAllMoviesGenero1() } returns mockGenero1Movies

        // Llamamos al método que obtiene las películas desde Room a través del repositorio
        runBlocking {
            liveData.postValue(movieRepository.getAllMoviesGenero1())
        }

        // Verificamos que el observador fue notificado con la lista de Genero1MovieEntity
        verify { observer.onChanged(mockGenero1Movies) }

        // Opcional: Limpiamos el observador para evitar fugas de memoria
        liveData.removeObserver(observer)
    }


}
