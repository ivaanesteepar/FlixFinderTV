package com.example.flixfindertv.utils

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import kotlinx.coroutines.launch


@Composable
fun ShowComments(commentsList: List<Comentarios>, viewModel: CommentsViewModel) {
    val usersViewModel: UsersViewModel = viewModel()
    var nombreUsuario: String? by remember { mutableStateOf(null) }
    val responseText = remember { mutableStateOf("") }
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val showDialogState = remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }
    val showAllResponsesState = remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }
    val commentsLikesState = remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    val likedCommentsState = remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }  // Mapa para manejar el estado de "like" por comentario
    val responsesLikesState = remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    val likedCommentsStateResponse = remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }
    val context = LocalContext.current // Para mostrar el Toast
    var isUserAdmin by remember { mutableStateOf(false) }

    // Llamamos a obtenerNombreUsuario y getUserAdminStatus cuando el userId cambia
    LaunchedEffect(userId) {
        if (userId != null) {
            viewModel.obtenerNombreUsuario(userId) { nombre -> nombreUsuario = nombre }
            usersViewModel.getUserAdminStatus(userId) { isAdmin -> isUserAdmin = isAdmin }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
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
                // Usamos comentario.revision directamente
                var shouldShowComment = !comentario.revision || isUserAdmin

                // Si el comentario no debe mostrarse, saltamos al siguiente
                if (!shouldShowComment) {
                    return@forEach
                }

                val showDialog = showDialogState.value[comentario.id] ?: false
                val showDeleteDialog = remember { mutableStateOf(false) }
                val showPublishDialog = remember { mutableStateOf(false) }
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

                // Cambiar el color del Card si el comentario tiene revision == true
                Card(modifier = Modifier.zIndex(0f).background(Color.White)) {
                    Column(modifier = Modifier.padding(8.dp).background(
                        if (isUserAdmin && shouldShowComment && comentario.revision) Color(0xFFFFCDD2)
                        else Color.White
                    )){
                        var fotoPerfilUrl by remember { mutableStateOf("") }

                        LaunchedEffect(comentario.usuario) {
                            viewModel.getUserProfilePhoto(comentario.usuario) { url -> fotoPerfilUrl = url ?: "" }
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
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )

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
                            Row(
                                verticalAlignment = Alignment.CenterVertically, // Alinea todos los íconos verticalmente al centro
                                horizontalArrangement = Arrangement.spacedBy(8.dp), // Espaciado entre los elementos
                                modifier = Modifier.fillMaxWidth() // Asegura que los elementos ocupen el ancho disponible
                            ) {
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
                                )
                                Spacer(modifier = Modifier.weight(1f))

                                // Si el comentario tiene 'revision' igual a true, mostrar el ícono de publicación
                                if (comentario.revision) {
                                    IconButton(
                                        onClick = {
                                            showPublishDialog.value = true
                                        },
                                        modifier = Modifier
                                            .size(30.dp) // Aseguramos que el tamaño del ícono de basura sea el mismo que los demás
                                            .padding(top = 0.dp) // Ajustamos el espaciado para que esté alineado
                                    ) {
                                        // Icono de publicar
                                        Icon(
                                            imageVector = Icons.Filled.Publish,
                                            contentDescription = "Publicar",
                                            tint = Color.Blue, // Puedes cambiar el color aquí
                                            modifier = Modifier.size(30.dp) // Tamaño del icono dentro del botón
                                        )
                                    }
                                }

                                // Icono de basura para el admin
                                if (isUserAdmin) {
                                    IconButton(
                                        onClick = {
                                            showDeleteDialog.value = true
                                        },
                                        modifier = Modifier
                                            .size(30.dp) // Aseguramos que el tamaño del ícono de basura sea el mismo que los demás
                                            .padding(top = 0.dp) // Ajustamos el espaciado para que esté alineado
                                    ) {
                                        // Icono de basura
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = "Eliminar comentario",
                                            tint = Color.Red, // Puedes cambiar el color aquí
                                            modifier = Modifier.size(30.dp) // Tamaño del icono dentro del botón
                                        )
                                    }
                                }
                            }
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
                                    val coroutineScope = rememberCoroutineScope()
                                    TextButton(
                                        onClick = {
                                            val comentarioTexto = responseText.value

                                            // Validamos si el comentario contiene palabras censurables
                                            coroutineScope.launch {
                                                val isOffensive = validateComment(comentarioTexto)

                                                if (isOffensive) {
                                                    // Si el comentario es ofensivo, cerramos el dialog y mostramos un mensaje
                                                    showDialogState.value = showDialogState.value.toMutableMap().apply {
                                                        put(comentario.id, false)
                                                    }
                                                    responseText.value = ""

                                                    // Mostrar Toast indicando que el comentario será revisado
                                                    Toast.makeText(context, "Tu mensaje será revisado antes de publicarse.", Toast.LENGTH_SHORT).show()
                                                    viewModel.sendResponse(
                                                        idContenido = comentario.idContenido,
                                                        comentarioId = comentario.id,
                                                        usuarioNombre = comentario.usuario,
                                                        respuesta = comentarioTexto,
                                                        reviewed = true
                                                    )
                                                } else {
                                                    // Si no es ofensivo, enviamos la respuesta
                                                    viewModel.sendResponse(
                                                        idContenido = comentario.idContenido,
                                                        comentarioId = comentario.id,
                                                        usuarioNombre = comentario.usuario,
                                                        respuesta = comentarioTexto,
                                                        reviewed = false
                                                    )
                                                    showDialogState.value = showDialogState.value.toMutableMap().apply {
                                                        put(comentario.id, false)
                                                    }
                                                    responseText.value = ""
                                                }
                                            }
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
                        // Cuadro de diálogo para confirmar la eliminación
                        if (showDeleteDialog.value) {
                            AlertDialog(
                                onDismissRequest = {
                                    showDeleteDialog.value = false // Cerrar el diálogo si se toca fuera de él
                                },
                                title = {
                                    Text("Confirmar eliminación")
                                },
                                text = {
                                    Text("¿Estás seguro de que quieres eliminar este comentario?")
                                },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            // Lógica para eliminar el comentario
                                            // Aquí iría la lógica para eliminar el comentario de la base de datos

                                            // Cerrar el cuadro de diálogo después de eliminar
                                            showDeleteDialog.value = false
                                        }
                                    ) {
                                        Text("Sí")
                                    }
                                },
                                dismissButton = {
                                    Button(
                                        onClick = {
                                            // Cerrar el cuadro de diálogo sin hacer nada
                                            showDeleteDialog.value = false
                                        }
                                    ) {
                                        Text("No")
                                    }
                                }
                            )
                        }
                        // Cuadro de diálogo para confirmar la eliminación
                        if (showPublishDialog.value) {
                            AlertDialog(
                                onDismissRequest = {
                                    showPublishDialog.value = false // Cerrar el diálogo si se toca fuera de él
                                },
                                title = {
                                    Text("Confirmar publicación")
                                },
                                text = {
                                    Text("¿Estás seguro de que quieres publicar este comentario?")
                                },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            // Lógica para eliminar el comentario
                                            // Aquí iría la lógica para eliminar el comentario de la base de datos

                                            // Cerrar el cuadro de diálogo después de eliminar
                                            showPublishDialog.value = false
                                        }
                                    ) {
                                        Text("Sí")
                                    }
                                },
                                dismissButton = {
                                    Button(
                                        onClick = {
                                            // Cerrar el cuadro de diálogo sin hacer nada
                                            showPublishDialog.value = false
                                        }
                                    ) {
                                        Text("No")
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
                            // Verifica si alguna respuesta no está en revisión o si el usuario es admin
                            val shouldShowDivider = comentario.respuestas.any { !it.revision || isUserAdmin }

                            // Solo mostrar el divisor si alguna respuesta no está en revisión o si el usuario es admin
                            if (shouldShowDivider) {
                                HorizontalDivider()
                                Spacer(modifier = Modifier.height(5.dp))
                            }
                        }


                        Column(modifier = Modifier.padding(8.dp).background(Color.White)) {
                            respuestasParaMostrar.forEachIndexed { index, res ->
                                println("revisiones: ${res.revision}")
                                if (res.revision && !isUserAdmin) {
                                    return@forEachIndexed
                                }

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
                                    Column(modifier = Modifier.padding(8.dp).background(if (res.revision) Color(0xFFFFCDD2) else Color.Transparent)) {
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
                                            // Icono de corazón para la respuesta
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

                                            // Agregar un Spacer para separar el ícono de eliminar
                                            Spacer(modifier = Modifier.width(16.dp))

                                            // Si el comentario tiene 'revision' igual a true, mostrar el ícono de publicación
                                            if (res.revision) {
                                                Icon(
                                                    imageVector = Icons.Filled.Publish, // Usamos el ícono de publicar
                                                    contentDescription = "Respuesta en revisión",
                                                    tint = Color.Blue, // Puedes cambiar el color si lo deseas
                                                    modifier = Modifier.size(30.dp)
                                                )
                                            }

                                            // Icono de basura para el admin
                                            if (isUserAdmin) {
                                                IconButton(
                                                    onClick = {
                                                        showDeleteDialog.value = true
                                                    },
                                                    modifier = Modifier
                                                        .size(30.dp) // Aseguramos que el tamaño del ícono de basura sea el mismo que los demás
                                                        .padding(top = 0.dp) // Ajustamos el espaciado para que esté alineado
                                                ) {
                                                    // Icono de basura
                                                    Icon(
                                                        imageVector = Icons.Filled.Delete,
                                                        contentDescription = "Eliminar respuesta",
                                                        tint = Color.Red, // Puedes cambiar el color aquí
                                                        modifier = Modifier.size(30.dp) // Tamaño del icono dentro del botón
                                                    )
                                                }
                                            }
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
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
