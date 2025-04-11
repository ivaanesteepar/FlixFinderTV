package com.example.flixfindertv.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.flixfindertv.ui.screens.DetailsScreen
import com.example.flixfindertv.ui.screens.EditProfileScreen
import com.example.flixfindertv.ui.screens.ExploreScreen
import com.example.flixfindertv.ui.screens.FavouriteContent
import com.example.flixfindertv.ui.screens.ForgotPasswordScreen
import com.example.flixfindertv.ui.screens.HomeScreen
import com.example.flixfindertv.ui.screens.LoginScreen
import com.example.flixfindertv.ui.screens.NewQuestionsScreen
import com.example.flixfindertv.ui.screens.ProfileScreen
import com.example.flixfindertv.ui.screens.RegisterScreen
import com.example.flixfindertv.ui.screens.TriviaScreen
import com.example.flixfindertv.ui.screens.UserListScreen
import com.example.flixfindertv.ui.viewmodels.ConexionViewModel
import com.example.flixfindertv.ui.viewmodels.MoviesViewModel
import com.example.flixfindertv.ui.viewmodels.TriviaViewModel
import com.example.flixfindertv.ui.viewmodels.UsersViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun FlixFinderTVroutes(modifier: Modifier = Modifier, navController: NavHostController) {
    val moviesViewModel: MoviesViewModel = viewModel()
    val usersViewModel: UsersViewModel = viewModel()
    val context = LocalContext.current

    Scaffold { padding ->
        // Obtenemos el UID del usuario
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        // Verificamos si el usuario está logueado y si el UID no es null
        val startDestination = if (uid != null && usersViewModel.isUserLoggedIn(context, uid)) {
            "home"  // Pantalla principal
        } else {
            "login"  // Pantalla de login
        }

        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(padding)
        ) {
            composable("login") {
                LoginScreen(navController)  // Pantalla de login
            }
            composable("home") {
                val conexionViewModel: ConexionViewModel = viewModel()
                HomeScreen(
                    navController,
                    moviesViewModel,
                    conexionViewModel
                )   // Pantalla de inicio
            }
            composable("register") {
                RegisterScreen(navController)  // Pantalla de registro
            }
            composable("explore") {
                ExploreScreen(navController, moviesViewModel)  // Pantalla de exploración
            }
            composable("profile/{userId}/{isComment}") { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId")
                val isComment = backStackEntry.arguments?.getString("isComment")?.toBoolean()
                if (isComment != null) {
                    if (userId != null) {
                        ProfileScreen(navController, userId, isComment)
                    }
                }
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
            composable("trivia") {
                TriviaScreen(navController)
            }
            composable("favourite_movies/{uid}/{esSerie}") { backStackEntry ->
                val esSerie = backStackEntry.arguments?.getString("esSerie")?.toBoolean() ?: false
                val uid = backStackEntry.arguments?.getString("uid") ?: ""
                FavouriteContent(navController, uid, esSerie)
            }
            composable("users_list/{uid}/{isFollowing}") { backStackEntry ->
                val uid = backStackEntry.arguments?.getString("uid") ?: ""
                val isFollowingString = backStackEntry.arguments?.getString("isFollowing")
                val isFollowing =
                    isFollowingString?.toBoolean() ?: false  // Convierte la cadena a Boolean
                UserListScreen(navController, uid, isFollowing)
            }

        }
    }
}
