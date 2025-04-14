package com.example.flixfindertv.room.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.flixfindertv.room.entities.Genero1MovieEntity
import com.example.flixfindertv.room.entities.Genero2MovieEntity
import com.example.flixfindertv.room.entities.ProximasMovieEntity
import com.example.flixfindertv.room.dao.MovieDao

@Database(
    entities = [Genero1MovieEntity::class, Genero2MovieEntity::class, ProximasMovieEntity::class],
    version = 2
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
