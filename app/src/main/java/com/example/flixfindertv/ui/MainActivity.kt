package com.example.flixfindertv.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.flixfindertv.ui.theme.FlixFinderTVTheme
import com.example.flixfindertv.ui.viewmodels.UsersViewModel
import com.google.firebase.auth.FirebaseAuth

// Actividad principal que configura la navegación y guarda la sesión del usuario al pausar la app
class MainActivity : ComponentActivity() {

    private val usersViewModel: UsersViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FlixFinderTVTheme {
                // Inicializamos el NavController
                val navController = rememberNavController()

                // Define el contenido de las pantallas con la navegación
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    FlixFinderTVroutes(modifier = Modifier.padding(innerPadding), navController = navController)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // Guardar sesión al salir de la app
        val uid = FirebaseAuth.getInstance().currentUser?.uid // Obtén el UID del usuario logueado

        // Si el UID no es nulo, guardamos la sesión
        if (uid != null) {
            usersViewModel.saveSession(context = this, isLoggedIn = true, uid = uid)  // Guardamos la sesión con el UID
        }
    }

}
