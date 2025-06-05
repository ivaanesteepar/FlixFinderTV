package com.example.flixfindertv.utils

import com.example.flixfindertv.models.Peliculas
import com.example.flixfindertv.ui.viewmodels.GenresViewModel
import com.example.flixfindertv.ui.viewmodels.UsersViewModel

object PeliculaHelper {
    fun cargarDatosDesdePelicula(
        pelicula: Peliculas,
        esSerie: Boolean,
        genresViewModel: GenresViewModel,
        usersViewModel: UsersViewModel,
        setMovieId: (String) -> Unit,
        setMovieTitle: (String) -> Unit,
        setMovieDescription: (String?) -> Unit,
        setOriginalLanguage: (String) -> Unit,
        setStatus: (String) -> Unit,
        setVoteCount: (String) -> Unit,
        setSeasons: (Int) -> Unit,
        setDuracion: (Int) -> Unit,
        setMovieBannerUrl: (String?) -> Unit,
        setMovieCoverUrl: (String) -> Unit,
        setMoviePopularity: (Double) -> Unit,
        setDirector: (String) -> Unit,
        setDirectorPhoto: (String) -> Unit,
        setMovieGenre: (String) -> Unit,
        setReleaseDate: (String) -> Unit,
        setVoteAverage: (String) -> Unit,
        setTrailerUrl: (String) -> Unit
    ) {
        setMovieId(pelicula.id)
        setMovieTitle(pelicula.title ?: pelicula.name ?: "")
        setMovieDescription(pelicula.overview)
        setOriginalLanguage(pelicula.original_language ?: "")
        setStatus(pelicula.status ?: "")
        setVoteCount(pelicula.vote_count)
        setSeasons(pelicula.seasons ?: 0)
        setDuracion(pelicula.duration ?: 0)

        setMovieBannerUrl(
            pelicula.backdrop_path?.takeIf { it.isNotEmpty() }
                ?.let { "https://image.tmdb.org/t/p/w500$it" }
        )

        setMovieCoverUrl(
            pelicula.poster_path?.takeIf { it.isNotEmpty() }
                ?.let { "https://image.tmdb.org/t/p/w500$it" } ?: ""
        )

        setMoviePopularity(pelicula.popularity)
        setDirector(pelicula.director_name ?: "")
        setDirectorPhoto(pelicula.director_photo_url ?: "")

        if (pelicula.genre_ids.isNotEmpty()) {
            genresViewModel.fetchGenreNames(pelicula.genre_ids) { genres ->
                setMovieGenre(genres.joinToString(", "))
            }
        } else {
            setMovieGenre("Genre not available")
        }

        setReleaseDate(
            if (esSerie) pelicula.release_date_series ?: "Date not available"
            else pelicula.release_date ?: "Date not available"
        )
        setVoteAverage(pelicula.vote_average)
        setTrailerUrl(pelicula.trailer ?: "")

        usersViewModel.checkIfFavorite(pelicula.id, esSerie)

        println("POPULARIDAD PELICULA: ${pelicula.popularity}")
    }
}
