package com.ivaanesteepar.flixfindertv.ui.screens

import android.content.res.Configuration
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ivaanesteepar.flixfindertv.R
import com.ivaanesteepar.flixfindertv.ui.viewmodels.ConexionViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ForgotPasswordScreen(navController: NavHostController) {
    var email by remember { mutableStateOf(TextFieldValue()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val conexionViewModel: ConexionViewModel = viewModel()
    val hayConexion by conexionViewModel.conexionEstablecida.collectAsState()
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    fun handlePasswordReset() {
        if (!hayConexion) {
            Toast.makeText(context, "You need an internet connection to log in", Toast.LENGTH_SHORT).show()
        } else if (email.text.isEmpty()) {
            errorMessage = "Enter your email"
        } else {
            isLoading = true
            errorMessage = ""
            auth.sendPasswordResetEmail(email.text)
                .addOnCompleteListener { task ->
                    isLoading = false
                    if (task.isSuccessful) {
                        Toast.makeText(context, "Recovery email sent", Toast.LENGTH_LONG).show()
                        navController.popBackStack()
                    } else {
                        Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.fondo_app),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        if (!isLandscape) {
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
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
                    text = "Recover Password",
                    color = Color.White,
                    fontSize = 24.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(16.dp))
                EmailInputField(value = email, onValueChange = { email = it })
                ErrorText(errorMessage)
                Spacer(modifier = Modifier.height(16.dp))
                SendButton(isLoading, ::handlePasswordReset)
                Spacer(modifier = Modifier.height(8.dp))
                BackToLoginTextBox { navController.popBackStack() }
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(170.dp)
                        .clip(CircleShape)
                        .padding(start = 36.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxHeight().weight(1f)
                ) {
                    Text("Recover Password", color = Color.White, fontSize = 24.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    EmailInputField(email, { email = it }, Modifier.width(500.dp))
                    ErrorText(errorMessage)
                    Spacer(modifier = Modifier.height(16.dp))
                    SendButton(isLoading, ::handlePasswordReset, Modifier.width(500.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    BackToLoginTextBox { navController.popBackStack() }
                }
            }
        }
    }
}

@Composable
fun EmailInputField(value: TextFieldValue, onValueChange: (TextFieldValue) -> Unit, modifier: Modifier = Modifier.fillMaxWidth()) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Email", color = Color.White) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
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

@Composable
fun ErrorText(message: String) {
    if (message.isNotEmpty()) {
        Text(
            text = message,
            color = Color.Red,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun SendButton(isLoading: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier.fillMaxWidth()) {
    Button(
        onClick = onClick,
        modifier = modifier.then(if (isLoading) Modifier.pointerInput(Unit) {} else Modifier),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Blue,
            contentColor = Color.White
        )
    ) {
        Text(if (isLoading) "Sending..." else "Send recovery link")
    }
}

@Composable
fun BackToLoginTextBox(onClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        TextButton(onClick = onClick) {
            Text("Back to login", color = Color.White)
        }
    }
}


