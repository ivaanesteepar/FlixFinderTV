package com.example.flixfindertv.ui.screens

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.flixfindertv.R
import com.example.flixfindertv.ui.viewmodels.ConexionViewModel
import com.example.flixfindertv.ui.viewmodels.UsersViewModel

@Composable
fun RegisterScreen(navController: NavController) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val usersViewModel: UsersViewModel = viewModel()
    val conexionViewModel: ConexionViewModel = viewModel()
    val hayConexion by conexionViewModel.conexionEstablecida.collectAsState()

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

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
        if (!isLandscape) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(bottom = 16.dp)
                ) {
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
                        text = "Register",
                        color = Color.White,
                        fontSize = 24.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                            .align(Alignment.CenterHorizontally)
                    )

                    // Llamamos a la función para los campos de texto
                    InputField(
                        label = "Username",
                        value = username,
                        onValueChange = { username = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                    InputField(
                        label = "Email",
                        value = email,
                        onValueChange = { email = it },
                        keyboardType = KeyboardType.Email,
                        modifier = Modifier.fillMaxWidth()
                    )
                    InputField(
                        label = "Password",
                        value = password,
                        onValueChange = { password = it },
                        isPassword = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    InputField(
                        label = "Confirm Password",
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        isPassword = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    errorMessage?.let {
                        Text(text = it, color = Color.Red, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Button(
                        onClick = {
                            if (!hayConexion) {
                                Toast.makeText(
                                    context,
                                    "You need an internet connection to log in",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                isLoading = true
                                usersViewModel.register(
                                    email = email,
                                    password = password,
                                    confirmPassword = confirmPassword,
                                    username = username,
                                    onSuccess = { screen ->
                                        isLoading = false
                                        navController.navigate(screen)
                                    },
                                    onFailure = { error ->
                                        isLoading = false
                                        errorMessage = error
                                    }
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(if (isLoading) Modifier.pointerInput(Unit) {} else Modifier),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Blue,
                            contentColor = Color.White
                        )
                    ) {
                        Text(text = if (isLoading) "Registering..." else "Register")
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        TextButton(onClick = { navController.popBackStack() }) {
                            Text("Back to login", color = Color.White)
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(170.dp)
                                .clip(CircleShape)
                                .padding(start = 26.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(1f),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Register",
                                color = Color.White,
                                fontSize = 24.sp,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            // Llamamos a la función para los campos de texto con ancho de 500 dp en landscape
                            InputField(
                                label = "Username",
                                value = username,
                                onValueChange = { username = it },
                                modifier = Modifier.width(500.dp)
                            )
                            InputField(
                                label = "Email",
                                value = email,
                                onValueChange = { email = it },
                                keyboardType = KeyboardType.Email,
                                modifier = Modifier.width(500.dp)
                            )
                            InputField(
                                label = "Password",
                                value = password,
                                onValueChange = { password = it },
                                isPassword = true,
                                modifier = Modifier.width(500.dp)
                            )
                            InputField(
                                label = "Confirm Password",
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                isPassword = true,
                                modifier = Modifier.width(500.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    if (!hayConexion) {
                                        Toast.makeText(
                                            context,
                                            "You need an internet connection to log in",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        isLoading = true
                                        usersViewModel.register(
                                            email = email,
                                            password = password,
                                            confirmPassword = confirmPassword,
                                            username = username,
                                            onSuccess = { screen ->
                                                isLoading = false
                                                navController.navigate(screen)
                                            },
                                            onFailure = { error ->
                                                isLoading = false
                                                errorMessage = error
                                            }
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .width(300.dp)
                                    .then(if (isLoading) Modifier.pointerInput(Unit) {} else Modifier),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Blue,
                                    contentColor = Color.White
                                )
                            ) {
                                Text(text = if (isLoading) "Registering..." else "Register")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                TextButton(onClick = { navController.popBackStack() }) {
                                    Text("Back to login", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it) },
        label = { Text(label, color = Color.White) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        textStyle = LocalTextStyle.current.copy(color = Color.White),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.White,
            unfocusedBorderColor = Color.White,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = Color.White
        ),
        singleLine = true,
        modifier = modifier
    )
    Spacer(modifier = Modifier.height(16.dp))
}

