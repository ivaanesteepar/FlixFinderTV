package com.example.flixfindertv.ui.viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
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
                    val fieldToCheck = if (isSerie) "seriesFavoritas" else "peliculasFavoritas"
                    val favoriteList = document.get(fieldToCheck) as? List<Map<String, Any>> ?: emptyList()
                    _isFavorite.value = favoriteList.any { it["id"] == id }
                } else {
                    _isFavorite.value = false
                }
            }
        }
    }

    // Función para guardar una película o serie en los favoritos
    fun saveToFavorites(id: String, title: String, description: String, posterUrl: String, isSerie: Boolean) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val favoritesCollection = firestore.collection("usuarios").document(userId)

            // Definir el campo a actualizar según si es serie o película
            val fieldToUpdate = if (isSerie) "seriesFavoritas" else "peliculasFavoritas"
            val movieData = mapOf(
                "id" to id,
                "title" to title,
                "posterUrl" to posterUrl,
            )

            // Realizamos la operación de agregar la película a favoritos en la base de datos
            favoritesCollection.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val existingFavorites = document.get(fieldToUpdate) as? List<Map<String, Any>> ?: emptyList()
                    val updatedFavorites = existingFavorites.toMutableList().apply {
                        add(movieData) // Agregar la película o serie a la lista de favoritos
                    }

                    // Actualizar el campo de favoritos en la base de datos
                    favoritesCollection.update(fieldToUpdate, updatedFavorites).addOnSuccessListener {
                        // Después de que la operación de base de datos haya sido exitosa, actualizamos el estado
                        _isFavorite.value = true
                    }
                } else {
                    // Si no existen favoritos, crear una nueva lista
                    val favorites = listOf(movieData)
                    favoritesCollection.set(mapOf(fieldToUpdate to favorites)).addOnSuccessListener {
                        // Después de que la operación de base de datos haya sido exitosa, actualizamos el estado
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
}
