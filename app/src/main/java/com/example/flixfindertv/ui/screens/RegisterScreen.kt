package com.example.flixfindertv.ui.screens

import android.content.res.Configuration
import android.widget.Toast
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
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
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.fondo_app),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            if (!isLandscape) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Logo(size = 120.dp)
                    Spacer(modifier = Modifier.height(16.dp))
                    RegisterForm(
                        username, email, password, confirmPassword,
                        onUsernameChange = { username = it },
                        onEmailChange = { email = it },
                        onPasswordChange = { password = it },
                        onConfirmPasswordChange = { confirmPassword = it },
                        modifier = Modifier.fillMaxWidth(),
                        errorMessage = errorMessage,
                        isLoading = isLoading,
                        onRegisterClick = {
                            if (!hayConexion) {
                                Toast.makeText(context, "You need an internet connection to register", Toast.LENGTH_SHORT).show()
                            } else {
                                isLoading = true
                                usersViewModel.register(
                                    email, password, confirmPassword, username,
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
                        onBackClick = { navController.popBackStack() }
                    )
                }
            } else {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Logo(size = 170.dp, modifier = Modifier.padding(start = 26.dp))
                        Spacer(modifier = Modifier.width(32.dp))
                        RegisterForm(
                            username, email, password, confirmPassword,
                            onUsernameChange = { username = it },
                            onEmailChange = { email = it },
                            onPasswordChange = { password = it },
                            onConfirmPasswordChange = { confirmPassword = it },
                            modifier = Modifier.width(500.dp),
                            errorMessage = errorMessage,
                            isLoading = isLoading,
                            onRegisterClick = {
                                if (!hayConexion) {
                                    Toast.makeText(context, "You need an internet connection to log in", Toast.LENGTH_SHORT).show()
                                } else {
                                    isLoading = true
                                    usersViewModel.register(
                                        email, password, confirmPassword, username,
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
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RegisterForm(
    username: String,
    email: String,
    password: String,
    confirmPassword: String,
    onUsernameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    modifier: Modifier,
    errorMessage: String?,
    isLoading: Boolean,
    onRegisterClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Register", color = Color.White, fontSize = 24.sp, modifier = Modifier.padding(bottom = 16.dp))
        InputField("Username", username, onUsernameChange, modifier = modifier)
        InputField("Email", email, onEmailChange, keyboardType = KeyboardType.Email, modifier = modifier)
        InputField("Password", password, onPasswordChange, isPassword = true, modifier = modifier)
        InputField("Confirm Password", confirmPassword, onConfirmPasswordChange, isPassword = true, modifier = modifier)

        errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = it, color = Color.Red, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRegisterClick,
            modifier = modifier.then(if (isLoading) Modifier.pointerInput(Unit) {} else Modifier),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Blue,
                contentColor = Color.White
            )
        ) {
            Text(if (isLoading) "Registering..." else "Register")
        }

        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onBackClick) {
            Text("Back to login", color = Color.White)
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
        onValueChange = onValueChange,
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

@Composable
fun Logo(size: Dp, modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.app_logo),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier.size(size).clip(CircleShape)
    )
}
