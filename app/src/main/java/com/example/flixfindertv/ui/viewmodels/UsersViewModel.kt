package com.example.flixfindertv.ui.viewmodels

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.flixfindertv.models.Peliculas
import com.example.flixfindertv.models.Usuarios
import com.example.flixfindertv.room.database.AppDatabase
import com.example.flixfindertv.room.entities.FavoritoEntity
import com.example.flixfindertv.room.repository.MovieRepository
import com.example.flixfindertv.utils.ImgurUploader
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UsersViewModel(application: Application) : AndroidViewModel(application) {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Estado para almacenar si la película o serie está en favoritos
    private val _isFavorite = mutableStateOf(false)
    val isFavorite: State<Boolean> get() = _isFavorite

    // Estado para almacenar el UID del usuario encontrado
    private val _userIdComment = MutableLiveData<String>()
    val userIdComment: LiveData<String> get() = _userIdComment

    private val _userNameComment = MutableLiveData<String>()
    val userNameComment: LiveData<String> get() = _userNameComment

    private val movieDao = AppDatabase.getDatabase(application).movieDao()
    private val movieRepository = MovieRepository(movieDao)

    val favouriteMovies = mutableStateOf<List<Peliculas>>(emptyList())
    val favouriteSeries = mutableStateOf<List<Peliculas>>(emptyList())

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private val _usuarioState = MutableStateFlow<Usuarios?>(null)
    val usuarioState: StateFlow<Usuarios?> = _usuarioState

    private var listenerRegistration: ListenerRegistration? = null

    val userId = FirebaseAuth.getInstance().currentUser?.uid


    fun cargarFavoritasDesdeFirestore(
        userId: String,
        repository: MovieRepository
    ) {
        val db = FirebaseFirestore.getInstance()
        val userDocRef = db.collection("usuarios").document(userId)

        userDocRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val peliculasFavoritasList = document.get("peliculasFavoritas") as? List<Map<String, Any>>
                    val seriesFavoritasList = document.get("seriesFavoritas") as? List<Map<String, Any>>

                    println("las peliculas favoritas son: $peliculasFavoritasList")
                    println("las series favoritas son: $seriesFavoritasList")

                    val favoritas = mutableListOf<Peliculas>()

                    if (peliculasFavoritasList != null) {
                        favoritas += peliculasFavoritasList.mapNotNull { mapa ->
                            try {
                                mapToPeliculas(mapa)
                            } catch (e: Exception) {
                                Log.e("Firestore", "Error al mapear película favorita", e)
                                null
                            }
                        }
                    } else {
                        Log.d("Firestore", "Campo peliculasFavoritas vacío o nulo.")
                    }

                    if (seriesFavoritasList != null) {
                        favoritas += seriesFavoritasList.mapNotNull { mapa ->
                            try {
                                mapToPeliculas(mapa)
                            } catch (e: Exception) {
                                Log.e("Firestore", "Error al mapear serie favorita", e)
                                null
                            }
                        }
                    } else {
                        Log.d("Firestore", "Campo seriesFavoritas vacío o nulo.")
                    }

                    if (favoritas.isNotEmpty()) {
                        val entidades = favoritas.map { pelicula ->
                            FavoritoEntity(
                                idMovieEntity = pelicula.id,
                                pelicula = pelicula,
                                userId = userId
                            )
                        }

                        CoroutineScope(Dispatchers.IO).launch {
                            repository.insertFavoritos(entidades)
                        }
                    }

                } else {
                    Log.d("Firestore", "No se encontró el documento del usuario.")
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al obtener las favoritas desde Firestore", e)
            }
    }

    fun mapToPeliculas(mapa: Map<String, Any>): Peliculas {
        return Peliculas(
            id = mapa["id"] as? String ?: "",
            title = mapa["title"] as? String,
            name = mapa["name"] as? String,
            overview = mapa["overview"] as? String ?: "",
            release_date = mapa["release_date"] as? String,
            release_date_series = mapa["release_date_series"] as? String,
            poster_path = mapa["poster_path"] as? String,
            vote_average = (mapa["vote_average"]?.toString()) ?: "0.0",
            vote_count = (mapa["vote_count"]?.toString()) ?: "0",
            genre_ids = (mapa["genre_ids"] as? List<*>)?.mapNotNull { (it as? Number)?.toInt() } ?: emptyList(),
            adult = mapa["adult"] as? Boolean ?: false,
            backdrop_path = mapa["backdrop_path"] as? String,
            popularity = (mapa["popularity"] as? Number)?.toDouble() ?: 0.0,
            esSerie = mapa["esSerie"] as? Boolean ?: false,
            comentarios = (mapa["comentarios"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
            original_language = mapa["original_language"] as? String ?: "",
            status = mapa["status"] as? String ?: "",
            trailer = mapa["trailer"] as? String,
            director_name = mapa["director_name"] as? String ?: "",
            director_photo_url = mapa["director_photo_url"] as? String ?: ""
        )
    }


    fun startListening(uid: String) {
        listenerRegistration?.remove() // Quitar listener anterior si existía

        listenerRegistration = firestore.collection("usuarios")
            .document(uid)
            .addSnapshotListener { documentSnapshot, exception ->
                if (exception != null) {
                    // Aquí podrías exponer un estado de error si quieres
                    return@addSnapshotListener
                }
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    val usuario = documentSnapshot.toObject(Usuarios::class.java)
                    _usuarioState.value = usuario
                }
            }
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }

    fun actualizarPerfil(
        uid: String,
        userName: String,
        passwordActual: String,
        passwordNueva: String,
        profileImageUri: String?,
        deleteImageInUI: Boolean,
        hayConexion: Boolean,
        context: Context,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        // Validaciones locales
        if (passwordActual.isNotEmpty() && passwordNueva.isEmpty()) {
            errorMessage = "You must enter the new password"
            return
        } else {
            errorMessage = null
        }

        if (passwordActual.isNotEmpty() && passwordNueva.isNotEmpty() && passwordActual == passwordNueva) {
            errorMessage = "The new password cannot be the same as the current password"
            return
        }

        if (!hayConexion) {
            Toast.makeText(context, "You need an internet connection to update your profile", Toast.LENGTH_SHORT).show()
            return
        }

        // Comprobar nombre de usuario disponible (excluyendo el usuario actual)
        firestore.collection("usuarios")
            .whereEqualTo("nombre", userName)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val nombreYaUsado = querySnapshot.documents.any { it.id != uid }
                if (nombreYaUsado) {
                    errorMessage = "That username is already taken"
                } else {
                    errorMessage = null
                    val userUpdates = mutableMapOf<String, Any?>("nombre" to userName)

                    fun actualizarFirestoreYTerminar() {
                        firestore.collection("usuarios").document(uid)
                            .update(userUpdates)
                            .addOnSuccessListener {
                                if (passwordActual.isNotEmpty()) {
                                    changePassword(passwordActual, passwordNueva) { success, mensaje ->
                                        if (success) {
                                            errorMessage = null
                                            Toast.makeText(context, "Profile updated and password changed", Toast.LENGTH_SHORT).show()
                                            onSuccess()
                                        } else {
                                            errorMessage = mensaje
                                            onFailure(mensaje)
                                        }
                                    }
                                } else {
                                    Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
                                    onSuccess()
                                }
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Error updating profile", Toast.LENGTH_SHORT).show()
                                onFailure("Error updating profile")
                            }
                    }

                    if (deleteImageInUI) {
                        userUpdates["fotoPerfil"] = null
                        actualizarFirestoreYTerminar()
                    } else if (!profileImageUri.isNullOrEmpty()) {
                        val imageUri = profileImageUri
                        val isRemoteUrl = imageUri?.startsWith("http")

                        if (!isRemoteUrl!!) {
                            try {
                                val inputStream = context.contentResolver.openInputStream(android.net.Uri.parse(imageUri))
                                val imageBytes = inputStream?.readBytes()

                                if (imageBytes != null) {
                                    ImgurUploader.uploadImage(imageBytes) { imageUrl ->
                                        if (imageUrl != null) {
                                            userUpdates["fotoPerfil"] = imageUrl
                                            actualizarFirestoreYTerminar()
                                        } else {
                                            Toast.makeText(context, "Error uploading the image", Toast.LENGTH_SHORT).show()
                                            onFailure("Error uploading the image")
                                        }
                                    }
                                    return@addOnSuccessListener
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error processing the image", Toast.LENGTH_SHORT).show()
                                onFailure("Error processing the image")
                            }
                        } else {
                            userUpdates["fotoPerfil"] = imageUri
                            actualizarFirestoreYTerminar()
                        }
                    } else {
                        actualizarFirestoreYTerminar()
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error checking username availability", Toast.LENGTH_SHORT).show()
                onFailure("Error checking username availability")
            }
    }


    private fun changePassword(
        passwordActual: String,
        passwordNueva: String,
        callback: (success: Boolean, mensaje: String) -> Unit
    ) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            callback(false, "User not authenticated")
            return
        }

        val email = user.email
        if (email.isNullOrEmpty()) {
            callback(false, "User email not found")
            return
        }

        val credential = EmailAuthProvider.getCredential(email, passwordActual)

        // Reautenticar
        user.reauthenticate(credential)
            .addOnCompleteListener { authTask ->
                if (authTask.isSuccessful) {
                    // Contraseña actual correcta, permite un cambio de contraseña
                    user.updatePassword(passwordNueva)
                        .addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                callback(true, "Password changed successfully")
                            } else {
                                callback(false, "The new password must be at least 6 characters long")
                            }
                        }
                } else {
                    callback(false, "Current password is incorrect")
                }
            }
    }


    fun register(email: String, password: String, confirmPassword: String, username: String, onSuccess: (String) -> Unit, onFailure: (String) -> Unit) {
        // Verificar si los campos están vacíos
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || username.isEmpty()) {
            onFailure("All fields are required")
            return
        }

        // Verificar si las contraseñas coinciden
        if (password != confirmPassword) {
            onFailure("Passwords do not match")
            return
        }

        // Verificar si el nombre de usuario ya existe
        firestore.collection("usuarios")
            .whereEqualTo("nombre", username)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    // El nombre de usuario no existe, continuar con el registro
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = auth.currentUser
                                user?.let {
                                    // Crear nuevo usuario en Firestore
                                    val newUser = Usuarios(
                                        uid = user.uid,
                                        nombre = username,
                                        email = email,
                                        fotoPerfil = "",
                                        peliculasFavoritas = emptyList(),
                                        seriesFavoritas = emptyList(),
                                        seguidores = emptyList(),
                                        siguiendo = emptyList(),
                                        numComentarios = 0,
                                        admin = false
                                    )

                                    firestore.collection("usuarios")
                                        .document(user.uid)
                                        .set(newUser)
                                        .addOnSuccessListener {
                                            onSuccess("login")
                                        }
                                        .addOnFailureListener { exception ->
                                            onFailure("Error adding user to Firestore: ${exception.message}")
                                        }
                                }
                            } else {
                                onFailure("Error registering user: ${task.exception?.message}")
                            }
                        }
                } else {
                    // El nombre de usuario ya está en uso
                    onFailure("Username already taken")
                }
            }
            .addOnFailureListener { exception ->
                onFailure("Error checking username availability: ${exception.message}")
            }
    }

    fun login(email: String, password: String, onSuccess: (String) -> Unit, onFailure: (String) -> Unit) {
        if (email.isEmpty() || password.isEmpty()) {
            onFailure("All fields are required")
            return
        }

        // Iniciar el proceso de inicio de sesión
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        val userId = user.uid
                        val userRef = firestore.collection("usuarios").document(userId)

                        userRef.get()
                            .addOnSuccessListener { document ->
                                if (document.exists()) {
                                    val esNuevo = document.getBoolean("esNuevo") ?: false
                                    if (esNuevo) {
                                        onSuccess("questions")
                                    } else {
                                        onSuccess("home")
                                    }
                                } else {
                                    onFailure("User data not found")
                                }
                            }
                            .addOnFailureListener {
                                onFailure("Error fetching user data")
                            }
                    }
                } else {
                    onFailure("Oops! Something went wrong. Please check your credentials and try again.")
                }
            }
    }

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
        println("movie genres que recibe el update: $movieGenre")
        val firstGenre = movieGenre.split(",").first().trim()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userDocRef = FirebaseFirestore.getInstance().collection("usuarios").document(userId)
        val maxGeneros = 2

        FirebaseFirestore.getInstance().runTransaction { transaction ->
            val snapshot = transaction.get(userDocRef)

            val generosFavoritos = snapshot.get("generosFavoritos") as? Map<String, Number> ?: emptyMap()
            val nuevosGeneros = generosFavoritos.toMutableMap()

            if (nuevosGeneros.containsKey(firstGenre)) {
                // Ya existe, actualizamos timestamp
                nuevosGeneros[firstGenre] = System.currentTimeMillis()
                println("Género ya existe, se actualiza timestamp: $firstGenre")
            } else {
                if (nuevosGeneros.size < maxGeneros) {
                    // Hay espacio, agregamos nuevo género
                    nuevosGeneros[firstGenre] = System.currentTimeMillis()
                    println("Género agregado: $firstGenre")
                } else {
                    // Reemplazamos el más antiguo
                    val generoMasAntiguo = nuevosGeneros.minByOrNull { (_, timestamp) -> timestamp.toLong() }?.key
                    if (generoMasAntiguo != null) {
                        nuevosGeneros.remove(generoMasAntiguo)
                        nuevosGeneros[firstGenre] = System.currentTimeMillis()
                        println("Género reemplazado: $generoMasAntiguo por $firstGenre")
                    }
                }
            }

            // Actualizamos el documento dentro de la transacción
            transaction.update(userDocRef, "generosFavoritos", nuevosGeneros)
            println("Géneros favoritos actualizados dentro de la transacción: $nuevosGeneros")

            // El bloque debe devolver algo, pero no nos interesa aquí.
            null
        }.addOnSuccessListener {
            println("Transacción completada con éxito")
        }.addOnFailureListener { e ->
            Log.e("UpdateFavoriteGenre", "Error en la transacción de actualización", e)
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

    fun unfollowUser(
        currentUid: String,
        targetUid: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if (currentUid == targetUid) return

        viewModelScope.launch {
            try {
                val userRefTarget = firestore.collection("usuarios").document(targetUid)
                val userRefCurrent = firestore.collection("usuarios").document(currentUid)

                // Obtener documentos
                val targetDoc = userRefTarget.get().await()
                val currentDoc = userRefCurrent.get().await()

                if (targetDoc.exists() && currentDoc.exists()) {
                    val seguidores = targetDoc.get("seguidores") as? List<String> ?: emptyList()
                    val siguiendo = currentDoc.get("siguiendo") as? List<String> ?: emptyList()

                    val updatedSeguidores = seguidores - currentUid
                    val updatedSiguiendo = siguiendo - targetUid

                    // Actualizar ambos documentos en paralelo
                    val updateTarget = userRefTarget.update("seguidores", updatedSeguidores)
                    val updateCurrent = userRefCurrent.update("siguiendo", updatedSiguiendo)

                    // Esperar ambos updates
                    updateTarget.await()
                    updateCurrent.await()

                    onSuccess()
                } else {
                    onFailure(Exception("User documents not found"))
                }
            } catch (e: Exception) {
                onFailure(e)
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

                if (!userId.isNullOrEmpty()) {  // Asegurar que no es nulo ni vacío
                    _userIdComment.value = userId
                } else {
                    println("El campo 'id' no está presente o es nulo.")
                }
            }
            .addOnFailureListener {
                println("Error al obtener el usuario")
            }
    }

    fun fetchUserName(userId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("usuarios")
            .whereEqualTo("uid", userId)
            .limit(1)  // Solo necesitamos un resultado
            .get()
            .addOnSuccessListener { documents ->
                val userName = documents.documents.firstOrNull()?.getString("nombre") ?: "Nombre Desconocido"

                if (!userName.isNullOrEmpty()) {  // Asegurar que no es nulo ni vacío
                    _userNameComment.value = userName
                } else {
                    println("El campo 'nombre' no está presente o es nulo.")
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

    fun saveToFavorites(context: Context, pelicula: Peliculas) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val favoritesCollection = firestore.collection("usuarios").document(userId)

            val fieldToUpdate = if (pelicula.esSerie) "seriesFavoritas" else "peliculasFavoritas"

            val movieData = mapOf(
                "id" to pelicula.id,
                "title" to pelicula.title,
                "name" to pelicula.name,
                "overview" to pelicula.overview,
                "poster_path" to pelicula.poster_path,
                "vote_average" to pelicula.vote_average,
                "vote_count" to pelicula.vote_count,
                "genre_ids" to pelicula.genre_ids,
                "adult" to pelicula.adult,
                "release_date" to pelicula.release_date,
                "release_Date_series" to pelicula.release_date_series,
                "backdrop_path" to pelicula.backdrop_path,
                "popularity" to pelicula.popularity,
                "esSerie" to pelicula.esSerie,
                "comentarios" to pelicula.comentarios,
                "original_language" to pelicula.original_language,
                "status" to pelicula.status,
                "trailer" to pelicula.trailer,
                "director_name" to pelicula.director_name,
                "director_photo_url" to pelicula.director_photo_url
            )

            favoritesCollection.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val existingFavorites = document.get(fieldToUpdate) as? List<Map<String, Any>> ?: emptyList()

                    if (existingFavorites.any { it["id"] == pelicula.id }) return@addOnSuccessListener

                    if (existingFavorites.size >= 20) {
                        Toast.makeText(
                            context,
                            "You can only save up to 20 favorite ${if (pelicula.esSerie) "TV shows" else "movies"}.",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@addOnSuccessListener
                    }

                    val updatedFavorites = existingFavorites + movieData

                    favoritesCollection.update(fieldToUpdate, updatedFavorites).addOnSuccessListener {
                        _isFavorite.value = true
                    }
                } else {
                    favoritesCollection.set(mapOf(fieldToUpdate to listOf(movieData))).addOnSuccessListener {
                        _isFavorite.value = true
                    }
                }
            }
        }
    }


    fun saveToLocalFavorites(context: Context, pelicula: Peliculas, userId: String?) {
        if (userId == null) return  // Si no hay usuario, no se guarda nada

        CoroutineScope(Dispatchers.IO).launch {
            val currentFavorites = if (pelicula.esSerie) {
                movieRepository.getSeriesFavoritas(userId)
            } else {
                movieRepository.getPeliculasFavoritas(userId)
            }

            if (currentFavorites.any { it.pelicula.id == pelicula.id }) return@launch

            if (currentFavorites.size >= 20) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "You can only save up to 20 ${if (pelicula.esSerie) "series" else "movies"} locally.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return@launch
            }

            val favorito = FavoritoEntity(
                idMovieEntity = pelicula.id,
                pelicula = pelicula,
                userId = userId
            )
            movieRepository.insertFavorito(favorito)
        }
    }

    fun removeFromLocalFavorites(
        pelicula: Peliculas
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val favorito = movieRepository.getFavoritoById(pelicula.id)
            if (favorito != null) {
                movieRepository.deleteFavorito(favorito)
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
                        poster_path = movieData["poster_path"] as? String ?: "",
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
    fun getFavoriteSeries(uid: String, onSuccess: (List<Peliculas>) -> Unit, onFailure: (Exception) -> Unit) {
        val favoritesCollection = firestore.collection("usuarios").document(uid)

        // Obtener las series favoritas del usuario
        favoritesCollection.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val favoriteItems = document.get("seriesFavoritas") as? List<Map<String, Any>> ?: emptyList()

                // Mapear los favoritos a objetos de tipo Peliculas
                val seriesFavoritas = favoriteItems.mapNotNull { movieData ->
                    Peliculas(
                        id = movieData["id"] as? String ?: "",
                        title = movieData["name"] as? String ?: "",
                        poster_path = movieData["poster_path"] as? String ?: "",
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

    // Función para obtener las películas favoritas desde Room
    fun getPeliculasFavoritasDesdeRoom() {
        viewModelScope.launch {
            // Obtener las películas favoritas desde Room
            val favoritos = userId?.let { movieRepository.getPeliculasFavoritas(it) }

            // Mapear la lista de FavoritoEntity a una lista de Peliculas
            val peliculas = favoritos?.map { favorito ->
                favorito.pelicula // Accedemos directamente a la propiedad 'pelicula' de FavoritoEntity
            }

            // Actualizar el estado con la lista de películas favoritas
            if (peliculas != null) {
                favouriteMovies.value = peliculas
            } // Usamos .value para modificar el estado de la UI
        }
    }

    // Función para obtener las series favoritas desde Room
    fun getSeriesFavoritasDesdeRoom() {
        viewModelScope.launch {
            // Obtener las series favoritas desde Room
            val favoritos = userId?.let { movieRepository.getSeriesFavoritas(it) }

            // Mapear la lista de FavoritoEntity a una lista de Peliculas (para series)
            val series = favoritos?.map { favorito ->
                favorito.pelicula // Accede directamente a la propiedad 'pelicula' de FavoritoEntity
            }

            // Actualizar el estado de la UI con la lista de series favoritas
            if (series != null) {
                favouriteSeries.value = series
            }
        }
    }

}
