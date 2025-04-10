package com.example.flixfindertv.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.flixfindertv.ui.theme.FlixFinderTVTheme
import com.example.flixfindertv.ui.viewmodels.UsersViewModel

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
        usersViewModel.saveSession(this, true)  // 'true' significa que el usuario está logueado
    }
}
