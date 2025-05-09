package com.example.flixfindertv.local

import androidx.room.TypeConverter

class Converters {

    @TypeConverter
    fun fromListIntToString(value: List<Int>): String {
        return value.joinToString(",")
    }

    @TypeConverter
    fun fromStringToListInt(value: String): List<Int> {
        return if (value.isBlank()) emptyList() else value.split(",").map { it.toInt() }
    }

    @TypeConverter
    fun fromListStringToString(value: List<String>): String {
        return value.joinToString("|||")
    }

    @TypeConverter
    fun fromStringToListString(value: String): List<String> {
        return if (value.isBlank()) emptyList() else value.split("|||")
    }
}