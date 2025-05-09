package com.example.flixfindertv.room.repository

import com.example.flixfindertv.room.dao.MovieDao
import com.example.flixfindertv.room.entities.*

// Repositorio que gestiona las operaciones CRUD sobre películas y series a través del MovieDao
class MovieRepository(private val movieDao: MovieDao) {

    // ------------------------ INSERTS ------------------------
    suspend fun insertMoviesGenero1(movies: List<Genero1MovieEntity>) = movieDao.insertMoviesGenero1(movies)
    suspend fun insertMoviesGenero2(movies: List<Genero2MovieEntity>) = movieDao.insertMoviesGenero2(movies)
    suspend fun insertMoviesProximas(movies: List<ProximasMovieEntity>) = movieDao.insertMoviesProximas(movies)
    suspend fun insertMoviesPopulares(movies: List<PeliculasEntity>) = movieDao.insertMoviesPopulares(movies)
    suspend fun insertMoviesUltimosLanzamientos(movies: List<PeliculasEntity>) = movieDao.insertMoviesUltimosLanzamientos(movies)
    suspend fun insertMoviesAccion(movies: List<PeliculasEntity>) = movieDao.insertMoviesAccion(movies)
    suspend fun insertMoviesRomance(movies: List<PeliculasEntity>) = movieDao.insertMoviesRomance(movies)
    suspend fun insertMoviesFamilia(movies: List<PeliculasEntity>) = movieDao.insertMoviesFamilia(movies)
    suspend fun insertMoviesComedia(movies: List<PeliculasEntity>) = movieDao.insertMoviesComedia(movies)
    suspend fun insertMoviesThriller(movies: List<PeliculasEntity>) = movieDao.insertMoviesThriller(movies)
    suspend fun insertMoviesHorror(movies: List<PeliculasEntity>) = movieDao.insertMoviesHorror(movies)
    suspend fun insertMoviesCienciaFiccion(movies: List<PeliculasEntity>) = movieDao.insertMoviesCienciaFiccion(movies)

    suspend fun insertSeriesPopulares(movies: List<PeliculasEntity>) = movieDao.insertSeriesPopulares(movies)
    suspend fun insertSeriesUltimosLanzamientos(movies: List<PeliculasEntity>) = movieDao.insertSeriesUltimosLanzamientos(movies)
    suspend fun insertSeriesAccionAventura(movies: List<PeliculasEntity>) = movieDao.insertSeriesAccionAventura(movies)
    suspend fun insertSeriesAnimacion(movies: List<PeliculasEntity>) = movieDao.insertSeriesAnimacion(movies)
    suspend fun insertSeriesComedia(movies: List<PeliculasEntity>) = movieDao.insertSeriesComedia(movies)
    suspend fun insertSeriesCrimen(movies: List<PeliculasEntity>) = movieDao.insertSeriesCrimen(movies)
    suspend fun insertSeriesDrama(movies: List<PeliculasEntity>) = movieDao.insertSeriesDrama(movies)
    suspend fun insertSeriesFamilia(movies: List<PeliculasEntity>) = movieDao.insertSeriesFamilia(movies)
    suspend fun insertSeriesKids(movies: List<PeliculasEntity>) = movieDao.insertSeriesKids(movies)

    // ------------------------ GETS ------------------------
    suspend fun getAllMovies() = movieDao.getAllMovies()
    suspend fun getAllMoviesGenero1() = movieDao.getAllMoviesGenero1()
    suspend fun getAllMoviesGenero2() = movieDao.getAllMoviesGenero2()
    suspend fun getAllMoviesProximas() = movieDao.getAllMoviesProximas()
    suspend fun getAllMoviesPopulares() = movieDao.getAllMoviesPopulares()
    suspend fun getAllMoviesUltimosLanzamientos() = movieDao.getAllMoviesUltimosLanzamientos()
    suspend fun getAllMoviesAccion() = movieDao.getAllMoviesAccion()
    suspend fun getAllMoviesRomance() = movieDao.getAllMoviesRomance()
    suspend fun getAllMoviesFamilia() = movieDao.getAllMoviesFamilia()
    suspend fun getAllMoviesComedia() = movieDao.getAllMoviesComedia()
    suspend fun getAllMoviesThriller() = movieDao.getAllMoviesThriller()
    suspend fun getAllMoviesHorror() = movieDao.getAllMoviesHorror()
    suspend fun getAllMoviesCienciaFiccion() = movieDao.getAllMoviesCienciaFiccion()

    suspend fun getAllSeriesPopulares() = movieDao.getAllSeriesPopulares()
    suspend fun getAllSeriesUltimosLanzamientos() = movieDao.getAllSeriesUltimosLanzamientos()
    suspend fun getAllSeriesAccionAventura() = movieDao.getAllSeriesAccionAventura()
    suspend fun getAllSeriesAnimacion() = movieDao.getAllSeriesAnimacion()
    suspend fun getAllSeriesComedia() = movieDao.getAllSeriesComedia()
    suspend fun getAllSeriesCrimen() = movieDao.getAllSeriesCrimen()
    suspend fun getAllSeriesDrama() = movieDao.getAllSeriesDrama()
    suspend fun getAllSeriesFamilia() = movieDao.getAllSeriesFamilia()
    suspend fun getAllSeriesKids() = movieDao.getAllSeriesKids()

    // ------------------------ DELETES ------------------------
    suspend fun deleteAllMoviesGenero1() = movieDao.deleteAllMoviesGenero1()
    suspend fun deleteAllMoviesGenero2() = movieDao.deleteAllMoviesGenero2()
    suspend fun deleteAllMoviesProximas() = movieDao.deleteAllMoviesProximas()
    suspend fun deleteAllMovies() = movieDao.deleteAllMovies()
    suspend fun deleteAllSeries() = movieDao.deleteAllSeries()


    // ------------------------ FAVORITOS ------------------------
    suspend fun getSeriesFavoritas(): List<FavoritoEntity> {
        return movieDao.getSeriesFavoritas()
    }

    // Obtener las películas favoritas
    suspend fun getPeliculasFavoritas(): List<FavoritoEntity> {
        return movieDao.getPeliculasFavoritas()
    }

    // Insertar una película o serie en los favoritos
    suspend fun insertFavorito(favorito: FavoritoEntity) {
        movieDao.insertFavorito(favorito)
    }

    // Eliminar una película o serie de los favoritos
    suspend fun deleteFavorito(favorito: FavoritoEntity) {
        movieDao.deleteFavorito(favorito)
    }

    // Obtener un favorito por ID
    suspend fun getFavoritoById(id: String): FavoritoEntity? {
        return movieDao.getFavoritoById(id)
    }

}