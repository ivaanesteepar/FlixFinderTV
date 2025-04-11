package com.example.flixfindertv.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.flixfindertv.R
import com.example.flixfindertv.models.Usuarios
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

@Composable
fun RegisterScreen(navController: NavHostController) {
    var username by remember { mutableStateOf(TextFieldValue()) }
    var email by remember { mutableStateOf(TextFieldValue()) }
    var password by remember { mutableStateOf(TextFieldValue()) }
    var confirmPassword by remember { mutableStateOf(TextFieldValue()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center) // Centra en ambas direcciones (horizontal y vertical)
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    text = "Register",
                    color = Color.White,
                    fontSize = 24.sp,
                    modifier = Modifier.padding(bottom = 16.dp).align(Alignment.CenterHorizontally)
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username", color = Color.White) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
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
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email", color = Color.White) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
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
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password", color = Color.White) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
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
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password", color = Color.White) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
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
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                errorMessage?.let {
                    Text(text = it, color = Color.Red, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Button(
                    onClick = {
                        if (email.text.isEmpty() || password.text.isEmpty() || username.text.isEmpty()) {
                            errorMessage = "All fields are required"
                        } else if (password.text != confirmPassword.text) {
                            errorMessage = "Passwords do not match"
                        } else {
                            errorMessage = null
                            isLoading = true

                            // Verificar si el nombre de usuario ya existe
                            firestore.collection("usuarios")
                                .whereEqualTo("nombre", username.text)
                                .get()
                                .addOnSuccessListener { result ->
                                    if (result.isEmpty) {
                                        // El nombre de usuario no existe, continuar con el registro
                                        auth.createUserWithEmailAndPassword(
                                            email.text,
                                            password.text
                                        )
                                            .addOnCompleteListener { task ->
                                                isLoading = false
                                                if (task.isSuccessful) {
                                                    val user = auth.currentUser
                                                    user?.let {
                                                        val newUser = Usuarios(
                                                            uid = user.uid,
                                                            nombre = username.text,
                                                            email = email.text,
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
                                                                navController.navigate("login")
                                                            }
                                                            .addOnFailureListener {
                                                                errorMessage =
                                                                    "Error adding user to Firestore"
                                                            }
                                                    }
                                                } else {
                                                    errorMessage =
                                                        "Error: ${task.exception?.message}"
                                                }
                                            }
                                    } else {
                                        // El nombre de usuario ya está en uso
                                        errorMessage = "Username already taken"
                                        isLoading = false
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    errorMessage =
                                        "Error checking username availability: ${exception.message}"
                                    isLoading = false
                                }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(if (isLoading) Modifier.pointerInput(Unit) {} else Modifier),

                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Blue, // El botón siempre será azul
                        contentColor = Color.White  // El texto siempre será blanco
                    )
                ) {
                    Text(text = if (isLoading) "Registering..." else "Register")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Box (
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("Back to login", color = Color.White, modifier = Modifier)
                    }
                }
            }
        }
    }
}
