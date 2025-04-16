package com.example.flixfindertv.room.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.flixfindertv.models.Peliculas

@Entity(tableName = "series_populares")
data class SeriesPopularesEntity(
    @Embedded val pelicula: Peliculas,
    @PrimaryKey val idMovieEntity: String = pelicula.id
)

@Entity(tableName = "ultimos_lanzamientos_series")
data class UltimosLanzamientosSeriesEntity(
    @Embedded val pelicula: Peliculas,
    @PrimaryKey val idMovieEntity: String = pelicula.id
)

@Entity(tableName = "series_accion_aventura")
data class AccionAventuraSerieEntity(
    @Embedded val pelicula: Peliculas,
    @PrimaryKey val idMovieEntity: String = pelicula.id
)

@Entity(tableName = "series_animacion")
data class AnimacionSerieEntity(
    @Embedded val pelicula: Peliculas,
    @PrimaryKey val idMovieEntity: String = pelicula.id
)

@Entity(tableName = "series_comedia")
data class ComediaSerieEntity(
    @Embedded val pelicula: Peliculas,
    @PrimaryKey val idMovieEntity: String = pelicula.id
)


@Entity(tableName = "series_crimen")
data class CrimenSerieEntity(
    @Embedded val pelicula: Peliculas,
    @PrimaryKey val idMovieEntity: String = pelicula.id
)

@Entity(tableName = "series_drama")
data class DramaSerieEntity(
    @Embedded val pelicula: Peliculas,
    @PrimaryKey val idMovieEntity: String = pelicula.id
)

@Entity(tableName = "series_familia")
data class FamiliaSerieEntity(
    @Embedded val pelicula: Peliculas,
    @PrimaryKey val idMovieEntity: String = pelicula.id
)

@Entity(tableName = "series_kids")
data class KidsSerieEntity(
    @Embedded val pelicula: Peliculas,
    @PrimaryKey val idMovieEntity: String = pelicula.id
)