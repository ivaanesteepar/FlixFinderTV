package com.example.flixfindertv.ui.viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.flixfindertv.models.Peliculas
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UsersViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance() // Obtén la instancia de FirebaseAuth para obtener el usuario actual

    // Estado para almacenar si la película o serie está en favoritos
    private val _isFavorite = mutableStateOf(false)
    val isFavorite: State<Boolean> get() = _isFavorite

    // Función para verificar si una película o serie está en favoritos
    fun checkIfFavorite(id: String, isSerie: Boolean) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val favoritesCollection = firestore.collection("usuarios").document(userId)

            // Verificar si está en la lista de favoritos
            favoritesCollection.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    // Determinar el campo según si es serie o película
                    val fieldToCheck = if (isSerie) "seriesFavoritas" else "peliculasFavoritas"

                    // Obtener las listas de favoritos del documento como una lista de mapas
                    val favoriteList = document.get(fieldToCheck) as? List<Map<String, Any>> ?: emptyList()

                    // Comprobar si la película o serie está en los favoritos (buscando por id)
                    _isFavorite.value = favoriteList.any { it["id"] == id }
                } else {
                    _isFavorite.value = false
                }
            }.addOnFailureListener {
                _isFavorite.value = false
            }
        } else {
            _isFavorite.value = false
        }
    }


    fun saveToFavorites(id: String, title: String, posterUrl: String, isSerie: Boolean) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val favoritesCollection = firestore.collection("usuarios").document(userId)

            // Definir el campo a actualizar según si es serie o película
            val fieldToUpdate = if (isSerie) "seriesFavoritas" else "peliculasFavoritas"

            // Crear los datos de la película o serie en formato Map
            val movieData = mapOf(
                "id" to id,
                "title" to title,
                "posterUrl" to posterUrl,
                "esSerie" to isSerie
            )

            favoritesCollection.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    // Obtener la lista actual de favoritos (convertimos a lista de mapas)
                    val existingFavorites = document.get(fieldToUpdate) as? List<Map<String, Any>> ?: emptyList()

                    // Verificar si ya existe en la lista para evitar duplicados
                    if (existingFavorites.none { it["id"] == id }) {
                        // Crear nueva lista con el favorito añadido
                        val updatedFavorites = existingFavorites + movieData

                        // Actualizar la lista en Firestore
                        favoritesCollection.update(fieldToUpdate, updatedFavorites).addOnSuccessListener {
                            _isFavorite.value = true
                        }
                    }
                } else {
                    // Si no existen favoritos, crear una nueva lista con la película o serie
                    favoritesCollection.set(mapOf(fieldToUpdate to listOf(movieData))).addOnSuccessListener {
                        _isFavorite.value = true
                    }
                }
            }
        }
    }


    // Función para eliminar una película o serie de los favoritos
    fun removeFromFavorites(id: String, isSerie: Boolean) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val favoritesCollection = firestore.collection("usuarios").document(userId)

            // Definir el campo a actualizar según si es serie o película
            val fieldToUpdate = if (isSerie) "seriesFavoritas" else "peliculasFavoritas"

            // Realizamos la operación de eliminar la película de favoritos en la base de datos
            favoritesCollection.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val existingFavorites = document.get(fieldToUpdate) as? List<Map<String, Any>> ?: emptyList()
                    val updatedFavorites = existingFavorites.filterNot { it["id"] == id }

                    // Actualizar el campo de favoritos en la base de datos
                    favoritesCollection.update(fieldToUpdate, updatedFavorites).addOnSuccessListener {
                        // Después de que la operación de base de datos haya sido exitosa, actualizamos el estado
                        _isFavorite.value = false
                    }
                }
            }
        }
    }

    // Función para obtener las películas favoritas
    fun getFavoriteMovies(onSuccess: (List<Peliculas>) -> Unit, onFailure: (Exception) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val favoritesCollection = firestore.collection("usuarios").document(userId)

            // Obtener las películas favoritas del usuario
            favoritesCollection.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val favoriteItems = document.get("peliculasFavoritas") as? List<Map<String, Any>> ?: emptyList()

                    // Mapear los favoritos a objetos de tipo Peliculas
                    val peliculasFavoritas = favoriteItems.mapNotNull { movieData ->
                        Peliculas(
                            id = movieData["id"] as? String ?: "",
                            title = movieData["title"] as? String ?: "",
                            poster_path = movieData["posterUrl"] as? String ?: "",
                            esSerie = movieData["esSerie"] as? Boolean ?: false
                        )
                    }

                    onSuccess(peliculasFavoritas)
                } else {
                    onSuccess(emptyList()) // Si no existen favoritos, devolvemos una lista vacía
                }
            }.addOnFailureListener { exception ->
                onFailure(exception)
            }
        } else {
            onFailure(Exception("Usuario no autenticado"))
        }
    }


    // Función para obtener las series favoritas
    fun getFavoriteSeries(onSuccess: (List<Peliculas>) -> Unit, onFailure: (Exception) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val favoritesCollection = firestore.collection("usuarios").document(userId)

            // Obtener las series favoritas del usuario
            favoritesCollection.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val favoriteItems = document.get("seriesFavoritas") as? List<Map<String, Any>> ?: emptyList()

                    // Mapear los favoritos a objetos de tipo Peliculas
                    val seriesFavoritas = favoriteItems.mapNotNull { movieData ->
                        movieData.let {
                            Peliculas(
                                id = movieData["id"] as? String ?: "",
                                title = movieData["title"] as? String ?: "",
                                poster_path = movieData["posterUrl"] as? String ?: "",
                                esSerie = movieData["esSerie"] as? Boolean ?: true

                            )
                        }
                    }

                    onSuccess(seriesFavoritas)
                } else {
                    onSuccess(emptyList()) // Si no existen favoritos, devolvemos una lista vacía
                }
            }.addOnFailureListener { exception ->
                onFailure(exception)
            }
        } else {
            onFailure(Exception("Usuario no autenticado"))
        }
    }

}
