package com.example.flixfindertv.ui.viewmodels

import android.app.Application
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State

class ConexionViewModel(application: Application) : AndroidViewModel(application) {

    // Usamos un MutableState para manejar el estado de la conexión
    private val _conexionEstablecida = mutableStateOf(false)
    val conexionEstablecida: State<Boolean> get() = _conexionEstablecida

    // Función suspendida para verificar si hay conexión a Internet
    suspend fun isOnline(): Boolean {
        val connectivityManager = getApplication<Application>().getSystemService(Application.CONNECTIVITY_SERVICE) as ConnectivityManager

        // Para dispositivos con Android 10 (API 29) o superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val activeNetwork = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            val isConnected = networkCapabilities != null &&
                    (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ||
                            networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED))

            // Verificamos si realmente tenemos acceso a Internet
            if (isConnected) {
                val internetAvailable = isInternetAvailable()
                println("internet: $internetAvailable")
                _conexionEstablecida.value = internetAvailable  // Actualizamos el estado
                println("conexion establecida: ${_conexionEstablecida.value}")
                return internetAvailable
            }
            _conexionEstablecida.value = false  // No hay conexión
            return false
        } else {
            // Para dispositivos con versiones inferiores a Android 10
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            val isConnected = activeNetworkInfo != null && activeNetworkInfo.isConnected

            if (isConnected) {
                val internetAvailable = isInternetAvailable()
                _conexionEstablecida.value = internetAvailable  // Actualizamos el estado
                return internetAvailable
            }
            _conexionEstablecida.value = false  // No hay conexión
            return false
        }
    }

    // Verificación de acceso a Internet mediante HTTP
    private suspend fun isInternetAvailable(): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                val url = URL("https://www.google.com")
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 2000 // Timeout de 2 segundos
                connection.readTimeout = 2000 // Timeout de 2 segundos

                try {
                    connection.connect()
                } catch (e: Exception) {
                    return@withContext false // Si no se puede conectar, devolver false
                }

                val responseCode = connection.responseCode
                responseCode == HttpURLConnection.HTTP_OK
            }
        } catch (e: Exception) {
            false
        }
    }
}
