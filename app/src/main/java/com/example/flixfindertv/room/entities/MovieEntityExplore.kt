package com.example.flixfindertv.room.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.flixfindertv.models.Peliculas

@Entity(tableName = "peliculas_populares")
data class PeliculasPopularesEntity(
    @Embedded val pelicula: Peliculas,
    @PrimaryKey val idMovieEntity: String = pelicula.id
)


@Entity(tableName = "ultimos_lanzamientos_peliculas")
data class UltimosLanzamientosMovieEntity(
    @Embedded val pelicula: Peliculas,
    @PrimaryKey val idMovieEntity: String = pelicula.id
)


@Entity(tableName = "peliculas_accion")
data class AccionMovieEntity(
    @Embedded val pelicula: Peliculas,
    @PrimaryKey val idMovieEntity: String = pelicula.id
)


@Entity(tableName = "peliculas_romance")
data class RomanceMovieEntity(
    @Embedded val pelicula: Peliculas,
    @PrimaryKey val idMovieEntity: String = pelicula.id
)


@Entity(tableName = "peliculas_familia")
data class FamiliaMovieEntity(
    @Embedded val pelicula: Peliculas,
    @PrimaryKey val idMovieEntity: String = pelicula.id
)


@Entity(tableName = "peliculas_comedia")
data class ComediaMovieEntity(
    @Embedded val pelicula: Peliculas,
    @PrimaryKey val idMovieEntity: String = pelicula.id
)


@Entity(tableName = "peliculas_thriller")
data class ThrillerMovieEntity(
    @Embedded val pelicula: Peliculas,
    @PrimaryKey val idMovieEntity: String = pelicula.id
)

@Entity(tableName = "peliculas_horror")
data class HorrorMovieEntity(
    @Embedded val pelicula: Peliculas,
    @PrimaryKey val idMovieEntity: String = pelicula.id
)

@Entity(tableName = "peliculas_cienciaficcion")
data class CienciaFiccionMovieEntity(
    @Embedded val pelicula: Peliculas,
    @PrimaryKey val idMovieEntity: String = pelicula.id
)