package com.example.flixfindertv.models

import com.google.gson.annotations.SerializedName

data class Generos (
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String
){
    constructor() : this(0, "")
}

data class GenreResponse(
    @SerializedName("genres")
    val genres: List<Generos>
)