package com.example.flixfindertv.room.repository

import com.example.flixfindertv.room.dao.MovieDao
import com.example.flixfindertv.room.entities.Genero1MovieEntity
import com.example.flixfindertv.room.entities.Genero2MovieEntity
import com.example.flixfindertv.room.entities.ProximasMovieEntity

class MovieRepository(private val movieDao: MovieDao) {

    // Insertar películas del Género 1
    suspend fun insertMoviesGenero1(movies: List<Genero1MovieEntity>) {
        movieDao.insertMoviesGenero1(movies)
    }

    // Insertar películas del Género 2
    suspend fun insertMoviesGenero2(movies: List<Genero2MovieEntity>) {
        movieDao.insertMoviesGenero2(movies)
    }

    // Insertar películas próximas
    suspend fun insertMoviesProximas(movies: List<ProximasMovieEntity>) {
        movieDao.insertMoviesProximas(movies)
    }

    // Obtener todas las películas del Género 1
    suspend fun getAllMoviesGenero1(): List<Genero1MovieEntity> {
        return movieDao.getAllMoviesGenero1()
    }

    // Obtener todas las películas del Género 2
    suspend fun getAllMoviesGenero2(): List<Genero2MovieEntity> {
        return movieDao.getAllMoviesGenero2()
    }

    // Obtener todas las películas próximas
    suspend fun getAllMoviesProximas(): List<ProximasMovieEntity> {
        return movieDao.getAllMoviesProximas()
    }

    // Eliminar todas las películas del Género 1
    suspend fun deleteAllMoviesGenero1() {
        movieDao.deleteAllMoviesGenero1()
    }

    // Eliminar todas las películas del Género 2
    suspend fun deleteAllMoviesGenero2() {
        movieDao.deleteAllMoviesGenero2()
    }

    // Eliminar todas las películas próximas
    suspend fun deleteAllMoviesProximas() {
        movieDao.deleteAllMoviesProximas()
    }

}