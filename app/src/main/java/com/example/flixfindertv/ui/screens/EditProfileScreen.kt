package com.example.flixfindertv.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Edit
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
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.flixfindertv.R
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

    // Launcher to pick an image from gallery
    val pickImageLauncher: ActivityResultLauncher<String> =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            profileImageUri = uri?.toString()
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
                // Mostrar imagen de perfil si está disponible
                if (profileImageUri.isNullOrEmpty()) {
                    // Si no hay imagen, mostrar la imagen predeterminada desde drawable
                    Image(
                        painter = rememberAsyncImagePainter(R.drawable.no_profile_icon),
                        contentDescription = "Foto de perfil",
                        modifier = Modifier
                            .fillMaxSize() // Asegura que la imagen ocupe todo el espacio disponible
                            .clip(CircleShape), // Mantiene la forma circular
                        contentScale = ContentScale.Crop // Recorta la imagen para ajustarse al círculo sin deformarla
                    )
                } else {
                    // Si hay una imagen, mostrar la imagen de la URI
                    Image(
                        painter = rememberAsyncImagePainter(profileImageUri),
                        contentDescription = "Imagen de perfil",
                        modifier = Modifier
                            .fillMaxSize() // Asegura que la imagen ocupe todo el espacio disponible
                            .clip(CircleShape), // Mantiene la forma circular
                        contentScale = ContentScale.Crop // Recorta la imagen para ajustarse al círculo sin deformarla
                    )
                }

                // Botón para seleccionar imagen
                IconButton(
                    onClick = { pickImageLauncher.launch("image/*") },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(36.dp)
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = "Editar foto de perfil")
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
                        val userUpdates = mutableMapOf<String, Any>("nombre" to userName)
                        if (userBirthdate.isNotEmpty()) {
                            userUpdates["fechaNacimiento"] = userBirthdate
                        }
                        if (profileImageUri != null) {
                            userUpdates["fotoPerfil"] = profileImageUri!!
                        }
                        firestore.collection("usuarios").document(uid).update(userUpdates)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Error al actualizar", Toast.LENGTH_SHORT).show()
                            }
                        navController.popBackStack()

                        // Si el usuario cambió el correo, autenticarse antes de actualizarlo
                        if (showPasswordField && newEmail.isNotEmpty() && newEmail != userEmail) {
                            // Si el correo no está verificado, enviamos un correo de verificación
                            if (!currentUser.isEmailVerified) {
                                currentUser.sendEmailVerification()
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Correo de verificación enviado. Verifica tu correo antes de continuar.", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { exception ->
                                        Toast.makeText(context, "Error al enviar correo de verificación: ${exception.message}", Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                // Si el correo está verificado, reautenticamos al usuario y actualizamos el correo
                                val credential = EmailAuthProvider.getCredential(userEmail, password)
                                currentUser.reauthenticate(credential)
                                    .addOnSuccessListener {
                                        currentUser.updateEmail(newEmail)
                                            .addOnSuccessListener {
                                                firestore.collection("usuarios").document(uid)
                                                    .update("email", newEmail)
                                                Toast.makeText(context, "Correo actualizado", Toast.LENGTH_SHORT).show()
                                            }
                                            .addOnFailureListener { exception ->
                                                Toast.makeText(context, "Error al actualizar el correo: ${exception.message}", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                    .addOnFailureListener { exception ->
                                        Toast.makeText(context, "Contraseña incorrecta: ${exception.message}", Toast.LENGTH_SHORT).show()
                                    }
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
