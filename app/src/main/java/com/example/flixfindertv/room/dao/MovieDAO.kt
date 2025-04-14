package com.example.flixfindertv.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.flixfindertv.room.entities.Genero1MovieEntity
import com.example.flixfindertv.room.entities.Genero2MovieEntity
import com.example.flixfindertv.room.entities.ProximasMovieEntity

@Dao
interface MovieDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoviesGenero1(movies: List<Genero1MovieEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoviesGenero2(movies: List<Genero2MovieEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoviesProximas(movies: List<ProximasMovieEntity>)

    @Query("SELECT * FROM peliculas_genero1")
    suspend fun getAllMoviesGenero1(): List<Genero1MovieEntity>

    @Query("SELECT * FROM peliculas_genero2")
    suspend fun getAllMoviesGenero2(): List<Genero2MovieEntity>

    @Query("SELECT * FROM peliculas_proximas")
    suspend fun getAllMoviesProximas(): List<ProximasMovieEntity>

    // Eliminar todas las películas del Género 1
    @Query("DELETE FROM peliculas_genero1")
    suspend fun deleteAllMoviesGenero1()

    // Eliminar todas las películas del Género 2
    @Query("DELETE FROM peliculas_genero2")
    suspend fun deleteAllMoviesGenero2()

    // Eliminar todas las películas próximas
    @Query("DELETE FROM peliculas_proximas")
    suspend fun deleteAllMoviesProximas()
}
