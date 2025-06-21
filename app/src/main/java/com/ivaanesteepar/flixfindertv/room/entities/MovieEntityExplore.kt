package com.ivaanesteepar.flixfindertv.room.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ivaanesteepar.flixfindertv.models.Peliculas

@Entity(tableName = "peliculas")
data class PeliculasEntity(
    @Embedded val pelicula: Peliculas,
    @PrimaryKey val idMovieEntity: String = pelicula.id,
    val genero: String
)