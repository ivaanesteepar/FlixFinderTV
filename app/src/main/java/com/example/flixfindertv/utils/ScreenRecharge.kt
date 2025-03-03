package com.example.flixfindertv.utils

import com.example.flixfindertv.ui.viewmodels.ConexionViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
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
    val conexionEstablecida by conexionViewModel.conexionEstablecida
    var isRecargando by remember { mutableStateOf(false) }
    var showContent by remember { mutableStateOf(false) }

    // LaunchedEffect para controlar el retraso
    LaunchedEffect(Unit) {
        delay(1000) // Retraso de 1 segundo antes de mostrar el contenido
        showContent = true
    }

    // LaunchedEffect para controlar la recarga cuando la conexión esté disponible
    LaunchedEffect(conexionEstablecida) {
        if (conexionEstablecida) {
            onRecargar() // Recargamos los datos cuando la conexión se restablece
        }
    }

    println("modificacion de conexion: $conexionEstablecida")

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
                // Solo mostrar el contenido después del retraso
                if (showContent) {
                    Text(
                        text = "No hay conexión a Internet",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            // Cambiamos el estado de recarga a true cuando el botón es presionado
                            isRecargando = true
                            println("recargando: $isRecargando")
                        },
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(text = "Recargar")
                    }

                    // Mostrar círculo de carga cuando isRecargando es true
                    if (isRecargando) {
                        Spacer(modifier = Modifier.height(16.dp))
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(50.dp)
                                .padding(8.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }

    // Si se está recargando, verificamos la conexión
    if (isRecargando) {
        // Usamos un LaunchedEffect que depende de isRecargando
        LaunchedEffect(isRecargando) {
            println("llega aqui")
            if (conexionViewModel.isOnline()) {
                onRecargar() // Recargamos los datos
                isRecargando = false // Terminamos el proceso de recarga
            }
            println("isRec: $isRecargando")
            isRecargando = false
        }
    }
}

