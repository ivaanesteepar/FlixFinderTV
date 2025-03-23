package com.example.flixfindertv.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.flixfindertv.interfaces.UiState
import com.example.flixfindertv.ui.viewmodels.TriviaViewModel
import com.example.flixfindertv.ui.viewmodels.TriviaViewModelFactory
import com.example.flixfindertv.utils.BottomNavigationBar
import com.google.firebase.auth.FirebaseAuth

@Composable
fun TriviaScreen(
    navController: NavHostController
) {
    val context = LocalContext.current

    // Use the factory here, after context is available
    val triviaViewModel: TriviaViewModel = viewModel(
        factory = TriviaViewModelFactory(context, context as LifecycleOwner)
    )

    var answer by rememberSaveable { mutableStateOf("") }
    var result by rememberSaveable { mutableStateOf("") }
    val uiState by triviaViewModel.uiState.collectAsState()
    val uid = FirebaseAuth.getInstance().currentUser?.uid

    // Estado para mostrar el botón de siguiente pregunta
    var showNextButton by rememberSaveable { mutableStateOf(false) }

    // Estado para controlar si ya se mostró la explicación
    var explanationShown by rememberSaveable { mutableStateOf(false) }

    // Estado para saber si se ha enviado una respuesta
    var hasAnswered by rememberSaveable { mutableStateOf(false) }

    var isAnswered by rememberSaveable { mutableStateOf(false) }

    val languageState by triviaViewModel.languageState.collectAsState()


    LaunchedEffect(true) {
        triviaViewModel.registerLanguageObserver(context as LifecycleOwner)
    }

    // Genera la pregunta automáticamente cuando se entra en la pantalla
    LaunchedEffect(Unit) {
        if (uiState !is UiState.Success) {  // Solo generar si no hay una pregunta guardada
            triviaViewModel.generateQuestion()
        }

        // Si ya se ha mostrado la explicación, no restablecer el botón
        if (!explanationShown) {
            showNextButton = false  // Asegúrate de ocultar el botón al principio
        }
    }

    // Cambiar la ubicación de la declaración de `speechResultLauncher`
    val speechResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.let {
                answer = it[0]
            }
        }
    }

    val speechIntent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
        }
    }

    // Lanzador de permisos para la grabación de audio
    val recordAudioPermissionRequest = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            // Permiso concedido, puedes continuar con la grabación de audio
            speechResultLauncher.launch(speechIntent) // Inicia el reconocimiento de voz
        } else {
            // Permiso denegado, puedes mostrar un mensaje o hacer algo al respecto
        }
    }

    Scaffold(
        bottomBar = {
            if (uid != null) {
                BottomNavigationBar(navController, uid)
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Mostramos la pregunta solo si está disponible
                if (uiState is UiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else if (uiState is UiState.Success) {
                    val question = (uiState as UiState.Success).outputText
                    Text(
                        text = question,
                        textAlign = TextAlign.Start,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(500.dp)  // Deja que la pregunta ocupe el espacio restante
                            .padding(bottom = 16.dp)
                            .verticalScroll(rememberScrollState())  // Habilita desplazamiento solo para la pregunta
                    )
                } else if (uiState is UiState.Error) {
                    Text(
                        text = (uiState as UiState.Error).errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                }
            }

            // Campo de texto y botones fijos en la parte inferior
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth() // Asegúrate de que la columna ocupe el ancho completo
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Fila con el campo de texto y el icono del micrófono dentro del TextField
                    TextField(
                        value = answer,
                        label = { Text("Write your response") },
                        onValueChange = { answer = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .heightIn(min = 56.dp)
                            .border(1.dp, Color.Gray),
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    // Verificar si el permiso de grabar audio está concedido
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                        // Si el permiso ya ha sido concedido, lanzar el reconocimiento de voz
                                        speechResultLauncher.launch(speechIntent)
                                    } else {
                                        // Solicitar el permiso
                                        recordAudioPermissionRequest.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            ) {
                                Icon(imageVector = Icons.Filled.Mic, contentDescription = "Voice Input")
                            }
                        }
                    )
                }

                // Botón para enviar respuesta
                Button(
                    onClick = {
                        val normalizedAnswer = answer.lowercase().trim() // Convertir a minúsculas y eliminar espacios
                        if (normalizedAnswer in listOf("a", "b", "c", "d")) {
                            triviaViewModel.checkAnswer(normalizedAnswer)
                            showNextButton = true  // Muestra el botón de siguiente pregunta después de responder
                            answer = "" // Limpia el campo de respuesta
                            hasAnswered = true // Marca que ya se ha respondido
                            isAnswered = true
                        } else {
                            result = "Invalid answer. Please enter a, b, c, or d."
                        }
                    },
                    enabled = !isAnswered && answer.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Text(text = "Send response")
                }

                // Mostrar mensaje de error si la respuesta es inválida
                if (result.isNotEmpty()) {
                    Text(
                        text = result,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // Mostrar el resultado de la respuesta solo si se ha respondido
                if (hasAnswered) {
                    explanationShown = true
                }

                // Botón de siguiente pregunta
                if (showNextButton) {
                    Button(
                        onClick = {
                            triviaViewModel.generateQuestion()  // Genera una nueva pregunta
                            showNextButton = false  // Oculta el botón de siguiente pregunta
                            result = ""  // Limpia el resultado anterior
                            explanationShown = false // Resetear el estado de explicación mostrada
                            hasAnswered = false  // Resetear el estado de respuesta
                            isAnswered = false // Reiniciar el estado de respuesta enviada
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Next question")
                    }
                }
            }
        }
    }
}
