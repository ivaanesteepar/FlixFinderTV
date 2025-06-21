package com.ivaanesteepar.flixfindertv.room.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ivaanesteepar.flixfindertv.models.Peliculas

@Entity(tableName = "peliculas_genero1")
data class Genero1MovieEntity(
    @Embedded val pelicula: Peliculas,
    @PrimaryKey val idMovieEntity: String = pelicula.id
)

@Entity(tableName = "peliculas_genero2")
data class Genero2MovieEntity(
    @PrimaryKey val idMovieEntity: String,
    @Embedded val pelicula: Peliculas
)

@Entity(tableName = "peliculas_proximas")
data class ProximasMovieEntity(
    @PrimaryKey val idMovieEntity: String,
    @Embedded val pelicula: Peliculas
)


