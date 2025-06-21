package com.ivaanesteepar.flixfindertv.room.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ivaanesteepar.flixfindertv.models.Peliculas

@Entity(tableName = "favoritos")
data class FavoritoEntity(
    @PrimaryKey val idMovieEntity: String,
    @Embedded val pelicula: Peliculas,
    val userId: String
)

