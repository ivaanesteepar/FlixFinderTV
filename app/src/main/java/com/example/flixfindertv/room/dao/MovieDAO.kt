package com.example.flixfindertv.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.flixfindertv.room.entities.*

// Interfaz que define las operaciones para gestionar películas y series en la base de datos
@Dao
interface MovieDao {
    // INSERTS
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoviesGenero1(movies: List<Genero1MovieEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoviesGenero2(movies: List<Genero2MovieEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoviesProximas(movies: List<ProximasMovieEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoviesPopulares(movies: List<PeliculasEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoviesUltimosLanzamientos(movies: List<PeliculasEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoviesAccion(movies: List<PeliculasEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoviesRomance(movies: List<PeliculasEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoviesFamilia(movies: List<PeliculasEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoviesComedia(movies: List<PeliculasEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoviesThriller(movies: List<PeliculasEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoviesHorror(movies: List<PeliculasEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoviesCienciaFiccion(movies: List<PeliculasEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeriesPopulares(movies: List<PeliculasEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeriesUltimosLanzamientos(movies: List<PeliculasEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeriesAccionAventura(movies: List<PeliculasEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeriesAnimacion(movies: List<PeliculasEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeriesComedia(movies: List<PeliculasEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeriesCrimen(movies: List<PeliculasEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeriesDrama(movies: List<PeliculasEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeriesFamilia(movies: List<PeliculasEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeriesKids(movies: List<PeliculasEntity>)


    // GETS
    @Query("SELECT * FROM peliculas")
    suspend fun getAllMovies(): List<PeliculasEntity>

    @Query("SELECT * FROM peliculas_genero1")
    suspend fun getAllMoviesGenero1(): List<Genero1MovieEntity>

    @Query("SELECT * FROM peliculas_genero2")
    suspend fun getAllMoviesGenero2(): List<Genero2MovieEntity>

    @Query("SELECT * FROM peliculas_proximas")
    suspend fun getAllMoviesProximas(): List<ProximasMovieEntity>

    @Query("SELECT * FROM peliculas WHERE esSerie = 0 ORDER BY popularity DESC LIMIT 10")
    suspend fun getAllMoviesPopulares(): List<PeliculasEntity>

    @Query("SELECT * FROM peliculas WHERE esSerie = 0 ORDER BY release_date DESC LIMIT 10")
    suspend fun getAllMoviesUltimosLanzamientos(): List<PeliculasEntity>

    @Query("SELECT * FROM peliculas WHERE genero LIKE '%Action%' AND esSerie = 0 LIMIT 10")
    suspend fun getAllMoviesAccion(): List<PeliculasEntity>

    @Query("SELECT * FROM peliculas WHERE genero LIKE '%Romance%' AND esSerie = 0 LIMIT 10")
    suspend fun getAllMoviesRomance(): List<PeliculasEntity>

    @Query("SELECT * FROM peliculas WHERE genero LIKE '%Family%' AND esSerie = 0 LIMIT 10")
    suspend fun getAllMoviesFamilia(): List<PeliculasEntity>

    @Query("SELECT * FROM peliculas WHERE genero LIKE '%Comedy%' AND esSerie = 0 LIMIT 10")
    suspend fun getAllMoviesComedia(): List<PeliculasEntity>

    @Query("SELECT * FROM peliculas WHERE genero LIKE '%Thriller%' AND esSerie = 0 LIMIT 10")
    suspend fun getAllMoviesThriller(): List<PeliculasEntity>

    @Query("SELECT * FROM peliculas WHERE genero LIKE '%Horror%' AND esSerie = 0 LIMIT 10")
    suspend fun getAllMoviesHorror(): List<PeliculasEntity>

    @Query("SELECT * FROM peliculas WHERE genero LIKE '%Science Fiction%' AND esSerie = 0 LIMIT 10")
    suspend fun getAllMoviesCienciaFiccion(): List<PeliculasEntity>

    @Query("SELECT * FROM peliculas WHERE esSerie = 1 ORDER BY popularity DESC LIMIT 10")
    suspend fun getAllSeriesPopulares(): List<PeliculasEntity>

    @Query("SELECT * FROM peliculas WHERE esSerie = 1 ORDER BY release_date_series DESC LIMIT 10")
    suspend fun getAllSeriesUltimosLanzamientos(): List<PeliculasEntity>

    @Query("SELECT * FROM peliculas WHERE genero LIKE '%Action & Adventure%' AND esSerie = 1 LIMIT 10")
    suspend fun getAllSeriesAccionAventura(): List<PeliculasEntity>

    @Query("SELECT * FROM peliculas WHERE genero LIKE '%Animation%' AND esSerie = 1 LIMIT 10")
    suspend fun getAllSeriesAnimacion(): List<PeliculasEntity>

    @Query("SELECT * FROM peliculas WHERE genero LIKE '%Comedy%' AND esSerie = 1 LIMIT 10")
    suspend fun getAllSeriesComedia(): List<PeliculasEntity>

    @Query("SELECT * FROM peliculas WHERE genero LIKE '%Crime%' AND esSerie = 1 LIMIT 10")
    suspend fun getAllSeriesCrimen(): List<PeliculasEntity>

    @Query("SELECT * FROM peliculas WHERE genero LIKE '%Drama%' AND esSerie = 1 LIMIT 10")
    suspend fun getAllSeriesDrama(): List<PeliculasEntity>

    @Query("SELECT * FROM peliculas WHERE genero LIKE '%Family%' AND esSerie = 1 LIMIT 10")
    suspend fun getAllSeriesFamilia(): List<PeliculasEntity>

    @Query("SELECT * FROM peliculas WHERE genero LIKE '%Kids%' AND esSerie = 1 LIMIT 10")
    suspend fun getAllSeriesKids(): List<PeliculasEntity>


    // DELETES
    @Query("DELETE FROM peliculas_genero1")
    suspend fun deleteAllMoviesGenero1()

    @Query("DELETE FROM peliculas_genero2")
    suspend fun deleteAllMoviesGenero2()

    @Query("DELETE FROM peliculas_proximas")
    suspend fun deleteAllMoviesProximas()

    @Query("DELETE FROM peliculas WHERE esSerie = 0")
    suspend fun deleteAllMovies()

    @Query("DELETE FROM peliculas WHERE esSerie = 1")
    suspend fun deleteAllSeries()


    // FAVORITOS
    @Query("SELECT * FROM favoritos WHERE esSerie = 1")
    suspend fun getSeriesFavoritas(): List<FavoritoEntity>

    @Query("SELECT * FROM favoritos WHERE esSerie = 0")
    suspend fun getPeliculasFavoritas(): List<FavoritoEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFavorito(favorito: FavoritoEntity)

    @Delete
    suspend fun deleteFavorito(favorito: FavoritoEntity)

    @Query("SELECT * FROM favoritos WHERE id = :id")
    suspend fun getFavoritoById(id: String): FavoritoEntity?
}
