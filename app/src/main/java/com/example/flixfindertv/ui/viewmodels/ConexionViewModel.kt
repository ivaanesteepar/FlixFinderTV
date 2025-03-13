package com.example.flixfindertv.ui.viewmodels

import android.app.Application
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State

class ConexionViewModel(application: Application) : AndroidViewModel(application) {

    private val _conexionEstablecida = mutableStateOf(false)
    val conexionEstablecida: State<Boolean> get() = _conexionEstablecida

    suspend fun isOnline(): Boolean {
        val connectivityManager = getApplication<Application>().getSystemService(Application.CONNECTIVITY_SERVICE) as ConnectivityManager

        // Obtener las capacidades de la red activa
        val activeNetwork = connectivityManager.activeNetwork
        println("activeNetwork: $activeNetwork")
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        println("networkCapabilities: $networkCapabilities")

        // Verificar si está conectada a una red con acceso a internet
        val isConnected = networkCapabilities != null &&
                (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ||
                        networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED))

        println("isConnected: $isConnected")
        // Si hay conexión, comprobar si se puede acceder a internet
        return if (isConnected) {
            val internetAvailable = isInternetAvailable()
            _conexionEstablecida.value = internetAvailable
            internetAvailable
        } else {
            _conexionEstablecida.value = false
            false
        }
    }

    private suspend fun isInternetAvailable(): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                // Hacer una prueba con una URL más confiable
                val url = URL("https://www.google.com") // Usar HTTP para mayor compatibilidad
                val connection = url.openConnection() as HttpURLConnection
                println("connection: $connection")
                connection.connectTimeout = 2000 // Timeout de 2 segundos
                connection.readTimeout = 2000 // Timeout de 2 segundos

                try {
                    connection.connect() // Intentar la conexión
                    val responseCode = connection.responseCode
                    println("responseCode: $responseCode")
                    responseCode == HttpURLConnection.HTTP_OK // Verificar si la respuesta fue OK
                } catch (e: Exception) {
                    println("Hay error de conexión: ${e.message}")  // Imprime el mensaje completo de la excepción
                    e.printStackTrace()  // Esto te mostrará la traza completa del error
                    false // Si hay un error de conexión, devolver false
                }
            }
        } catch (e: Exception) {
            false // Si no se puede realizar la prueba, devolver false
        }
    }
}
