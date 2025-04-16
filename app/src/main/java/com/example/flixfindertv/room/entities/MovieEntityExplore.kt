package com.example.flixfindertv.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.flixfindertv.models.Peliculas

@Entity(tableName = "peliculas_populares")
data class PeliculasPopularesEntity(
    @PrimaryKey val id: String,
    val title: String?,
    val name: String?,
    val overview: String,
    val release_date: String?,
    val release_date_series: String?,
    val poster_path: String,  // Asegurarse de que no sea nulo
    val vote_average: String,
    val vote_count: String,
    val genre_ids: String,
    val adult: Boolean,
    val backdrop_path: String,  // Asegurarse de que no sea nulo
    val popularity: Double,
    val esSerie: Boolean,
    val original_language: String,
    val status: String,
    val director_name: String?,
    val director_photo_url: String?
) {
    fun toPelicula(): Peliculas {
        return Peliculas(
            id = id,
            title = title,
            name = name,
            overview = overview,
            release_date = release_date,
            release_date_series = release_date_series,
            poster_path = poster_path,
            vote_average = vote_average,
            vote_count = vote_count,
            genre_ids = genre_ids.split(",").map { it.toInt() },
            adult = adult,
            backdrop_path = backdrop_path,
            popularity = popularity,
            esSerie = esSerie,
            original_language = original_language,
            status = status,
            director_name = director_name ?: "",
            director_photo_url = director_photo_url ?: ""
        )
    }

    companion object {
        fun fromPelicula(pelicula: Peliculas): PeliculasPopularesEntity {
            return PeliculasPopularesEntity(
                id = pelicula.id,
                title = pelicula.title,
                name = pelicula.name,
                overview = pelicula.overview,
                release_date = pelicula.release_date,
                release_date_series = pelicula.release_date_series,
                poster_path = pelicula.poster_path ?: "", // Valor por defecto si es nulo
                vote_average = pelicula.vote_average,
                vote_count = pelicula.vote_count,
                genre_ids = pelicula.genre_ids.joinToString(","),
                adult = pelicula.adult,
                backdrop_path = pelicula.backdrop_path ?: "", // Valor por defecto si es nulo
                popularity = pelicula.popularity,
                esSerie = pelicula.esSerie,
                original_language = pelicula.original_language,
                status = pelicula.status,
                director_name = pelicula.director_name,
                director_photo_url = pelicula.director_photo_url
            )
        }
    }
}

@Entity(tableName = "ultimos_lanzamientos_peliculas")
data class UltimosLanzamientosMovieEntity(
    @PrimaryKey val id: String,
    val title: String?,
    val name: String?,
    val overview: String,
    val release_date: String?,
    val release_date_series: String?,
    val poster_path: String,  // Asegurarse de que no sea nulo
    val vote_average: String,
    val vote_count: String,
    val genre_ids: String,
    val adult: Boolean,
    val backdrop_path: String,  // Asegurarse de que no sea nulo
    val popularity: Double,
    val esSerie: Boolean,
    val original_language: String,
    val status: String,
    val director_name: String?,
    val director_photo_url: String?
) {
    fun toPelicula(): Peliculas {
        return Peliculas(
            id = id,
            title = title,
            name = name,
            overview = overview,
            release_date = release_date,
            release_date_series = release_date_series,
            poster_path = poster_path,
            vote_average = vote_average,
            vote_count = vote_count,
            genre_ids = genre_ids.split(",").map { it.toInt() },
            adult = adult,
            backdrop_path = backdrop_path,
            popularity = popularity,
            esSerie = esSerie,
            original_language = original_language,
            status = status,
            director_name = director_name ?: "",
            director_photo_url = director_photo_url ?: ""
        )
    }

    companion object {
        fun fromPelicula(pelicula: Peliculas): UltimosLanzamientosMovieEntity {
            return UltimosLanzamientosMovieEntity(
                id = pelicula.id,
                title = pelicula.title,
                name = pelicula.name,
                overview = pelicula.overview,
                release_date = pelicula.release_date,
                release_date_series = pelicula.release_date_series,
                poster_path = pelicula.poster_path ?: "", // Valor por defecto si es nulo
                vote_average = pelicula.vote_average,
                vote_count = pelicula.vote_count,
                genre_ids = pelicula.genre_ids.joinToString(","),
                adult = pelicula.adult,
                backdrop_path = pelicula.backdrop_path ?: "", // Valor por defecto si es nulo
                popularity = pelicula.popularity,
                esSerie = pelicula.esSerie,
                original_language = pelicula.original_language,
                status = pelicula.status,
                director_name = pelicula.director_name,
                director_photo_url = pelicula.director_photo_url
            )
        }
    }
}

@Entity(tableName = "peliculas_accion")
data class AccionMovieEntity(
    @PrimaryKey val id: String,
    val title: String?,
    val name: String?,
    val overview: String,
    val release_date: String?,
    val release_date_series: String?,
    val poster_path: String,  // Asegurarse de que no sea nulo
    val vote_average: String,
    val vote_count: String,
    val genre_ids: String,
    val adult: Boolean,
    val backdrop_path: String,  // Asegurarse de que no sea nulo
    val popularity: Double,
    val esSerie: Boolean,
    val original_language: String,
    val status: String,
    val director_name: String?,
    val director_photo_url: String?
) {
    fun toPelicula(): Peliculas {
        return Peliculas(
            id = id,
            title = title,
            name = name,
            overview = overview,
            release_date = release_date,
            release_date_series = release_date_series,
            poster_path = poster_path,
            vote_average = vote_average,
            vote_count = vote_count,
            genre_ids = genre_ids.split(",").map { it.toInt() },
            adult = adult,
            backdrop_path = backdrop_path,
            popularity = popularity,
            esSerie = esSerie,
            original_language = original_language,
            status = status,
            director_name = director_name ?: "",
            director_photo_url = director_photo_url ?: ""
        )
    }

    companion object {
        fun fromPelicula(pelicula: Peliculas): AccionMovieEntity {
            return AccionMovieEntity(
                id = pelicula.id,
                title = pelicula.title,
                name = pelicula.name,
                overview = pelicula.overview,
                release_date = pelicula.release_date,
                release_date_series = pelicula.release_date_series,
                poster_path = pelicula.poster_path ?: "", // Valor por defecto si es nulo
                vote_average = pelicula.vote_average,
                vote_count = pelicula.vote_count,
                genre_ids = pelicula.genre_ids.joinToString(","),
                adult = pelicula.adult,
                backdrop_path = pelicula.backdrop_path ?: "", // Valor por defecto si es nulo
                popularity = pelicula.popularity,
                esSerie = pelicula.esSerie,
                original_language = pelicula.original_language,
                status = pelicula.status,
                director_name = pelicula.director_name,
                director_photo_url = pelicula.director_photo_url
            )
        }
    }
}

@Entity(tableName = "peliculas_romance")
data class RomanceMovieEntity(
    @PrimaryKey val id: String,
    val title: String?,
    val name: String?,
    val overview: String,
    val release_date: String?,
    val release_date_series: String?,
    val poster_path: String,  // Asegurarse de que no sea nulo
    val vote_average: String,
    val vote_count: String,
    val genre_ids: String,
    val adult: Boolean,
    val backdrop_path: String,  // Asegurarse de que no sea nulo
    val popularity: Double,
    val esSerie: Boolean,
    val original_language: String,
    val status: String,
    val director_name: String?,
    val director_photo_url: String?
) {
    fun toPelicula(): Peliculas {
        return Peliculas(
            id = id,
            title = title,
            name = name,
            overview = overview,
            release_date = release_date,
            release_date_series = release_date_series,
            poster_path = poster_path,
            vote_average = vote_average,
            vote_count = vote_count,
            genre_ids = genre_ids.split(",").map { it.toInt() },
            adult = adult,
            backdrop_path = backdrop_path,
            popularity = popularity,
            esSerie = esSerie,
            original_language = original_language,
            status = status,
            director_name = director_name ?: "",
            director_photo_url = director_photo_url ?: ""
        )
    }

    companion object {
        fun fromPelicula(pelicula: Peliculas): RomanceMovieEntity {
            return RomanceMovieEntity(
                id = pelicula.id,
                title = pelicula.title,
                name = pelicula.name,
                overview = pelicula.overview,
                release_date = pelicula.release_date,
                release_date_series = pelicula.release_date_series,
                poster_path = pelicula.poster_path ?: "", // Valor por defecto si es nulo
                vote_average = pelicula.vote_average,
                vote_count = pelicula.vote_count,
                genre_ids = pelicula.genre_ids.joinToString(","),
                adult = pelicula.adult,
                backdrop_path = pelicula.backdrop_path ?: "", // Valor por defecto si es nulo
                popularity = pelicula.popularity,
                esSerie = pelicula.esSerie,
                original_language = pelicula.original_language,
                status = pelicula.status,
                director_name = pelicula.director_name,
                director_photo_url = pelicula.director_photo_url
            )
        }
    }
}

@Entity(tableName = "peliculas_familia")
data class FamiliaMovieEntity(
    @PrimaryKey val id: String,
    val title: String?,
    val name: String?,
    val overview: String,
    val release_date: String?,
    val release_date_series: String?,
    val poster_path: String,  // Asegurarse de que no sea nulo
    val vote_average: String,
    val vote_count: String,
    val genre_ids: String,
    val adult: Boolean,
    val backdrop_path: String,  // Asegurarse de que no sea nulo
    val popularity: Double,
    val esSerie: Boolean,
    val original_language: String,
    val status: String,
    val director_name: String?,
    val director_photo_url: String?
) {
    fun toPelicula(): Peliculas {
        return Peliculas(
            id = id,
            title = title,
            name = name,
            overview = overview,
            release_date = release_date,
            release_date_series = release_date_series,
            poster_path = poster_path,
            vote_average = vote_average,
            vote_count = vote_count,
            genre_ids = genre_ids.split(",").map { it.toInt() },
            adult = adult,
            backdrop_path = backdrop_path,
            popularity = popularity,
            esSerie = esSerie,
            original_language = original_language,
            status = status,
            director_name = director_name ?: "",
            director_photo_url = director_photo_url ?: ""
        )
    }

    companion object {
        fun fromPelicula(pelicula: Peliculas): FamiliaMovieEntity {
            return FamiliaMovieEntity(
                id = pelicula.id,
                title = pelicula.title,
                name = pelicula.name,
                overview = pelicula.overview,
                release_date = pelicula.release_date,
                release_date_series = pelicula.release_date_series,
                poster_path = pelicula.poster_path ?: "", // Valor por defecto si es nulo
                vote_average = pelicula.vote_average,
                vote_count = pelicula.vote_count,
                genre_ids = pelicula.genre_ids.joinToString(","),
                adult = pelicula.adult,
                backdrop_path = pelicula.backdrop_path ?: "", // Valor por defecto si es nulo
                popularity = pelicula.popularity,
                esSerie = pelicula.esSerie,
                original_language = pelicula.original_language,
                status = pelicula.status,
                director_name = pelicula.director_name,
                director_photo_url = pelicula.director_photo_url
            )
        }
    }
}

@Entity(tableName = "peliculas_comedia")
data class ComediaMovieEntity(
    @PrimaryKey val id: String,
    val title: String?,
    val name: String?,
    val overview: String,
    val release_date: String?,
    val release_date_series: String?,
    val poster_path: String,  // Asegurarse de que no sea nulo
    val vote_average: String,
    val vote_count: String,
    val genre_ids: String,
    val adult: Boolean,
    val backdrop_path: String,  // Asegurarse de que no sea nulo
    val popularity: Double,
    val esSerie: Boolean,
    val original_language: String,
    val status: String,
    val director_name: String?,
    val director_photo_url: String?
) {
    fun toPelicula(): Peliculas {
        return Peliculas(
            id = id,
            title = title,
            name = name,
            overview = overview,
            release_date = release_date,
            release_date_series = release_date_series,
            poster_path = poster_path,
            vote_average = vote_average,
            vote_count = vote_count,
            genre_ids = genre_ids.split(",").map { it.toInt() },
            adult = adult,
            backdrop_path = backdrop_path,
            popularity = popularity,
            esSerie = esSerie,
            original_language = original_language,
            status = status,
            director_name = director_name ?: "",
            director_photo_url = director_photo_url ?: ""
        )
    }

    companion object {
        fun fromPelicula(pelicula: Peliculas): ComediaMovieEntity {
            return ComediaMovieEntity(
                id = pelicula.id,
                title = pelicula.title,
                name = pelicula.name,
                overview = pelicula.overview,
                release_date = pelicula.release_date,
                release_date_series = pelicula.release_date_series,
                poster_path = pelicula.poster_path ?: "", // Valor por defecto si es nulo
                vote_average = pelicula.vote_average,
                vote_count = pelicula.vote_count,
                genre_ids = pelicula.genre_ids.joinToString(","),
                adult = pelicula.adult,
                backdrop_path = pelicula.backdrop_path ?: "", // Valor por defecto si es nulo
                popularity = pelicula.popularity,
                esSerie = pelicula.esSerie,
                original_language = pelicula.original_language,
                status = pelicula.status,
                director_name = pelicula.director_name,
                director_photo_url = pelicula.director_photo_url
            )
        }
    }
}

@Entity(tableName = "peliculas_thriller")
data class ThrillerMovieEntity(
    @PrimaryKey val id: String,
    val title: String?,
    val name: String?,
    val overview: String,
    val release_date: String?,
    val release_date_series: String?,
    val poster_path: String,  // Asegurarse de que no sea nulo
    val vote_average: String,
    val vote_count: String,
    val genre_ids: String,
    val adult: Boolean,
    val backdrop_path: String,  // Asegurarse de que no sea nulo
    val popularity: Double,
    val esSerie: Boolean,
    val original_language: String,
    val status: String,
    val director_name: String?,
    val director_photo_url: String?
) {
    fun toPelicula(): Peliculas {
        return Peliculas(
            id = id,
            title = title,
            name = name,
            overview = overview,
            release_date = release_date,
            release_date_series = release_date_series,
            poster_path = poster_path,
            vote_average = vote_average,
            vote_count = vote_count,
            genre_ids = genre_ids.split(",").map { it.toInt() },
            adult = adult,
            backdrop_path = backdrop_path,
            popularity = popularity,
            esSerie = esSerie,
            original_language = original_language,
            status = status,
            director_name = director_name ?: "",
            director_photo_url = director_photo_url ?: ""
        )
    }

    companion object {
        fun fromPelicula(pelicula: Peliculas): ThrillerMovieEntity {
            return ThrillerMovieEntity(
                id = pelicula.id,
                title = pelicula.title,
                name = pelicula.name,
                overview = pelicula.overview,
                release_date = pelicula.release_date,
                release_date_series = pelicula.release_date_series,
                poster_path = pelicula.poster_path ?: "", // Valor por defecto si es nulo
                vote_average = pelicula.vote_average,
                vote_count = pelicula.vote_count,
                genre_ids = pelicula.genre_ids.joinToString(","),
                adult = pelicula.adult,
                backdrop_path = pelicula.backdrop_path ?: "", // Valor por defecto si es nulo
                popularity = pelicula.popularity,
                esSerie = pelicula.esSerie,
                original_language = pelicula.original_language,
                status = pelicula.status,
                director_name = pelicula.director_name,
                director_photo_url = pelicula.director_photo_url
            )
        }
    }
}

@Entity(tableName = "peliculas_horror")
data class HorrorMovieEntity(
    @PrimaryKey val id: String,
    val title: String?,
    val name: String?,
    val overview: String,
    val release_date: String?,
    val release_date_series: String?,
    val poster_path: String,  // Asegurarse de que no sea nulo
    val vote_average: String,
    val vote_count: String,
    val genre_ids: String,
    val adult: Boolean,
    val backdrop_path: String,  // Asegurarse de que no sea nulo
    val popularity: Double,
    val esSerie: Boolean,
    val original_language: String,
    val status: String,
    val director_name: String?,
    val director_photo_url: String?
) {
    fun toPelicula(): Peliculas {
        return Peliculas(
            id = id,
            title = title,
            name = name,
            overview = overview,
            release_date = release_date,
            release_date_series = release_date_series,
            poster_path = poster_path,
            vote_average = vote_average,
            vote_count = vote_count,
            genre_ids = genre_ids.split(",").map { it.toInt() },
            adult = adult,
            backdrop_path = backdrop_path,
            popularity = popularity,
            esSerie = esSerie,
            original_language = original_language,
            status = status,
            director_name = director_name ?: "",
            director_photo_url = director_photo_url ?: ""
        )
    }

    companion object {
        fun fromPelicula(pelicula: Peliculas): HorrorMovieEntity {
            return HorrorMovieEntity(
                id = pelicula.id,
                title = pelicula.title,
                name = pelicula.name,
                overview = pelicula.overview,
                release_date = pelicula.release_date,
                release_date_series = pelicula.release_date_series,
                poster_path = pelicula.poster_path ?: "", // Valor por defecto si es nulo
                vote_average = pelicula.vote_average,
                vote_count = pelicula.vote_count,
                genre_ids = pelicula.genre_ids.joinToString(","),
                adult = pelicula.adult,
                backdrop_path = pelicula.backdrop_path ?: "", // Valor por defecto si es nulo
                popularity = pelicula.popularity,
                esSerie = pelicula.esSerie,
                original_language = pelicula.original_language,
                status = pelicula.status,
                director_name = pelicula.director_name,
                director_photo_url = pelicula.director_photo_url
            )
        }
    }
}

@Entity(tableName = "peliculas_cienciaficcion")
data class CienciaFiccionMovieEntity(
    @PrimaryKey val id: String,
    val title: String?,
    val name: String?,
    val overview: String,
    val release_date: String?,
    val release_date_series: String?,
    val poster_path: String,  // Asegurarse de que no sea nulo
    val vote_average: String,
    val vote_count: String,
    val genre_ids: String,
    val adult: Boolean,
    val backdrop_path: String,  // Asegurarse de que no sea nulo
    val popularity: Double,
    val esSerie: Boolean,
    val original_language: String,
    val status: String,
    val director_name: String?,
    val director_photo_url: String?
) {
    fun toPelicula(): Peliculas {
        return Peliculas(
            id = id,
            title = title,
            name = name,
            overview = overview,
            release_date = release_date,
            release_date_series = release_date_series,
            poster_path = poster_path,
            vote_average = vote_average,
            vote_count = vote_count,
            genre_ids = genre_ids.split(",").map { it.toInt() },
            adult = adult,
            backdrop_path = backdrop_path,
            popularity = popularity,
            esSerie = esSerie,
            original_language = original_language,
            status = status,
            director_name = director_name ?: "",
            director_photo_url = director_photo_url ?: ""
        )
    }

    companion object {
        fun fromPelicula(pelicula: Peliculas): CienciaFiccionMovieEntity {
            return CienciaFiccionMovieEntity(
                id = pelicula.id,
                title = pelicula.title,
                name = pelicula.name,
                overview = pelicula.overview,
                release_date = pelicula.release_date,
                release_date_series = pelicula.release_date_series,
                poster_path = pelicula.poster_path ?: "", // Valor por defecto si es nulo
                vote_average = pelicula.vote_average,
                vote_count = pelicula.vote_count,
                genre_ids = pelicula.genre_ids.joinToString(","),
                adult = pelicula.adult,
                backdrop_path = pelicula.backdrop_path ?: "", // Valor por defecto si es nulo
                popularity = pelicula.popularity,
                esSerie = pelicula.esSerie,
                original_language = pelicula.original_language,
                status = pelicula.status,
                director_name = pelicula.director_name,
                director_photo_url = pelicula.director_photo_url
            )
        }
    }
}