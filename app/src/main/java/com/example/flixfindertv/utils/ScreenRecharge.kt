package com.example.flixfindertv.utils

import com.example.flixfindertv.ui.viewmodels.ConexionViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun ScreenRecharge(conexionViewModel: ConexionViewModel, onRecargar: () -> Unit) {
    val conexionEstablecida by conexionViewModel.conexionEstablecida.collectAsState()
    var isRecargando by remember { mutableStateOf(false) }
    var showContent by remember { mutableStateOf(false) }
    var showProgressIndicator by remember { mutableStateOf(false) }
    var recargaIniciada by remember { mutableStateOf(false) }

    // LaunchedEffect para controlar el retraso en la aparición del contenido
    LaunchedEffect(Unit) {
        delay(1000) // Retraso de 1 segundo antes de mostrar el contenido
        showContent = true
    }

    // LaunchedEffect para manejar el indicador de progreso durante 3 segundos
    LaunchedEffect(recargaIniciada) {
        if (recargaIniciada) {
            delay(3000) // Esperamos 3 segundos
            showProgressIndicator = false // Ocultamos el indicador de progreso después de 3 segundos
            recargaIniciada = false // Reseteamos el estado de recarga
        }
    }

    // Controlamos que la pantalla no cambie hasta que la conexión se restablezca
    LaunchedEffect(conexionEstablecida) {
        if (conexionEstablecida && !isRecargando && !recargaIniciada) {
            // Inicia la recarga cuando la conexión se establece
            isRecargando = true
            recargaIniciada = true
            showProgressIndicator = true
        }
    }

    println("modificación de conexion: $conexionEstablecida")

    if (!conexionEstablecida) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                if (showContent) {
                    Text(
                        text = "No hay conexión a Internet",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }

    // Si se está recargando, verificamos la conexión
    if (isRecargando && conexionEstablecida) {
        LaunchedEffect(conexionEstablecida) {
            // Solo recargamos cuando la conexión se establece
            onRecargar()
            isRecargando = false
            showProgressIndicator = false // Ocultamos el indicador cuando la recarga finaliza
        }
    }
}
