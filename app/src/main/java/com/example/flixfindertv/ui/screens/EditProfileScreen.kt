package com.example.flixfindertv.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
    var userBirthdate by remember { mutableStateOf("") }
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
            }
        }

    // Función para abrir el DatePicker
    val openDatePicker = {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = android.app.DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                // Formato de la fecha: día/mes/año
                userBirthdate = "${selectedDayOfMonth}/${selectedMonth + 1}/${selectedYear}"
            },
            year, month, dayOfMonth
        )
        datePickerDialog.show()
    }

    // Cargar datos actuales del usuario
    LaunchedEffect(currentUser) {
        currentUser?.uid?.let { uid ->
            firestore.collection("usuarios").document(uid).get()
                .addOnSuccessListener { document ->
                    userName = document.getString("nombre") ?: ""
                    userEmail = document.getString("email") ?: ""  // Aquí cargamos el correo actual
                    newEmail = userEmail  // Asignamos el correo actual a newEmail
                    userBirthdate = document.getString("fechaNacimiento") ?: ""
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
                title = { Text("Editar Perfil") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
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
                            .clip(CircleShape)
                            .clickable { pickImageLauncher.launch("image/*") },
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = rememberAsyncImagePainter(profileImageUri),
                        contentDescription = "Imagen de perfil",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
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
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo Correo Electrónico (solo lectura)
            OutlinedTextField(
                value = userEmail,
                onValueChange = {},
                label = { Text("Correo Electrónico") },
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
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Campo Fecha de Nacimiento
            OutlinedTextField(
                value = userBirthdate,
                onValueChange = { userBirthdate = it },
                label = { Text("Fecha de Nacimiento") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                trailingIcon = {
                    IconButton(onClick = { openDatePicker() }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Seleccionar Fecha")
                    }
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Botón Guardar Cambios
            Button(
                onClick = {
                    currentUser?.uid?.let { uid ->
                        // Actualizar los datos del usuario (nombre, fecha de nacimiento)
                        val userUpdates = mutableMapOf<String, Any?>("nombre" to userName)
                        if (userBirthdate.isNotEmpty()) {
                            userUpdates["fechaNacimiento"] = userBirthdate
                        }

                        // Verificar si se debe eliminar la foto de perfil
                        if (deleteImageInUI) {
                            // Si se marcó para eliminar la foto, actualizamos el campo fotoPerfil en Firestore como null
                            userUpdates["fotoPerfil"] = null
                        } else if (!profileImageUri.isNullOrEmpty()) {
                            // Subir imagen si se ha seleccionado una nueva imagen
                            val imageUri = profileImageUri
                            val imageBytes = imageUri?.let { uri ->
                                val inputStream = context.contentResolver.openInputStream(android.net.Uri.parse(uri))
                                inputStream?.readBytes()
                            }

                            // Llamar a la función para subir la imagen a Imgur
                            if (imageBytes != null) {
                                ImgurUploader.uploadImage(imageBytes) { imageUrl ->
                                    if (imageUrl != null) {
                                        println("Imagen subida exitosamente: $imageUrl")
                                        userUpdates["fotoPerfil"] = imageUrl
                                        firestore.collection("usuarios").document(uid).update(userUpdates)
                                            .addOnSuccessListener {
                                                Toast.makeText(context, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                                                navController.popBackStack()
                                            }
                                            .addOnFailureListener { exception ->
                                                println("Error al actualizar los datos: ${exception.message}")
                                                Toast.makeText(context, "Error al actualizar", Toast.LENGTH_SHORT).show()
                                            }
                                    } else {
                                        println("Error al subir la imagen: La URL de la imagen es null")
                                        Toast.makeText(context, "Error al subir la imagen", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                return@Button
                            }
                        }

                        // Si no se seleccionó una nueva imagen ni se marcó para eliminar la foto
                        // Solo actualizamos los datos sin modificar la foto de perfil
                        if (userUpdates.isNotEmpty()) {
                            firestore.collection("usuarios").document(uid).update(userUpdates)
                                .addOnSuccessListener {
                                    // Mostrar el mensaje de éxito
                                    Toast.makeText(context, "Perfil actualizado", Toast.LENGTH_SHORT).show()

                                    // Realizar el popBackStack después de actualizar los datos
                                    navController.popBackStack()
                                }
                                .addOnFailureListener {
                                    // Mostrar el mensaje de error
                                    Toast.makeText(context, "Error al actualizar", Toast.LENGTH_SHORT).show()
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
