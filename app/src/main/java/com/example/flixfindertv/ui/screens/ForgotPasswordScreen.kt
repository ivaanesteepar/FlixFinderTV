package com.example.flixfindertv.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.flixfindertv.R
import com.google.firebase.auth.FirebaseAuth

// Pantalla para restablecer la contraseña mediante correo electrónico
@Composable
fun ForgotPasswordScreen(navController: NavHostController) {
    var email by remember { mutableStateOf(TextFieldValue()) }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

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
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(100.dp) // Tamaño de la imagen
                        .clip(CircleShape) // Hace que la imagen sea circular
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Recover Password",
                    color = Color.White,
                    fontSize = 24.sp,
                    modifier = Modifier.padding(bottom = 16.dp).align(Alignment.CenterHorizontally)
                )

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

                Button(
                    onClick = {
                        if (email.text.isEmpty()) {
                            Toast.makeText(context, "Enter your email", Toast.LENGTH_SHORT).show()
                        } else {
                            isLoading = true
                            auth.sendPasswordResetEmail(email.text)
                                .addOnCompleteListener { task ->
                                    isLoading = false
                                    if (task.isSuccessful) {
                                        Toast.makeText(
                                            context,
                                            "Recovery email sent",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        navController.popBackStack()
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Error: ${task.exception?.message}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
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
                    Text(if (isLoading) "Sending..." else "Send recovery link")
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
