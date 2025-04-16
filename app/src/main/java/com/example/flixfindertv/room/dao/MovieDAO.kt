package com.example.flixfindertv.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.flixfindertv.room.entities.AccionAventuraSerieEntity
import com.example.flixfindertv.room.entities.AccionMovieEntity
import com.example.flixfindertv.room.entities.AnimacionSerieEntity
import com.example.flixfindertv.room.entities.CienciaFiccionMovieEntity
import com.example.flixfindertv.room.entities.ComediaMovieEntity
import com.example.flixfindertv.room.entities.ComediaSerieEntity
import com.example.flixfindertv.room.entities.CrimenSerieEntity
import com.example.flixfindertv.room.entities.DramaSerieEntity
import com.example.flixfindertv.room.entities.FamiliaMovieEntity
import com.example.flixfindertv.room.entities.FamiliaSerieEntity
import com.example.flixfindertv.room.entities.Genero1MovieEntity
import com.example.flixfindertv.room.entities.Genero2MovieEntity
import com.example.flixfindertv.room.entities.HorrorMovieEntity
import com.example.flixfindertv.room.entities.KidsSerieEntity
import com.example.flixfindertv.room.entities.PeliculasPopularesEntity
import com.example.flixfindertv.room.entities.ProximasMovieEntity
import com.example.flixfindertv.room.entities.RomanceMovieEntity
import com.example.flixfindertv.room.entities.SeriesPopularesEntity
import com.example.flixfindertv.room.entities.ThrillerMovieEntity
import com.example.flixfindertv.room.entities.UltimosLanzamientosMovieEntity
import com.example.flixfindertv.room.entities.UltimosLanzamientosSeriesEntity

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
    suspend fun insertMoviesPopulares(movies: List<PeliculasPopularesEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoviesUltimosLanzamientos(movies: List<UltimosLanzamientosMovieEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoviesAccion(movies: List<AccionMovieEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoviesRomance(movies: List<RomanceMovieEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoviesFamilia(movies: List<FamiliaMovieEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoviesComedia(movies: List<ComediaMovieEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoviesThriller(movies: List<ThrillerMovieEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoviesHorror(movies: List<HorrorMovieEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoviesCienciaFiccion(movies: List<CienciaFiccionMovieEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeriesPopulares(movies: List<SeriesPopularesEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeriesUltimosLanzamientos(movies: List<UltimosLanzamientosSeriesEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeriesAccionAventura(movies: List<AccionAventuraSerieEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeriesAnimacion(movies: List<AnimacionSerieEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeriesComedia(movies: List<ComediaSerieEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeriesCrimen(movies: List<CrimenSerieEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeriesDrama(movies: List<DramaSerieEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeriesFamilia(movies: List<FamiliaSerieEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeriesKids(movies: List<KidsSerieEntity>)

    // GETS
    @Query("SELECT * FROM peliculas_genero1")
    suspend fun getAllMoviesGenero1(): List<Genero1MovieEntity>

    @Query("SELECT * FROM peliculas_genero2")
    suspend fun getAllMoviesGenero2(): List<Genero2MovieEntity>

    @Query("SELECT * FROM peliculas_proximas")
    suspend fun getAllMoviesProximas(): List<ProximasMovieEntity>

    @Query("SELECT * FROM peliculas_populares")
    suspend fun getAllMoviesPopulares(): List<PeliculasPopularesEntity>

    @Query("SELECT * FROM ultimos_lanzamientos_peliculas")
    suspend fun getAllMoviesUltimosLanzamientos(): List<UltimosLanzamientosMovieEntity>

    @Query("SELECT * FROM peliculas_accion")
    suspend fun getAllMoviesAccion(): List<AccionMovieEntity>

    @Query("SELECT * FROM peliculas_romance")
    suspend fun getAllMoviesRomance(): List<RomanceMovieEntity>

    @Query("SELECT * FROM peliculas_familia")
    suspend fun getAllMoviesFamilia(): List<FamiliaMovieEntity>

    @Query("SELECT * FROM peliculas_comedia")
    suspend fun getAllMoviesComedia(): List<ComediaMovieEntity>

    @Query("SELECT * FROM peliculas_thriller")
    suspend fun getAllMoviesThriller(): List<ThrillerMovieEntity>

    @Query("SELECT * FROM peliculas_horror")
    suspend fun getAllMoviesHorror(): List<HorrorMovieEntity>

    @Query("SELECT * FROM peliculas_cienciaficcion")
    suspend fun getAllMoviesCienciaFiccion(): List<CienciaFiccionMovieEntity>

    @Query("SELECT * FROM series_populares")
    suspend fun getAllSeriesPopulares(): List<SeriesPopularesEntity>

    @Query("SELECT * FROM ultimos_lanzamientos_series")
    suspend fun getAllSeriesUltimosLanzamientos(): List<UltimosLanzamientosSeriesEntity>

    @Query("SELECT * FROM series_accion_aventura")
    suspend fun getAllSeriesAccionAventura(): List<AccionAventuraSerieEntity>

    @Query("SELECT * FROM series_animacion")
    suspend fun getAllSeriesAnimacion(): List<AnimacionSerieEntity>

    @Query("SELECT * FROM series_comedia")
    suspend fun getAllSeriesComedia(): List<ComediaSerieEntity>

    @Query("SELECT * FROM series_crimen")
    suspend fun getAllSeriesCrimen(): List<CrimenSerieEntity>

    @Query("SELECT * FROM series_drama")
    suspend fun getAllSeriesDrama(): List<DramaSerieEntity>

    @Query("SELECT * FROM series_familia")
    suspend fun getAllSeriesFamilia(): List<FamiliaSerieEntity>

    @Query("SELECT * FROM series_kids")
    suspend fun getAllSeriesKids(): List<KidsSerieEntity>


    // DELETES
    @Query("DELETE FROM peliculas_genero1")
    suspend fun deleteAllMoviesGenero1()

    @Query("DELETE FROM peliculas_genero2")
    suspend fun deleteAllMoviesGenero2()

    @Query("DELETE FROM peliculas_proximas")
    suspend fun deleteAllMoviesProximas()

    @Query("DELETE FROM peliculas_populares")
    suspend fun deleteAllMoviesPopulares()

    @Query("DELETE FROM ultimos_lanzamientos_peliculas")
    suspend fun deleteAllMoviesUltimosLanzamientos()

    @Query("DELETE FROM peliculas_accion")
    suspend fun deleteAllMoviesAccion()

    @Query("DELETE FROM peliculas_romance")
    suspend fun deleteAllMoviesRomance()

    @Query("DELETE FROM peliculas_familia")
    suspend fun deleteAllMoviesFamilia()

    @Query("DELETE FROM peliculas_comedia")
    suspend fun deleteAllMoviesComedia()

    @Query("DELETE FROM peliculas_thriller")
    suspend fun deleteAllMoviesThriller()

    @Query("DELETE FROM peliculas_horror")
    suspend fun deleteAllMoviesHorror()

    @Query("DELETE FROM peliculas_cienciaficcion")
    suspend fun deleteAllMoviesCienciaFiccion()

    @Query("DELETE FROM series_populares")
    suspend fun deleteAllSeriesPopulares()

    @Query("DELETE FROM ultimos_lanzamientos_series")
    suspend fun deleteAllSeriesUltimosLanzamientos()

    @Query("DELETE FROM series_accion_aventura")
    suspend fun deleteAllSeriesAccionAventura()

    @Query("DELETE FROM series_animacion")
    suspend fun deleteAllSeriesAnimacion()

    @Query("DELETE FROM series_comedia")
    suspend fun deleteAllSeriesComedia()

    @Query("DELETE FROM series_crimen")
    suspend fun deleteAllSeriesCrimen()

    @Query("DELETE FROM series_drama")
    suspend fun deleteAllSeriesDrama()

    @Query("DELETE FROM series_familia")
    suspend fun deleteAllSeriesFamilia()

    @Query("DELETE FROM series_kids")
    suspend fun deleteAllSeriesKids()
}
