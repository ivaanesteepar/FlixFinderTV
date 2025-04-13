package com.example.flixfindertv.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.flixfindertv.R
import com.example.flixfindertv.utils.ImgurUploader
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(navController: NavHostController) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    val currentUser = auth.currentUser
    var userName by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf("") }
    var newEmail by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPasswordField by remember { mutableStateOf(false) }
    var profileImageUri by remember { mutableStateOf<String?>(null) }

    var deleteImageInUI by remember { mutableStateOf(false) }

    // Launcher to pick an image from gallery
    val pickImageLauncher: ActivityResultLauncher<String> =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            // Solo actualiza la URI de la imagen si se seleccionó una nueva
            if (uri != null) {
                profileImageUri = uri.toString()
                deleteImageInUI = false
            }
        }

    // Cargar datos actuales del usuario
    LaunchedEffect(currentUser) {
        currentUser?.uid?.let { uid ->
            firestore.collection("usuarios").document(uid).get()
                .addOnSuccessListener { document ->
                    userName = document.getString("nombre") ?: ""
                    userEmail = document.getString("email") ?: ""  // Aquí cargamos el correo actual
                    newEmail = userEmail  // Asignamos el correo actual a newEmail
                    // Aquí puedes cargar la imagen del perfil si está disponible
                    profileImageUri = document.getString("fotoPerfil")
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Error al cargar datos", Toast.LENGTH_SHORT).show()
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Editar Perfil",
                        color = Color.White // Cambia el color del texto a blanco
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White // Cambia el color del icono a blanco
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent // Fondo transparente
                )
            )
        }

    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Image(
                painter = painterResource(id = R.drawable.fondo_app),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Imagen de perfil
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (profileImageUri.isNullOrEmpty()) {
                        Image(
                            painter = rememberAsyncImagePainter(R.drawable.no_profile_icon),
                            contentDescription = "Foto de perfil",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape) // Esto asegura que la imagen tenga forma circular
                                .border(2.dp, Color.White, CircleShape) // Aquí se agrega el borde blanco
                                .clickable { pickImageLauncher.launch("image/*") },
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = rememberAsyncImagePainter(profileImageUri),
                            contentDescription = "Imagen de perfil",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape) // Esto asegura que la imagen tenga forma circular
                                .border(2.dp, Color.White, CircleShape) // Aquí se agrega el borde blanco
                                .clickable { pickImageLauncher.launch("image/*") },
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                // Botón para eliminar la foto de perfil
                if (!profileImageUri.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            // Marcar como que se desea eliminar la foto de perfil solo en la UI
                            deleteImageInUI = true
                            profileImageUri = null // Eliminar la imagen solo en la UI
                        },
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar Foto",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Eliminar")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                // Campo Nombre
                OutlinedTextField(
                    value = userName,
                    onValueChange = { userName = it },
                    label = { Text("Nombre", color = Color.White) },
                    textStyle = LocalTextStyle.current.copy(color = Color.White), // Texto en blanco
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,    // Borde blanco al estar seleccionado
                        unfocusedBorderColor = Color.White, // Borde blanco en estado normal
                        focusedTextColor = Color.White,      // Texto en blanco
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White,           // Cursor blanco
                        focusedTrailingIconColor = Color.White, // Ícono de búsqueda blanco
                        unfocusedTrailingIconColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = userEmail,
                    onValueChange = {},
                    label = { Text("Correo Electrónico", color = Color(0xFFB0B0B0)) },
                    textStyle = LocalTextStyle.current.copy(color = Color(0xFFB0B0B0)), // Texto en blanco
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFB0B0B0),    // Borde blanco al estar seleccionado
                        unfocusedBorderColor = Color(0xFFB0B0B0),  // Borde blanco en estado normal
                        disabledBorderColor = Color(0xFFB0B0B0),  // Borde gris claro cuando está deshabilitado
                        focusedTextColor = Color(0xFFB0B0B0),      // Texto en blanco
                        unfocusedTextColor = Color(0xFFB0B0B0),
                        cursorColor = Color(0xFFB0B0B0),           // Cursor blanco
                        focusedTrailingIconColor = Color(0xFFB0B0B0), // Ícono de búsqueda blanco
                        unfocusedTrailingIconColor = Color(0xFFB0B0B0)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false // Deshabilitar el campo para que sea solo lectura
                )


                Spacer(modifier = Modifier.height(16.dp))

                // Mostrar campo de contraseña solo si el email cambia
                if (newEmail.isNotEmpty() && newEmail != userEmail) {
                    showPasswordField = true
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Contraseña actual") },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(color = Color.White), // Texto en blanco
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White,    // Borde blanco al estar seleccionado
                            unfocusedBorderColor = Color.White, // Borde blanco en estado normal
                            focusedTextColor = Color.White,      // Texto en blanco
                            unfocusedTextColor = Color.White,
                            cursorColor = Color.White,           // Cursor blanco
                            focusedTrailingIconColor = Color.White, // Ícono de búsqueda blanco
                            unfocusedTrailingIconColor = Color.White
                        ),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botón Guardar Cambios
                Button(
                    onClick = {
                        currentUser?.uid?.let { uid ->
                            val userUpdates = mutableMapOf<String, Any?>("nombre" to userName)

                            if (deleteImageInUI) {
                                userUpdates["fotoPerfil"] = null
                            } else if (!profileImageUri.isNullOrEmpty()) {
                                val imageUri = profileImageUri
                                val imageBytes = imageUri?.let { uri ->
                                    val inputStream = context.contentResolver.openInputStream(
                                        android.net.Uri.parse(uri)
                                    )
                                    inputStream?.readBytes()
                                }

                                if (imageBytes != null) {
                                    ImgurUploader.uploadImage(imageBytes) { imageUrl ->
                                        if (imageUrl != null) {
                                            userUpdates["fotoPerfil"] = imageUrl
                                            firestore.collection("usuarios").document(uid)
                                                .update(userUpdates)
                                                .addOnSuccessListener {
                                                    Toast.makeText(
                                                        context,
                                                        "Perfil actualizado",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    navController.popBackStack()
                                                }
                                                .addOnFailureListener {
                                                    Toast.makeText(
                                                        context,
                                                        "Error al actualizar",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Error al subir la imagen",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                    return@Button
                                }
                            }

                            // Si no hay nueva imagen y tampoco se marcó eliminar la foto, no modificar el campo fotoPerfil
                            if (userUpdates.isNotEmpty()) {
                                firestore.collection("usuarios").document(uid).update(userUpdates)
                                    .addOnSuccessListener {
                                        Toast.makeText(
                                            context,
                                            "Perfil actualizado",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        navController.popBackStack()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(
                                            context,
                                            "Error al actualizar",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Guardar Cambios")
                }
            }
        }
    }
}
