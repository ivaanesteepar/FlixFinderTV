package com.example.flixfindertv.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHost
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.flixfindertv.ui.screens.ExploreScreen
import com.example.flixfindertv.ui.screens.ForgotPasswordScreen
import com.example.flixfindertv.ui.screens.HomeScreen
import com.example.flixfindertv.ui.screens.LoginScreen
import com.example.flixfindertv.ui.screens.ProfileScreen
import com.example.flixfindertv.ui.screens.RegisterScreen
import com.example.flixfindertv.ui.screens.SearchScreen

@Composable
fun FlixFinderTVroutes() {
    val navController = rememberNavController()
    Scaffold { padding ->
        NavHost(
            navController = navController,
            startDestination = "login", // Pantalla de inicio es el login
            modifier = Modifier.padding(padding)
        ) {
            composable("login") {
                LoginScreen(navController)  // Pantalla de login
            }
            composable("home") {
                HomeScreen(navController)   // Pantalla de inicio
            }
            composable("register") {
                RegisterScreen(navController)  // Pantalla de registro
            }
            composable("explore") {
                ExploreScreen(navController)  // Pantalla de exploración
            }
            composable("search") {
                SearchScreen(navController)  // Pantalla de búsqueda
            }
            composable("profile") {
                ProfileScreen(navController)  // Pantalla de perfil
            }
            composable("forgot_password") {
                ForgotPasswordScreen(navController)  // Pantalla de recuperación de contraseña
            }

        }
    }
}
