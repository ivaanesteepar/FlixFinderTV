package com.example.flixfindertv.room.repository

import com.example.flixfindertv.room.dao.MovieDao
import com.example.flixfindertv.room.entities.*

class MovieRepository(private val movieDao: MovieDao) {

    // ------------------------ INSERTS ------------------------
    suspend fun insertMoviesGenero1(movies: List<Genero1MovieEntity>) = movieDao.insertMoviesGenero1(movies)
    suspend fun insertMoviesGenero2(movies: List<Genero2MovieEntity>) = movieDao.insertMoviesGenero2(movies)
    suspend fun insertMoviesProximas(movies: List<ProximasMovieEntity>) = movieDao.insertMoviesProximas(movies)
    suspend fun insertMoviesPopulares(movies: List<PeliculasPopularesEntity>) = movieDao.insertMoviesPopulares(movies)
    suspend fun insertMoviesUltimosLanzamientos(movies: List<UltimosLanzamientosMovieEntity>) = movieDao.insertMoviesUltimosLanzamientos(movies)
    suspend fun insertMoviesAccion(movies: List<AccionMovieEntity>) = movieDao.insertMoviesAccion(movies)
    suspend fun insertMoviesRomance(movies: List<RomanceMovieEntity>) = movieDao.insertMoviesRomance(movies)
    suspend fun insertMoviesFamilia(movies: List<FamiliaMovieEntity>) = movieDao.insertMoviesFamilia(movies)
    suspend fun insertMoviesComedia(movies: List<ComediaMovieEntity>) = movieDao.insertMoviesComedia(movies)
    suspend fun insertMoviesThriller(movies: List<ThrillerMovieEntity>) = movieDao.insertMoviesThriller(movies)
    suspend fun insertMoviesHorror(movies: List<HorrorMovieEntity>) = movieDao.insertMoviesHorror(movies)
    suspend fun insertMoviesCienciaFiccion(movies: List<CienciaFiccionMovieEntity>) = movieDao.insertMoviesCienciaFiccion(movies)

    suspend fun insertSeriesPopulares(movies: List<SeriesPopularesEntity>) = movieDao.insertSeriesPopulares(movies)
    suspend fun insertSeriesUltimosLanzamientos(movies: List<UltimosLanzamientosSeriesEntity>) = movieDao.insertSeriesUltimosLanzamientos(movies)
    suspend fun insertSeriesAccionAventura(movies: List<AccionAventuraSerieEntity>) = movieDao.insertSeriesAccionAventura(movies)
    suspend fun insertSeriesAnimacion(movies: List<AnimacionSerieEntity>) = movieDao.insertSeriesAnimacion(movies)
    suspend fun insertSeriesComedia(movies: List<ComediaSerieEntity>) = movieDao.insertSeriesComedia(movies)
    suspend fun insertSeriesCrimen(movies: List<CrimenSerieEntity>) = movieDao.insertSeriesCrimen(movies)
    suspend fun insertSeriesDrama(movies: List<DramaSerieEntity>) = movieDao.insertSeriesDrama(movies)
    suspend fun insertSeriesFamilia(movies: List<FamiliaSerieEntity>) = movieDao.insertSeriesFamilia(movies)
    suspend fun insertSeriesKids(movies: List<KidsSerieEntity>) = movieDao.insertSeriesKids(movies)

    // ------------------------ GETS ------------------------
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
    suspend fun deleteAllMoviesPopulares() = movieDao.deleteAllMoviesPopulares()
    suspend fun deleteAllMoviesUltimosLanzamientos() = movieDao.deleteAllMoviesUltimosLanzamientos()
    suspend fun deleteAllMoviesAccion() = movieDao.deleteAllMoviesAccion()
    suspend fun deleteAllMoviesRomance() = movieDao.deleteAllMoviesRomance()
    suspend fun deleteAllMoviesFamilia() = movieDao.deleteAllMoviesFamilia()
    suspend fun deleteAllMoviesComedia() = movieDao.deleteAllMoviesComedia()
    suspend fun deleteAllMoviesThriller() = movieDao.deleteAllMoviesThriller()
    suspend fun deleteAllMoviesHorror() = movieDao.deleteAllMoviesHorror()
    suspend fun deleteAllMoviesCienciaFiccion() = movieDao.deleteAllMoviesCienciaFiccion()

    suspend fun deleteAllSeriesPopulares() = movieDao.deleteAllSeriesPopulares()
    suspend fun deleteAllSeriesUltimosLanzamientos() = movieDao.deleteAllSeriesUltimosLanzamientos()
    suspend fun deleteAllSeriesAccionAventura() = movieDao.deleteAllSeriesAccionAventura()
    suspend fun deleteAllSeriesAnimacion() = movieDao.deleteAllSeriesAnimacion()
    suspend fun deleteAllSeriesComedia() = movieDao.deleteAllSeriesComedia()
    suspend fun deleteAllSeriesCrimen() = movieDao.deleteAllSeriesCrimen()
    suspend fun deleteAllSeriesDrama() = movieDao.deleteAllSeriesDrama()
    suspend fun deleteAllSeriesFamilia() = movieDao.deleteAllSeriesFamilia()
    suspend fun deleteAllSeriesKids() = movieDao.deleteAllSeriesKids()

}