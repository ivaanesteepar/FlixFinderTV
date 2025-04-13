package com.example.flixfindertv.ui.viewmodels

import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flixfindertv.models.Peliculas
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class UsersViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance() // Obtén la instancia de FirebaseAuth para obtener el usuario actual

    // Estado para almacenar si la película o serie está en favoritos
    private val _isFavorite = mutableStateOf(false)
    val isFavorite: State<Boolean> get() = _isFavorite

    // Estado para almacenar el UID del usuario encontrado
    private val _userIdComment = MutableLiveData<String>()
    val userIdComment: LiveData<String> get() = _userIdComment

    // Guardar sesión con el UID del usuario
    fun saveSession(context: Context, isLoggedIn: Boolean, uid: String?) {
        val sharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Verifica si el UID es válido antes de guardar
        if (uid != null) {
            editor.putBoolean("is_logged_in_$uid", isLoggedIn)  // Guardamos el estado de logueo para ese UID específico
        }
        editor.apply()
    }

    // Verificar si un usuario específico está logueado usando su UID
    fun isUserLoggedIn(context: Context, uid: String?): Boolean {
        val sharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

        // Verificar si el UID es válido
        if (uid != null) {
            // Obtenemos el estado de logueo del usuario usando su UID
            return sharedPreferences.getBoolean("is_logged_in_$uid", false)  // Devuelve 'false' si no está logueado
        }
        return false
    }


    fun updateFavoriteGenre(movieGenre: String) {
        val firstGenre = movieGenre.split(",").first().trim()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userDocRef = FirebaseFirestore.getInstance().collection("usuarios").document(userId)
        val maxGeneros = 2

        userDocRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val generosFavoritos = document.get("generosFavoritos") as? Map<String, Number> ?: emptyMap()
                val nuevosGeneros = generosFavoritos.toMutableMap()

                if (nuevosGeneros.containsKey(firstGenre)) {
                    // El género ya existe, actualizamos el timestamp
                    nuevosGeneros[firstGenre] = System.currentTimeMillis()
                } else {
                    if (nuevosGeneros.size < maxGeneros) {
                        // Hay espacio, lo agregamos
                        nuevosGeneros[firstGenre] = System.currentTimeMillis()
                    } else {
                        // Reemplazamos el más antiguo
                        val generoMasAntiguo = nuevosGeneros.minByOrNull { it.value.toLong() }?.key
                        if (generoMasAntiguo != null) {
                            nuevosGeneros.remove(generoMasAntiguo)
                            nuevosGeneros[firstGenre] = System.currentTimeMillis()
                        }
                    }
                }

                userDocRef.update("generosFavoritos", nuevosGeneros)
            }
        }.addOnFailureListener { e ->
            Log.e("UpdateFavoriteGenre", "Error al obtener el documento del usuario", e)
        }
    }


    suspend fun getFollowersUsers(uid: String): List<Pair<String, String>>? {
        return try {
            val document = FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(uid)
                .get()
                .await()

            val followersIds = document.get("seguidores") as? List<String> ?: emptyList()

            followersIds.mapNotNull { followerId ->
                val userDoc = FirebaseFirestore.getInstance()
                    .collection("usuarios")
                    .document(followerId)
                    .get()
                    .await()

                val name = userDoc.getString("nombre")
                name?.let { followerId to it }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getFollowingUsers(uid: String): List<Pair<String, String>>? {
        return try {
            val document = FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(uid)
                .get()
                .await()

            val followingIds = document.get("siguiendo") as? List<String> ?: emptyList()

            followingIds.mapNotNull { followingId ->
                val userDoc = FirebaseFirestore.getInstance()
                    .collection("usuarios")
                    .document(followingId)
                    .get()
                    .await()

                val name = userDoc.getString("nombre")
                name?.let { followingId to it }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun followUser(currentUid: String, targetUid: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        if (currentUid != targetUid) {
            viewModelScope.launch {
                try {
                    val userRef = firestore.collection("usuarios").document(targetUid)
                    val currentUserRef = firestore.collection("usuarios").document(currentUid)

                    val targetDoc = userRef.get().await()
                    val currentUserDoc = currentUserRef.get().await()

                    if (targetDoc.exists() && currentUserDoc.exists()) {
                        val seguidores = targetDoc.get("seguidores") as? List<String> ?: emptyList()
                        val siguiendo = currentUserDoc.get("siguiendo") as? List<String> ?: emptyList()

                        // Agregar el targetUid al campo "siguiendo" del usuario actual
                        if (!siguiendo.contains(targetUid)) {
                            val updatedSiguiendo = siguiendo + targetUid
                            currentUserRef.update("siguiendo", updatedSiguiendo).await()
                        }

                        // Agregar el currentUid al campo "seguidores" del usuario objetivo
                        if (!seguidores.contains(currentUid)) {
                            val updatedSeguidores = seguidores + currentUid
                            userRef.update("seguidores", updatedSeguidores).await()
                        }

                        onSuccess() // Notifica éxito
                    }
                } catch (e: Exception) {
                    onFailure(e) // Notifica error
                }
            }
        }
    }


    fun unfollowUser(currentUid: String, targetUid: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        if (currentUid != targetUid) {
            viewModelScope.launch {
                try {
                    val userRef = firestore.collection("usuarios").document(targetUid)
                    val document = userRef.get().await()
                    if (document.exists()) {
                        val seguidores = document.get("seguidores") as? List<String> ?: emptyList()
                        if (seguidores.contains(currentUid)) {
                            val updatedSeguidores = seguidores - currentUid
                            userRef.update("seguidores", updatedSeguidores).await()
                            onSuccess() // Notifica éxito
                        }
                    }
                } catch (e: Exception) {
                    onFailure(e) // Notifica error
                }
            }
        }
    }

    fun checkIfFollowing(currentUid: String, targetUid: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val userRef = firestore.collection("usuarios").document(targetUid)
                val document = userRef.get().await()
                if (document.exists()) {
                    val seguidores = document.get("seguidores") as? List<String> ?: emptyList()
                    callback(seguidores.contains(currentUid)) // Devuelve si el usuario está siguiendo
                } else {
                    callback(false)
                }
            } catch (e: Exception) {
                callback(false)
            }
        }
    }


    fun fetchUserId(nombreUsuario: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("usuarios")
            .whereEqualTo("nombre", nombreUsuario)
            .limit(1)  // Solo necesitamos un resultado
            .get()
            .addOnSuccessListener { documents ->
                val userId = documents.documents.firstOrNull()?.getString("uid") ?: "ID_DESCONOCIDO"
                println("prueba4")

                if (!userId.isNullOrEmpty()) {  // Asegurar que no es nulo ni vacío
                    _userIdComment.value = userId
                    println("el id que se ha seleccionado es: ${_userIdComment.value}")
                    println("entonces el id es: ${userIdComment.value}")
                } else {
                    println("El campo 'id' no está presente o es nulo.")
                }
            }
            .addOnFailureListener {
                println("Error al obtener el usuario")
            }
    }


    suspend fun getUserAdminStatus(userId: String, callback: (Boolean) -> Unit) {
        // Obtener una referencia a la colección de usuarios
        val db = FirebaseFirestore.getInstance()

        // Referencia al documento del usuario por ID
        val userDocument = db.collection("usuarios").document(userId)

        try {
            // Obtener los datos del documento del usuario
            val documentSnapshot = userDocument.get().await()

            // Verificar si el documento existe y contiene el campo 'admin'
            if (documentSnapshot.exists() && documentSnapshot.contains("admin")) {
                // Obtener el valor del campo 'admin'
                val isAdmin = documentSnapshot.getBoolean("admin") ?: false
                // Pasar el valor de 'admin' al callback
                callback(isAdmin)
            } else {
                // Si no existe el campo 'admin', asumir que el usuario no es admin
                callback(false)
            }
        } catch (e: Exception) {
            // Manejar errores, por ejemplo, si hay un problema con la conexión
            callback(false)
        }
    }



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
    fun getFavoriteMovies(
        uid: String,
        onSuccess: (List<Peliculas>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val favoritesCollection = firestore.collection("usuarios").document(uid)

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
    }



    // Función para obtener las series favoritas
    fun getFavoriteSeries(
        uid: String,
        onSuccess: (List<Peliculas>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val favoritesCollection = firestore.collection("usuarios").document(uid)

        // Obtener las series favoritas del usuario
        favoritesCollection.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val favoriteItems = document.get("seriesFavoritas") as? List<Map<String, Any>> ?: emptyList()

                // Mapear los favoritos a objetos de tipo Peliculas
                val seriesFavoritas = favoriteItems.mapNotNull { movieData ->
                    Peliculas(
                        id = movieData["id"] as? String ?: "",
                        title = movieData["title"] as? String ?: "",
                        poster_path = movieData["posterUrl"] as? String ?: "",
                        esSerie = movieData["esSerie"] as? Boolean ?: true
                    )
                }

                onSuccess(seriesFavoritas)
            } else {
                onSuccess(emptyList()) // Si no existen favoritos, devolvemos una lista vacía
            }
        }.addOnFailureListener { exception ->
            onFailure(exception)
        }
    }

}
