package com.example.flixfindertv.ui.viewmodels

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.flixfindertv.models.Peliculas
import com.example.flixfindertv.models.Usuarios
import com.example.flixfindertv.room.database.AppDatabase
import com.example.flixfindertv.room.entities.FavoritoEntity
import com.example.flixfindertv.room.repository.MovieRepository
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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


    fun cambiarContrasena(
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

        // 1. Create credentials with email and current password entered
        val credential = EmailAuthProvider.getCredential(email, passwordActual)

        // 2. Reauthenticate
        user.reauthenticate(credential)
            .addOnCompleteListener { authTask ->
                if (authTask.isSuccessful) {
                    // Current password correct, now update the password
                    user.updatePassword(passwordNueva)
                        .addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                callback(true, "Password changed successfully")
                            } else {
                                callback(false, "The new password must be at least 6 characters long")
                            }
                        }
                } else {
                    // Reauthentication failed -> current password incorrect
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

        userDocRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val generosFavoritos = document.get("generosFavoritos") as? Map<String, Number> ?: emptyMap()
                val nuevosGeneros = HashMap(generosFavoritos) // Asegura que sea mutable y compatible

                if (nuevosGeneros.containsKey(firstGenre)) {
                    // El género ya existe, actualizamos el timestamp
                    nuevosGeneros[firstGenre] = System.currentTimeMillis()
                    println("Género ya existe, se actualiza timestamp: $firstGenre")
                } else {
                    if (nuevosGeneros.size < maxGeneros) {
                        // Hay espacio, lo agregamos
                        nuevosGeneros[firstGenre] = System.currentTimeMillis()
                        println("Género agregado: $firstGenre")
                    } else {
                        // Reemplazamos el más antiguo
                        val generoMasAntiguo = nuevosGeneros.minByOrNull { it.value.toLong() }?.key
                        if (generoMasAntiguo != null) {
                            nuevosGeneros.remove(generoMasAntiguo)
                            nuevosGeneros[firstGenre] = System.currentTimeMillis()
                            println("Género reemplazado: $generoMasAntiguo por $firstGenre")
                        }
                    }
                }

                userDocRef.update("generosFavoritos", nuevosGeneros)
                    .addOnSuccessListener {
                        println("Géneros favoritos actualizados: $nuevosGeneros")
                    }
                    .addOnFailureListener { e ->
                        Log.e("UpdateFavoriteGenre", "Error al actualizar géneros favoritos", e)
                    }
            } else {
                println("Documento de usuario no existe.")
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

    fun saveToFavorites(context: Context, id: String, title: String, posterUrl: String, isSerie: Boolean) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val favoritesCollection = firestore.collection("usuarios").document(userId)

            val fieldToUpdate = if (isSerie) "seriesFavoritas" else "peliculasFavoritas"

            val movieData = mapOf(
                "id" to id,
                "title" to title,
                "posterUrl" to posterUrl,
                "esSerie" to isSerie
            )

            favoritesCollection.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val existingFavorites = document.get(fieldToUpdate) as? List<Map<String, Any>> ?: emptyList()

                    if (existingFavorites.any { it["id"] == id }) return@addOnSuccessListener

                    if (existingFavorites.size >= 20) {
                        Toast.makeText(
                            context,
                            "You can only save up to 20 favorite ${if (isSerie) "TV shows" else "movies"}.",
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

    fun saveToLocalFavorites(context: Context, pelicula: Peliculas) {
        CoroutineScope(Dispatchers.IO).launch {
            // Aquí llamas al repositorio para obtener las películas/series favoritas
            val currentFavorites = if (pelicula.esSerie) {
                movieRepository.getSeriesFavoritas()
            } else {
                movieRepository.getPeliculasFavoritas()
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

            val favorito = FavoritoEntity(idMovieEntity = pelicula.id, pelicula = pelicula)
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

    // Función para obtener las películas favoritas desde Room
    fun getPeliculasFavoritasDesdeRoom() {
        viewModelScope.launch {
            // Obtener las películas favoritas desde Room
            val favoritos = movieRepository.getPeliculasFavoritas()

            // Mapear la lista de FavoritoEntity a una lista de Peliculas
            val peliculas = favoritos.map { favorito ->
                favorito.pelicula // Accedemos directamente a la propiedad 'pelicula' de FavoritoEntity
            }

            // Actualizar el estado con la lista de películas favoritas
            favouriteMovies.value = peliculas // Usamos .value para modificar el estado de la UI
        }
    }

    // Función para obtener las series favoritas desde Room
    fun getSeriesFavoritasDesdeRoom() {
        viewModelScope.launch {
            // Obtener las series favoritas desde Room
            val favoritos = movieRepository.getSeriesFavoritas()

            // Mapear la lista de FavoritoEntity a una lista de Peliculas (para series)
            val series = favoritos.map { favorito ->
                favorito.pelicula // Accede directamente a la propiedad 'pelicula' de FavoritoEntity
            }

            // Actualizar el estado de la UI con la lista de series favoritas
            favouriteSeries.value = series
        }
    }

}
