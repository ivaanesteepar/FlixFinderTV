package com.example.flixfindertv.ui.viewmodels

import android.app.Application
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class ConexionViewModel(application: Application) : AndroidViewModel(application) {

    // Usamos StateFlow para poder observar la conexión
    private val _conexionEstablecida = MutableStateFlow(true)  // true por defecto, asumimos conexión al inicio
    val conexionEstablecida: StateFlow<Boolean> get() = _conexionEstablecida

    init {
        // Llamamos a checkConnection al iniciar el ViewModel para monitorear el estado de la conexión
        viewModelScope.launch {
            monitorConnection()
        }
    }

    // Función para monitorear la conexión
    private suspend fun monitorConnection() {
        while (true) {
            checkConnection()
            kotlinx.coroutines.delay(5000) // Revisa cada 5 segundos
        }
    }

    // Función para verificar la conexión
    private suspend fun checkConnection() {
        val connectivityManager = getApplication<Application>().getSystemService(Application.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)

        // Verificar si la red tiene acceso a Internet
        val isConnected = networkCapabilities != null &&
                (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ||
                        networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED))

        // Actualizar el estado de la conexión
        _conexionEstablecida.value = isConnected && isInternetAvailable()
    }

    // Función para comprobar si hay acceso a Internet
    private suspend fun isInternetAvailable(): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                val url = URL("https://www.google.com")  // Usar HTTP para mayor compatibilidad
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 2000  // Timeout de 2 segundos
                connection.readTimeout = 2000  // Timeout de 2 segundos

                try {
                    connection.connect()  // Intentar la conexión
                    connection.responseCode == HttpURLConnection.HTTP_OK  // Verificar si la respuesta fue OK
                } catch (e: Exception) {
                    false  // Si hay un error de conexión, devolver false
                }
            }
        } catch (e: Exception) {
            false  // Si no se puede realizar la prueba, devolver false
        }
    }
}
