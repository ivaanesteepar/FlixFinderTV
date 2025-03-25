package com.example.flixfindertv.ui.viewmodels

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flixfindertv.models.Comentarios
import com.example.flixfindertv.models.Respuestas
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class CommentsViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    // Cambiar mutableStateOf a MutableStateFlow
    private val _comments = MutableStateFlow<List<Comentarios>>(emptyList())
    val comments: StateFlow<List<Comentarios>> get() = _comments

    fun addLikeToResponse(idContenido: String, comentarioId: String, respuestaId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val comentariosRef = FirebaseFirestore.getInstance().collection("comentarios")
                .document(idContenido)
                .collection("comentarios")
                .document(comentarioId)

            comentariosRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val respuestas = document.get("respuestas") as? MutableList<Map<String, Any>> ?: mutableListOf()
                    val index = respuestas.indexOfFirst { it["id"] == respuestaId }
                    if (index != -1) {
                        val respuesta = respuestas[index].toMutableMap()
                        val likes = (respuesta["likes"] as? Long ?: 0) + 1
                        respuesta["likes"] = likes

                        obtenerNombreUsuario(userId) { userName ->
                            if (userName != null) {
                                val nombreLikes = (respuesta["nombreLikes"] as? MutableList<String>) ?: mutableListOf()
                                if (!nombreLikes.contains(userName)) {
                                    nombreLikes.add(userName)
                                    respuesta["nombreLikes"] = nombreLikes
                                }
                                respuestas[index] = respuesta
                                comentariosRef.update("respuestas", respuestas)
                            }
                        }
                    }
                }
            }
        }
    }

    fun removeLikeFromResponse(idContenido: String, comentarioId: String, respuestaId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val comentariosRef = FirebaseFirestore.getInstance().collection("comentarios")
                .document(idContenido)
                .collection("comentarios")
                .document(comentarioId)

            comentariosRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val respuestas = document.get("respuestas") as? MutableList<Map<String, Any>> ?: mutableListOf()
                    val index = respuestas.indexOfFirst { it["id"] == respuestaId }
                    if (index != -1) {
                        val respuesta = respuestas[index].toMutableMap()
                        val likes = ((respuesta["likes"] as? Long) ?: 0) - 1
                        respuesta["likes"] = if (likes < 0) 0 else likes

                        obtenerNombreUsuario(userId) { userName ->
                            if (userName != null) {
                                val nombreLikes = (respuesta["nombreLikes"] as? MutableList<String>) ?: mutableListOf()
                                if (nombreLikes.contains(userName)) {
                                    nombreLikes.remove(userName)
                                    respuesta["nombreLikes"] = nombreLikes
                                }
                                respuestas[index] = respuesta
                                comentariosRef.update("respuestas", respuestas)
                            }
                        }
                    }
                }
            }
        }
    }

    suspend fun obtenerNombreLikesDeRespuesta(idContenido: String, comentarioId: String, respuestaId: String): List<String> {
        val db = FirebaseFirestore.getInstance()
        val comentarioRef = db.collection("comentarios")
            .document(idContenido)
            .collection("comentarios")
            .document(comentarioId)

        return try {
            val snapshot: DocumentSnapshot = comentarioRef.get().await()
            if (snapshot.exists()) {
                val respuestas = snapshot.get("respuestas") as? List<Map<String, Any>> ?: emptyList()
                val respuesta = respuestas.find { it["id"] == respuestaId }
                respuesta?.get("nombreLikes") as? List<String> ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }


    suspend fun obtenerNombreLikes(peliculaId: String, comentarioId: String): List<String> {
        val db = FirebaseFirestore.getInstance()

        // Obtener la referencia del comentario específico en Firestore
        val comentarioRef = db.collection("comentarios")  // Colección de comentarios
            .document(peliculaId) // Documento de la película
            .collection("comentarios") // Subcolección de comentarios
            .document(comentarioId)  // Documento del comentario

        return try {
            // Realizamos la consulta para obtener el documento del comentario
            val snapshot: DocumentSnapshot = comentarioRef.get().await()

            // Verificamos si el documento existe y luego obtenemos el campo 'nombreLikes'
            if (snapshot.exists()) {
                // Retornamos el valor de 'nombreLikes' como una lista de String
                val nombreLikes = snapshot.get("nombreLikes") as? List<String> ?: emptyList()
                nombreLikes
            } else {
                emptyList()  // Si el comentario no existe, retornamos una lista vacía
            }
        } catch (e: Exception) {
            // Manejo de excepciones en caso de que ocurra un error
            e.printStackTrace()
            emptyList()  // Si hay un error, retornamos una lista vacía
        }
    }

    // Función que obtiene el nombre del usuario de Firestore usando un callback
    fun obtenerNombreUsuario(uid: String, callback: (String?) -> Unit) {
        FirebaseFirestore.getInstance().collection("usuarios")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val nombre = document.getString("nombre")  // Obtener el nombre del campo 'nombre'
                    callback(nombre)  // Llamar al callback con el nombre
                } else {
                    Log.w("UsersViewModel", "No such document")
                    callback(null)  // Si no se encuentra el documento, llamar al callback con null
                }
            }
            .addOnFailureListener { exception ->
                Log.e("UsersViewModel", "Error getting document: ", exception)
                callback(null)  // En caso de error, llamar al callback con null
            }
    }

    fun addLike(idContenido: String, comentarioId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val comentariosRef = FirebaseFirestore.getInstance().collection("comentarios")
                .document(idContenido)
                .collection("comentarios")
                .document(comentarioId)

            // Aumentamos el contador de likes del comentario directamente
            comentariosRef.update("likes", FieldValue.increment(1))

            // Obtener el nombre del usuario para agregarlo a la lista de nombres
            obtenerNombreUsuario(userId) { userName ->
                if (userName != null) {
                    // Obtener el campo nombreLikes y agregar el nombre del usuario si no está presente
                    comentariosRef.get().addOnSuccessListener { document ->
                        val nombreLikes = document.get("nombreLikes") as? List<String> ?: emptyList()
                        // Verificar si el nombre ya está en la lista antes de agregarlo
                        if (!nombreLikes.contains(userName)) {
                            val updatedNombreLikes = nombreLikes.toMutableList().apply {
                                add(userName) // Agregar el nombre del usuario a la lista
                            }

                            // Actualizamos el campo nombreLikes con la lista modificada
                            comentariosRef.update("nombreLikes", updatedNombreLikes)
                        }
                    }
                }
            }
        }
    }

    fun removeLike(idContenido: String, comentarioId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val comentariosRef = FirebaseFirestore.getInstance().collection("comentarios")
                .document(idContenido)
                .collection("comentarios")
                .document(comentarioId)

            // Eliminar el like en la subcolección "likes"
            val likesRef = comentariosRef.collection("likes")
            likesRef.document(userId).delete()

            // Luego, disminuir el contador de likes del comentario
            comentariosRef.update("likes", FieldValue.increment(-1))

            // Obtener el nombre del usuario a través del callback
            obtenerNombreUsuario(userId) { userName ->
                if (userName != null) {
                    // Obtener el campo nombreLikes y quitar el nombre del usuario si está presente
                    comentariosRef.get().addOnSuccessListener { document ->
                        val nombreLikes = document.get("nombreLikes") as? List<String> ?: emptyList()
                        // Verificar si el nombre ya está en la lista antes de eliminarlo
                        if (nombreLikes.contains(userName)) {
                            val updatedNombreLikes = nombreLikes.toMutableList().apply {
                                remove(userName) // Eliminar el nombre del usuario de la lista
                            }

                            // Actualizar el campo nombreLikes con la lista modificada
                            comentariosRef.update("nombreLikes", updatedNombreLikes)
                        }
                    }
                }
            }
        }
    }


    // Función para escuchar los cambios de 'likes' en un comentario específico
    fun listenToComentarioLikes(peliculaId: String, comentarioId: String, onLikesChanged: (Int) -> Unit): ListenerRegistration {
        return firestore.collection("comentarios") // Colección de comentarios
            .document(peliculaId) // Documento correspondiente a la película
            .collection("comentarios") // Subcolección de comentarios
            .document(comentarioId) // Documento correspondiente al comentario
            .addSnapshotListener { documentSnapshot, exception ->
                if (exception != null || documentSnapshot == null) {
                    // Maneja el error si es necesario
                    return@addSnapshotListener
                }

                // Extrae el número de likes
                val likes = documentSnapshot.getLong("likes")?.toInt() ?: 0
                onLikesChanged(likes) // Pasa el número de likes al callback
            }
    }

    // Función para escuchar los cambios de 'likes' en una respuesta dentro del campo 'respuestas' en el comentario
    fun listenToRespuestaLikes(
        peliculaId: String,
        comentarioId: String,
        respuestaId: String,
        onLikesChanged: (Int) -> Unit
    ): ListenerRegistration {
        return firestore.collection("comentarios") // Colección de comentarios
            .document(peliculaId) // Documento correspondiente a la película
            .collection("comentarios") // Subcolección de comentarios
            .document(comentarioId) // Documento correspondiente al comentario
            .addSnapshotListener { documentSnapshot, exception ->
                if (exception != null || documentSnapshot == null) {
                    // Maneja el error si es necesario
                    return@addSnapshotListener
                }

                // Extrae la lista de respuestas del comentario
                val respuestas = documentSnapshot.get("respuestas") as? List<Map<String, Any>> ?: emptyList()

                // Encuentra la respuesta por su ID
                val respuesta = respuestas.find { it["id"] == respuestaId }

                // Extrae el número de likes de la respuesta
                val likes = respuesta?.get("nombreLikes") as? List<String> ?: emptyList()
                onLikesChanged(likes.size) // Pasa el número de likes al callback
            }
    }


    // Función para obtener los comentarios de la subcolección 'subcoleccionComentarios' dentro de un documento 'idContenido'
    fun getComments(idContenido: String) {
        firestore.collection("comentarios")  // Nueva colección comentarios
            .document(idContenido)  // Documento con el idContenido
            .collection("comentarios")  // Subcolección de comentarios
            .get()
            .addOnSuccessListener { querySnapshot ->
                val comentariosList = querySnapshot.documents.mapNotNull { document ->
                    try {
                        val id = document.getString("id") ?: return@mapNotNull null
                        val usuario = document.getString("usuario") ?: return@mapNotNull null
                        val puntuacion = (document.getLong("puntuacion")?.toInt()) ?: return@mapNotNull null
                        val comentario = document.getString("comentario") ?: return@mapNotNull null
                        val idContenido = document.getString("idContenido") ?: return@mapNotNull null
                        // Obtener las respuestas, que están en formato HashMap
                        val respuestasList = document["respuestas"] as? List<HashMap<String, Any>> ?: emptyList()

                        // Convertir cada HashMap de respuesta en un objeto Respuestas
                        val respuestas = respuestasList.mapNotNull { respuestaMap ->
                            try {
                                val idRespuesta = respuestaMap["id"] as? String ?: return@mapNotNull null
                                val usuarioRespuesta = respuestaMap["usuario"] as? String ?: return@mapNotNull null
                                val respuestaTexto = respuestaMap["respuesta"] as? String ?: return@mapNotNull null
                                val fechaPublicacion = respuestaMap["fechaPublicacion"] as? Timestamp ?: return@mapNotNull null

                                Respuestas(idRespuesta, id, idContenido, usuarioRespuesta, respuestaTexto, fechaPublicacion)
                            } catch (e: Exception) {
                                // Si hay un error en la conversión, omitir esta respuesta
                                null
                            }
                        }
                        Comentarios(id, usuario, puntuacion, comentario, respuestas, idContenido)
                    } catch (e: Exception) {
                        null
                    }
                }
                _comments.value = comentariosList
            }
            .addOnFailureListener {
                // Manejar el error si ocurre
            }
    }


    // Función para agregar un nuevo comentario a la subcolección 'subcoleccionComentarios' dentro de un documento 'idContenido'
    fun sendComment(idContenido: String, usuarioNombre: String, puntuacion: Int, comentario: String) {
        val newComment = Comentarios(
            id = UUID.randomUUID().toString(),
            usuario = usuarioNombre,
            puntuacion = puntuacion,
            comentario = comentario,
            respuestas = emptyList(),
            idContenido = idContenido
        )

        // Obtener la referencia de la subcolección donde se almacenan los comentarios
        firestore.collection("comentarios")  // Nueva colección de comentarios
            .document(idContenido)  // Documento de la película o contenido
            .collection("comentarios")  // Subcolección de comentarios
            .document(newComment.id)  // Usamos el ID del comentario como nombre del documento
            .set(newComment)
            .addOnSuccessListener {
                // Actualizar los comentarios después de agregar el nuevo
                getComments(idContenido)
            }
            .addOnFailureListener {
                // Manejar el error si ocurre
            }
    }

    // Función para obtener la URL de la foto de perfil de un usuario
    fun getUserProfilePhoto(usuarioNombre: String, onProfilePhotoFetched: (String?) -> Unit) {
        viewModelScope.launch {
            firestore.collection("usuarios")  // Accedemos a la colección 'usuarios'
                .whereEqualTo("nombre", usuarioNombre)  // Hacemos un filtro donde el campo 'nombre' sea igual al 'usuarioNombre'
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        // Si encontramos el documento, obtenemos la URL de la foto de perfil
                        val document = querySnapshot.documents.first()  // Suponemos que el nombre es único
                        val photoUrl = document.getString("fotoPerfil")
                        onProfilePhotoFetched(photoUrl)
                    } else {
                        // Si no encontramos el documento con ese nombre de usuario
                        onProfilePhotoFetched(null)
                    }
                }
                .addOnFailureListener {
                    // Si ocurre algún error al obtener los datos
                    onProfilePhotoFetched(null)
                }
        }
    }

    fun sendResponse(idContenido: String, comentarioId: String, usuarioNombre: String, respuesta: String) {
        // Crear una nueva instancia de la respuesta
        val nuevaRespuesta = Respuestas(
            id = UUID.randomUUID().toString(), // Generar un ID único para la respuesta
            idComentario = comentarioId,
            idContenido = idContenido,
            usuario = usuarioNombre,
            respuesta = respuesta,
            fechaPublicacion = Timestamp.now() // Usar la fecha y hora del servidor
        )

        // Actualizar el comentario con la nueva respuesta dentro de la subcolección de comentarios
        firestore.collection("comentarios")  // Colección comentarios
            .document(idContenido)  // Documento de la película o contenido
            .collection("comentarios")  // Subcolección de comentarios
            .document(comentarioId)  // Documento del comentario específico
            .update(
                "respuestas",  // Apuntar al campo 'respuestas' dentro del comentario específico
                FieldValue.arrayUnion(nuevaRespuesta)  // Añadir la respuesta al campo 'respuestas'
            )
            .addOnSuccessListener {
                // Si la actualización es exitosa, puedes actualizar la lista de comentarios si lo deseas
                println("Respuesta enviada exitosamente")
                getComments(idContenido)  // Actualiza la lista de comentarios si lo deseas
            }
            .addOnFailureListener { e ->
                // Manejar el error si ocurre
                println("Error al enviar respuesta: ${e.message}")
            }
    }


}
