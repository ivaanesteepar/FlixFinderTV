package com.example.flixfindertv.room.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.flixfindertv.local.Converters
import com.example.flixfindertv.room.entities.*
import com.example.flixfindertv.room.dao.MovieDao

// Base de datos Room que define las entidades y el DAO para las películas y series
@Database(
    entities = [
        Genero1MovieEntity::class,
        Genero2MovieEntity::class,
        ProximasMovieEntity::class,
        FavoritoEntity::class,
        PeliculasEntity::class
    ],
    version = 27
)

@TypeConverters(Converters::class)
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
