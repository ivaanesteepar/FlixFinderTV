package com.ivaanesteepar.flixfindertv.models

// Clase que representa un género con su identificador y nombre
data class Generos (
    val id: Int,
    val name: String
){
    constructor() : this(0, "")
}
