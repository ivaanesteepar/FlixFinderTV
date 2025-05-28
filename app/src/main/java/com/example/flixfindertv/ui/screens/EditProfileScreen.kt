package com.example.flixfindertv.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.flixfindertv.R
import com.example.flixfindertv.ui.viewmodels.ConexionViewModel
import com.example.flixfindertv.ui.viewmodels.UsersViewModel
import com.example.flixfindertv.utils.ImgurUploader

// Función que gestiona la pantalla de edición de perfil del usuario
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(navController: NavHostController) {
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val conexionViewModel: ConexionViewModel = viewModel()
    val hayConexion by conexionViewModel.conexionEstablecida.collectAsState()
    val usersViewModel: UsersViewModel = viewModel()

    val currentUser = auth.currentUser
    var userName by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf("") }
    var profileImageUri by remember { mutableStateOf<String?>(null) }
    var passwordActual by remember { mutableStateOf("") }
    var passwordNueva by remember { mutableStateOf("") }
    var deleteImageInUI by remember { mutableStateOf(false) }

    val pickImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            profileImageUri = it.toString()
            deleteImageInUI = false
        }
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            pickImageLauncher.launch("image/*")
        } else {
            Toast.makeText(
                context,
                "Permission is required to access photos",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val onProfileImageClick = {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            pickImageLauncher.launch("image/*")
        } else {
            requestPermissionLauncher.launch(permission)
        }
    }

    // Cargar datos actuales del usuario
    LaunchedEffect(currentUser) {
        currentUser?.uid?.let { uid ->
            firestore.collection("usuarios").document(uid).get()
                .addOnSuccessListener { document ->
                    userName = document.getString("nombre") ?: ""
                    userEmail = document.getString("email") ?: ""
                    profileImageUri = document.getString("fotoPerfil")
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Error loading data", Toast.LENGTH_SHORT).show()
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Edit Profile",
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
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
                    .verticalScroll(rememberScrollState())
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
                                .clickable { onProfileImageClick() },

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
                                .clickable { onProfileImageClick() },

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
                            profileImageUri = null
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
                            Text("Delete")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                // Campo Nombre
                OutlinedTextField(
                    value = userName,
                    onValueChange = { userName = it },
                    label = { Text("Name", color = Color.White) },
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
                    label = { Text("Email", color = Color(0xFFB0B0B0)) },
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
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Change Password",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = passwordActual,
                    onValueChange = { passwordActual = it },
                    label = { Text("Current Password", color = Color(0xFFB0B0B0)) },
                    textStyle = LocalTextStyle.current.copy(color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Mostrar campo nueva contraseña solo si el usuario ha escrito algo en la contraseña actual
                if (passwordActual.isNotEmpty()) {
                    OutlinedTextField(
                        value = passwordNueva,
                        onValueChange = { passwordNueva = it },
                        label = { Text("New Password", color = Color(0xFFB0B0B0)) },
                        textStyle = LocalTextStyle.current.copy(color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        // Validación: si passwordActual no está vacío pero passwordNueva sí, error
                        if (passwordActual.isNotEmpty() && passwordNueva.isEmpty()) {
                            errorMessage = "You must enter the new password"
                            return@Button
                        } else {
                            errorMessage = null // limpiar error si ya no hay problema
                        }

                        // Validación: si ambas contraseñas coinciden, error
                        if (passwordActual.isNotEmpty() && passwordNueva.isNotEmpty() && passwordActual == passwordNueva) {
                            errorMessage = "The new password cannot be the same as the current password"
                            return@Button
                        }

                        if (!hayConexion) {
                            Toast.makeText(context, "You need an internet connection to update your profile", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        currentUser?.uid?.let { uid ->

                            val userUpdates = mutableMapOf<String, Any?>("nombre" to userName)

                            fun actualizarFirestoreYPopBack() {
                                firestore.collection("usuarios").document(uid)
                                    .update(userUpdates)
                                    .addOnSuccessListener {
                                        if (passwordActual.isNotEmpty()) {
                                            usersViewModel.cambiarContrasena(passwordActual, passwordNueva) { success, mensaje ->
                                                if (success) {
                                                    errorMessage = null
                                                    Toast.makeText(context, "Profile updated and password changed", Toast.LENGTH_SHORT).show()
                                                    navController.popBackStack()
                                                } else {
                                                    errorMessage = mensaje
                                                }
                                            }
                                        } else {
                                            Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
                                            navController.popBackStack()
                                        }
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(context, "Error updating profile", Toast.LENGTH_SHORT).show()
                                    }
                            }

                            if (deleteImageInUI) {
                                userUpdates["fotoPerfil"] = null
                                actualizarFirestoreYPopBack()
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
                                                    actualizarFirestoreYPopBack()
                                                } else {
                                                    Toast.makeText(context, "Error uploading the image", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                            return@Button // upload async
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Error processing the image", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    userUpdates["fotoPerfil"] = imageUri
                                    actualizarFirestoreYPopBack()
                                }
                            } else {
                                actualizarFirestoreYPopBack()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Changes")
                }

                if (!errorMessage.isNullOrEmpty()) {
                    Text(
                        text = errorMessage ?: "",
                        color = Color.Red,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}
