package com.ivaanesteepar.flixfindertv.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ivaanesteepar.flixfindertv.R
import com.ivaanesteepar.flixfindertv.ui.viewmodels.UsersViewModel
import com.ivaanesteepar.flixfindertv.utils.DateOfBirth.EditableDatePicker

@Composable
fun NewQuestionsScreen(navController: NavHostController) {
    val generos = listOf(
        "Action", "Adventure", "Animation",
        "Comedy", "Crime", "Family",
        "Fantasy", "History", "Horror",
        "Kids", "Music", "Mystery",
        "Reality", "Romance", "Science Fiction",
        "Soap", "Talk", "Thriller",
        "Western"
    )

    var generosSeleccionados by remember { mutableStateOf<List<String>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var mostrarFechaNacimiento by remember { mutableStateOf(false) }
    var fechaNacimiento by remember { mutableStateOf("") }

    val viewModel: UsersViewModel = viewModel()

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.fondo_app),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        if (!mostrarFechaNacimiento) {
            // Pantalla selección de géneros
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(top = 56.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "1/2",
                    color = Color.White,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Which are your favourite genres?",
                    fontSize = 20.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(32.dp))

                generos.chunked(3).forEach { filaGeneros ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        filaGeneros.forEach { genero ->
                            Box(modifier = Modifier.weight(1f)) {
                                GeneroCard(
                                    genero = genero,
                                    isSelected = generosSeleccionados.contains(genero),
                                    onClick = {
                                        if (generosSeleccionados.contains(genero)) {
                                            generosSeleccionados = generosSeleccionados - genero
                                            errorMessage = null
                                        } else if (generosSeleccionados.size < 2) {
                                            generosSeleccionados = generosSeleccionados + genero
                                            errorMessage = null
                                        } else {
                                            errorMessage = "You can only select up to 2 genres"
                                        }
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                errorMessage?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = it, color = Color.Red, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (generosSeleccionados.size == 2) {
                            mostrarFechaNacimiento = true
                        } else {
                            errorMessage = "Please select 2 genres"
                        }
                    },
                    enabled = generosSeleccionados.isNotEmpty()
                ) {
                    Text(text = "Continue")
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(top = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { mostrarFechaNacimiento = false }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "2/2",
                    color = Color.White,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "When is your birthday?",
                    fontSize = 20.sp,
                    color = Color.White
                )

                EditableDatePicker(
                    fecha = fechaNacimiento,
                    onFechaChange = { fechaNacimiento = it },
                    error = errorMessage,
                    onContinue = {
                        viewModel.updateFechaNacimiento(fechaNacimiento)
                        viewModel.newUsersFunction(
                            generosSeleccionados = generosSeleccionados,
                            onSuccess = { navController.navigate("home") },
                            onError = { error -> errorMessage = error }
                        )
                    },
                    setError = { errorMessage = it }
                )
            }
        }
    }
}

@Composable
fun GeneroCard(genero: String, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFFFA500) else Color.LightGray
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = genero, fontSize = 12.sp, color = Color.Black)
        }
    }
}
