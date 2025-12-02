// File: app/src/main/java/com/example/nusantaraview/NusantaraViewApp.kt

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.nusantaraview.ui.auth.LoginScreen
import com.example.nusantaraview.ui.auth.RegisterScreen
import com.example.nusantaraview.ui.auth.EmailVerificationScreen
import com.example.nusantaraview.ui.auth.AuthViewModel
import com.example.nusantaraview.ui.main.MainScreen

@Composable
fun NusantaraViewApp() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()

    // 👇 Observe status login
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    // 👇 KUNCI: Set startDestination berdasarkan session
    val startDestination = if (isLoggedIn) "main" else "login"

    // 👇 LaunchedEffect untuk handle perubahan state login
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            // User login → paksa ke main
            navController.navigate("main") {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        } else {
            // User logout → paksa ke login
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    // Navigasi sudah ditangani oleh LaunchedEffect di atas
                    // Jadi callback ini bisa kosong
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                },
                viewModel = authViewModel
            )
        }

        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate("verify_email")
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                viewModel = authViewModel
            )
        }

        composable("verify_email") {
            EmailVerificationScreen(
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("verify_email") { inclusive = true }
                        popUpTo("register") { inclusive = true }
                    }
                }
            )
        }

        composable("main") {
            MainScreen(
                onLogout = {
                    authViewModel.logout()
                    // Navigasi ditangani oleh LaunchedEffect di atas
                }
            )
        }
    }
}