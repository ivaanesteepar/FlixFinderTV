package com.example.flixfindertv.ui.screens

import android.app.Activity
import android.content.res.Configuration
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.flixfindertv.R
import com.example.flixfindertv.ui.viewmodels.ConexionViewModel
import com.example.flixfindertv.ui.viewmodels.UsersViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf(TextFieldValue()) }
    var password by remember { mutableStateOf(TextFieldValue()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val usersViewModel: UsersViewModel = viewModel()
    val context = LocalContext.current
    val activity = context as? Activity
    val conexionViewModel: ConexionViewModel = viewModel()
    val hayConexion by conexionViewModel.conexionEstablecida.collectAsState()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Manejo del botón de retroceso para cerrar la actividad
    BackHandler {
        activity?.finish()
    }

    // Función para realizar el login
    fun loginAction() {
        if (!hayConexion) {
            Toast.makeText(context, "Se necesita una conexión a internet para iniciar sesión", Toast.LENGTH_SHORT).show()
        } else {
            isLoading = true
            usersViewModel.login(email.text, password.text,
                onSuccess = { screen ->
                    isLoading = false
                    navController.navigate(screen)
                    usersViewModel.saveSession(
                        context,
                        true,
                        FirebaseAuth.getInstance().currentUser?.uid ?: ""
                    )
                },
                onFailure = { error ->
                    isLoading = false
                    errorMessage = error
                }
            )
        }
    }

    // Caja principal que contiene la imagen de fondo y los elementos de la UI
    Box(modifier = Modifier.fillMaxSize()) {
        // Imagen de fondo
        Image(
            painter = painterResource(id = R.drawable.fondo_app),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Componente que contiene los campos de texto del login
        val textFields = @Composable {
            LoginTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                keyboardType = KeyboardType.Text,
                modifier = if (isLandscape) Modifier.width(500.dp) else Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            LoginTextField(
                value = password,
                onValueChange = { password = it },
                label = "Contraseña",
                keyboardType = KeyboardType.Password,
                visualTransformation = PasswordVisualTransformation(),
                modifier = if (isLandscape) Modifier.width(500.dp) else Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            errorMessage?.let {
                Text(text = it, color = Color.Red, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
            }
            Button(
                onClick = { loginAction() },
                modifier = Modifier
                    .then(if (isLandscape) Modifier.width(300.dp) else Modifier.fillMaxWidth())
                    .then(if (isLoading) Modifier.pointerInput(Unit) {} else Modifier),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Blue,
                    contentColor = Color.White
                )
            ) {
                Text(text = if (isLoading) "Iniciando sesión..." else "Iniciar sesión")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = { navController.navigate("forgot_password") }) {
                    Text("¿Olvidaste tu contraseña?", color = Color.White)
                }
                TextButton(onClick = { navController.navigate("register") }) {
                    Text("¿No tienes cuenta? Regístrate", color = Color.White)
                }
            }
        }

        // Componente para la orientación vertical (Portrait)
        if (!isLandscape) {
            Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                Column(verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Iniciar sesión",
                        color = Color.White,
                        fontSize = 24.sp,
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                    textFields()
                }
            }
            // Footer en modo Portrait (vertical), colocado en la parte inferior
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 50.dp)
            ) {
                Text(
                    text = "App desarrollada por Iván Estépar © 2025",
                    color = Color.LightGray,
                    fontSize = 15.sp,
                    fontStyle = FontStyle.Italic,
                    lineHeight = 14.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        } else {
            // Componente para la orientación horizontal (Landscape)
            Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Logo alineado a la izquierda en modo landscape
                        Image(
                            painter = painterResource(id = R.drawable.app_logo),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(170.dp)
                                .clip(CircleShape)
                                .padding(start = 26.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(1f),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Iniciar sesión",
                                color = Color.White,
                                fontSize = 24.sp,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            // Campos de texto del login
                            CompositionLocalProvider(LocalTextStyle provides LocalTextStyle.current.copy(fontSize = 16.sp)) {
                                CompositionLocalProvider(LocalContentColor provides Color.White) {
                                    textFields()
                                }
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            // Footer en modo Landscape (horizontal), colocado en la parte inferior
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.CenterHorizontally)
                                    .padding(top = 20.dp) // Espacio entre los campos de texto y el footer
                            ) {
                                Text(
                                    text = "App desarrollada por Iván Estépar © 2025",
                                    color = Color.LightGray,
                                    fontSize = 15.sp,
                                    fontStyle = FontStyle.Italic,
                                    lineHeight = 14.sp,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Componente de campo de texto personalizado
@Composable
fun LoginTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    label: String,
    keyboardType: KeyboardType,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color.White) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = visualTransformation,
        textStyle = LocalTextStyle.current.copy(color = Color.White),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.White,
            unfocusedBorderColor = Color.White,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = Color.White,
            focusedTrailingIconColor = Color.White,
            unfocusedTrailingIconColor = Color.White
        ),
        singleLine = true,
        modifier = modifier
    )
}

