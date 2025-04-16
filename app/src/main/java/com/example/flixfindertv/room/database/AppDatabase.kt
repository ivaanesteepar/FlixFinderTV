package com.example.flixfindertv.room.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.flixfindertv.room.entities.*
import com.example.flixfindertv.room.dao.MovieDao

@Database(
    entities = [
        Genero1MovieEntity::class,
        Genero2MovieEntity::class,
        ProximasMovieEntity::class,
        PeliculasPopularesEntity::class,
        UltimosLanzamientosMovieEntity::class,
        AccionMovieEntity::class,
        RomanceMovieEntity::class,
        FamiliaMovieEntity::class,
        ComediaMovieEntity::class,
        ThrillerMovieEntity::class,
        HorrorMovieEntity::class,
        CienciaFiccionMovieEntity::class,
        SeriesPopularesEntity::class,
        UltimosLanzamientosSeriesEntity::class,
        AccionAventuraSerieEntity::class,
        AnimacionSerieEntity::class,
        ComediaSerieEntity::class,
        CrimenSerieEntity::class,
        DramaSerieEntity::class,
        FamiliaSerieEntity::class,
        KidsSerieEntity::class
    ],
    version = 3
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun movieDao(): MovieDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "flixfinder_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
