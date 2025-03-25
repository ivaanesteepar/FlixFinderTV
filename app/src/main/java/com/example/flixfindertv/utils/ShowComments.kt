package com.example.flixfindertv.utils

import androidx.compose.runtime.Composable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.rememberAsyncImagePainter
import com.example.flixfindertv.R
import java.text.SimpleDateFormat
import java.util.*
import com.example.flixfindertv.models.Comentarios
import com.example.flixfindertv.ui.viewmodels.CommentsViewModel
import com.example.flixfindertv.ui.viewmodels.UsersViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth


@Composable
fun ShowComments(commentsList: List<Comentarios>, viewModel: CommentsViewModel) {
    var nombreUsuario: String? by remember { mutableStateOf(null) }
    val responseText = remember { mutableStateOf("") }
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val showDialogState = remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }
    val showAllResponsesState = remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }
    val commentsLikesState = remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    val likedCommentsState = remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }  // Mapa para manejar el estado de "like" por comentario
    val responsesLikesState = remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    val likedCommentsStateResponse = remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }

    // Llamamos a obtenerNombreUsuario cuando el userId cambia
    LaunchedEffect(userId) {
        if (userId != null) {
            // Usamos el callback para obtener el nombre del usuario
            viewModel.obtenerNombreUsuario(userId) { nombre ->
                // Almacenamos el nombre del usuario en la variable mutable
                nombreUsuario = nombre
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth().background(Color.White)) {
        // Verificar si no hay comentarios
        if (commentsList.isEmpty()) {
            Text(
                text = "There are no comments yet. Be the first to comment!",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp)
            )
        } else {
            commentsList.forEach { comentario ->
                val showDialog = showDialogState.value[comentario.id] ?: false
                val showAllResponses = showAllResponsesState.value[comentario.id] ?: false
                var isLiked = likedCommentsState.value[comentario.id] ?: false
                val likes = commentsLikesState.value[comentario.id] ?: comentario.likes

                DisposableEffect(comentario.id) {
                    // Configuramos el listener
                    val listener = viewModel.listenToComentarioLikes(comentario.idContenido, comentario.id) { newLikes ->
                        // Actualizamos el estado de likes cuando Firestore detecta un cambio
                        val updatedLikesMap = commentsLikesState.value.toMutableMap().apply {
                            put(comentario.id, newLikes)
                        }
                        commentsLikesState.value = updatedLikesMap
                    }

                    // Acción de limpieza cuando el Composable se elimina
                    onDispose {
                        listener.remove()
                    }
                }

                // Obtenemos los nombres de los usuarios que han dado like a este comentario
                LaunchedEffect(comentario.id) {
                    val nombreLikes =
                        viewModel.obtenerNombreLikes(comentario.idContenido, comentario.id)
                    isLiked =
                        nombreLikes.contains(nombreUsuario) // Verificamos si el nombre del usuario está en los likes
                    // Actualizamos el estado de "like" para este comentario
                    val updatedLikedComments = likedCommentsState.value.toMutableMap().apply {
                        put(comentario.id, isLiked)
                    }
                    likedCommentsState.value = updatedLikedComments
                }

                Card(modifier = Modifier.zIndex(0f).background(Color.White)) {
                    Column(modifier = Modifier.padding(8.dp).background(Color.White)) {
                        var fotoPerfilUrl by remember { mutableStateOf("") }

                        LaunchedEffect(comentario.usuario) {
                            viewModel.getUserProfilePhoto(comentario.usuario) { url ->
                                fotoPerfilUrl = url ?: ""
                            }
                        }

                        // Row para foto de perfil, nombre, fecha y rating
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(fotoPerfilUrl.ifEmpty { R.drawable.no_profile_icon }),
                                contentDescription = "Foto de perfil",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, Color.Black, CircleShape)
                                    .background(Color.White),
                                contentScale = ContentScale.Crop
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = comentario.usuario,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.Black
                                )
                                Spacer(modifier = Modifier.height(4.dp))

                                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                val formattedDate =
                                    dateFormat.format(comentario.fechaPublicacion.toDate())
                                Text(
                                    text = formattedDate,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }

                            Text(
                                text = "${comentario.puntuacion / 2} stars",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = comentario.comentario,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Botón de responder
                            TextButton(
                                onClick = {
                                    // Acción para abrir el diálogo de respuesta
                                    showDialogState.value =
                                        showDialogState.value.toMutableMap().apply {
                                            put(comentario.id, true)
                                        }
                                },
                                modifier = Modifier.padding(end = 8.dp) // Espaciado entre el botón y el icono del corazón
                            ) {
                                Text("Responder")
                            }
                            // Icono de corazón
                            Icon(
                                imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = "Corazón",
                                tint = if (isLiked) Color.Red else Color.Black,
                                modifier = Modifier
                                    .size(30.dp)
                                    .clickable {
                                        // Cambiar el estado de like solo para este comentario
                                        val updatedLikedComments = likedCommentsState.value.toMutableMap().apply {
                                            put(comentario.id, !isLiked)
                                        }
                                        likedCommentsState.value = updatedLikedComments

                                        // Ahora actualizamos la base de datos dependiendo del nuevo estado
                                        if (isLiked) {
                                            // Si ya estaba likeado y el usuario quita el like, eliminamos el like
                                            viewModel.removeLike(comentario.idContenido, comentario.id)
                                        } else {
                                            // Si no estaba likeado y el usuario da like, agregamos el like
                                            viewModel.addLike(comentario.idContenido, comentario.id)
                                        }
                                    }
                                    .padding(start = 4.dp)
                            )
                            // Mostrar el número de likes al lado del corazón
                            Text(
                                text = "$likes",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Black,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }

                        // Mostrar el AlertDialog solo para el comentario correspondiente
                        if (showDialog) {
                            AlertDialog(
                                onDismissRequest = {
                                    showDialogState.value =
                                        showDialogState.value.toMutableMap().apply {
                                            put(comentario.id, false)
                                        }
                                    responseText.value = ""
                                },
                                title = { Text(text = "Responder al comentario") },
                                text = {
                                    OutlinedTextField(
                                        value = responseText.value,
                                        onValueChange = { responseText.value = it },
                                        label = { Text("Escribe tu respuesta") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(250.dp)
                                            .padding(top = 16.dp)
                                    )
                                },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            viewModel.sendResponse(
                                                idContenido = comentario.idContenido,
                                                comentarioId = comentario.id,
                                                usuarioNombre = comentario.usuario,
                                                respuesta = responseText.value
                                            )
                                            showDialogState.value =
                                                showDialogState.value.toMutableMap().apply {
                                                    put(comentario.id, false)
                                                }
                                            responseText.value = ""
                                        }
                                    ) {
                                        Text("Enviar")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = {
                                        showDialogState.value =
                                            showDialogState.value.toMutableMap().apply {
                                                put(comentario.id, false)
                                            }
                                        responseText.value = ""
                                    }) {
                                        Text("Cancelar")
                                    }
                                }
                            )
                        }
                        // Mostrar las respuestas
                        val respuestasParaMostrar =
                            if (comentario.respuestas.size > 2 && !showAllResponses) {
                                comentario.respuestas.take(2)
                            } else {
                                comentario.respuestas
                            }

                        if (comentario.respuestas.isNotEmpty()) {
                            // Agregar una línea horizontal antes de las respuestas
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        Column(modifier = Modifier.padding(8.dp).background(Color.White)) {
                            respuestasParaMostrar.forEachIndexed { index, res ->
                                val isLikedResponse = likedCommentsStateResponse.value[res.id] ?: false
                                val likesRespuesta = responsesLikesState.value[res.id] ?: res.likes

                                DisposableEffect(res.id) {
                                    // Configuramos el listener
                                    val listener = viewModel.listenToRespuestaLikes(res.idContenido, res.idComentario, res.id) { newLikes ->
                                        // Actualizamos el estado de likes cuando Firestore detecta un cambio
                                        val updatedLikesMapResponses = responsesLikesState.value.toMutableMap().apply {
                                            put(res.id, newLikes)
                                        }
                                        responsesLikesState.value = updatedLikesMapResponses
                                    }

                                    // Acción de limpieza cuando el Composable se elimina
                                    onDispose {
                                        listener.remove()
                                    }
                                }

                                LaunchedEffect(res.id) {
                                    val nombreLikesResponse =
                                        viewModel.obtenerNombreLikesDeRespuesta(res.idContenido, res.idComentario, res.id)
                                    val isCurrentlyLiked = nombreLikesResponse.contains(nombreUsuario)

                                    // Actualizamos el estado global de likes de respuestas
                                    likedCommentsStateResponse.value = likedCommentsStateResponse.value.toMutableMap().apply {
                                        put(res.id, isCurrentlyLiked)
                                    }
                                }

                                Card(
                                    modifier = Modifier.padding(
                                        start = 24.dp,
                                        top = 8.dp,
                                        end = 16.dp
                                    ).zIndex(1f)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        var respuestaFotoPerfilUrl by remember { mutableStateOf("") }

                                        LaunchedEffect(res.usuario) {
                                            viewModel.getUserProfilePhoto(res.usuario) { url ->
                                                respuestaFotoPerfilUrl = url ?: ""
                                            }
                                        }

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp),
                                            horizontalArrangement = Arrangement.Start
                                        ) {
                                            Image(
                                                painter = rememberAsyncImagePainter(
                                                    respuestaFotoPerfilUrl.ifEmpty { R.drawable.no_profile_icon }
                                                ),
                                                contentDescription = "Foto de perfil de respuesta",
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .border(2.dp, Color.Black, CircleShape)
                                                    .background(Color.White),
                                                contentScale = ContentScale.Crop
                                            )

                                            Spacer(modifier = Modifier.width(8.dp))

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = res.usuario,
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    color = Color.Black
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))

                                                val dateFormat = SimpleDateFormat(
                                                    "dd/MM/yyyy",
                                                    Locale.getDefault()
                                                )
                                                val formattedDate = dateFormat.format(res.fechaPublicacion.toDate())
                                                Text(
                                                    text = formattedDate,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color.Gray
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        Text(
                                            text = res.respuesta,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.Black,
                                            modifier = Modifier.padding(horizontal = 16.dp)
                                        )

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            Icon(
                                                imageVector = if (isLikedResponse) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                                contentDescription = "Corazón",
                                                tint = if (isLikedResponse) Color.Red else Color.Black,
                                                modifier = Modifier
                                                    .size(25.dp)
                                                    .clickable {
                                                        val newLikedState = !isLikedResponse

                                                        likedCommentsStateResponse.value =
                                                            likedCommentsStateResponse.value.toMutableMap().apply {
                                                                put(res.id, newLikedState)
                                                            }

                                                        if (newLikedState) {
                                                            viewModel.addLikeToResponse(res.idContenido, res.idComentario, res.id)
                                                        } else {
                                                            viewModel.removeLikeFromResponse(res.idContenido, res.idComentario, res.id)
                                                        }
                                                    }
                                            )
                                            // Mostrar el número de likes al lado del corazón
                                            Text(
                                                text = "$likesRespuesta", // Número de likes
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.Black,
                                                modifier = Modifier.padding(start = 8.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Mostrar el botón "Ver todas las respuestas"
                        if (comentario.respuestas.size > 2) {
                            Row(
                                modifier = Modifier.padding(start = 24.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                TextButton(onClick = {
                                    showAllResponsesState.value =
                                        showAllResponsesState.value.toMutableMap().apply {
                                            put(comentario.id, !showAllResponses)
                                        }
                                }) {
                                    val respuestasAcontar = comentario.respuestas.drop(2)
                                    Text(
                                        text = if (showAllResponses) {
                                            "Ver menos"
                                        } else {
                                            // Mostrar cuántas respuestas adicionales hay
                                            "Ver más respuestas (${respuestasAcontar.size})"
                                        },
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
