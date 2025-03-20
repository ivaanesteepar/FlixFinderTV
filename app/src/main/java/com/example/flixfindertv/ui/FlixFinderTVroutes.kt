package com.example.flixfindertv.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.flixfindertv.ui.screens.DetailsScreen
import com.example.flixfindertv.ui.screens.EditProfileScreen
import com.example.flixfindertv.ui.screens.ExploreScreen
import com.example.flixfindertv.ui.screens.ForgotPasswordScreen
import com.example.flixfindertv.ui.screens.HomeScreen
import com.example.flixfindertv.ui.screens.LoginScreen
import com.example.flixfindertv.ui.screens.NewQuestionsScreen
import com.example.flixfindertv.ui.screens.ProfileScreen
import com.example.flixfindertv.ui.screens.RegisterScreen
import com.example.flixfindertv.ui.screens.TriviaScreen
import com.example.flixfindertv.ui.viewmodels.ConexionViewModel
import com.example.flixfindertv.ui.viewmodels.MoviesViewModel
import com.example.flixfindertv.ui.viewmodels.TriviaViewModel

@Composable
fun FlixFinderTVroutes(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val moviesViewModel: MoviesViewModel = viewModel()
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
                val conexionViewModel: ConexionViewModel = viewModel()
                HomeScreen(navController, moviesViewModel, conexionViewModel)   // Pantalla de inicio
            }
            composable("register") {
                RegisterScreen(navController)  // Pantalla de registro
            }
            composable("explore") {
                ExploreScreen(navController, moviesViewModel)  // Pantalla de exploración
            }
            composable("profile/{uid}") { backStackEntry ->
                val uid = backStackEntry.arguments?.getString("uid") ?: ""
                ProfileScreen(navController, uid)
            }
            composable("forgot_password") {
                ForgotPasswordScreen(navController)  // Pantalla de recuperación de contraseña
            }
            composable("detalles/{id}/{esSerie}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id") ?: ""
                val esSerie = backStackEntry.arguments?.getString("esSerie")?.toBoolean() ?: false
                DetailsScreen(navController, id, esSerie)
            }
            composable("questions") {
                NewQuestionsScreen(navController)
            }
            composable("edit_profile") {
                EditProfileScreen(navController)  // Pantalla de exploración
            }
            composable("trivia"){
                val triviaViewModel: TriviaViewModel = viewModel()
                TriviaScreen(navController, triviaViewModel)
            }

        }
    }
}
