import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.nusantaraview.ui.auth.LoginScreen
import com.example.nusantaraview.ui.auth.RegisterScreen
import com.example.nusantaraview.ui.auth.EmailVerificationScreen // Pastikan di-import
import com.example.nusantaraview.ui.auth.AuthViewModel
import com.example.nusantaraview.ui.main.MainScreen

@Composable
fun NusantaraViewApp() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    // Cek status login saat aplikasi dibuka
    LaunchedEffect(Unit) {
        authViewModel.checkLoginStatus()
    }

    NavHost(
        navController = navController,
        // Jika isLoggedIn true, langsung ke main. Jika false, ke login.
        startDestination = if (isLoggedIn) "main" else "login"
    ) {
        // 1. Rute Login
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                },
                viewModel = authViewModel
            )
        }

        // 2. Rute Register
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    // BERUBAH: Saat sukses register, jangan ke main, tapi ke halaman verifikasi
                    navController.navigate("verify_email")
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                viewModel = authViewModel
            )
        }

        // 3. Rute Baru: Verifikasi Email
        composable("verify_email") {
            EmailVerificationScreen(
                onNavigateToLogin = {
                    // Arahkan user kembali ke login agar mereka login manual
                    navController.navigate("login") {
                        popUpTo("verify_email") { inclusive = true }
                        popUpTo("register") { inclusive = true }
                    }
                }
            )
        }

        // 4. Rute Main (Home)
        composable("main") {
            MainScreen(
                onLogout = {
                    // 1. Panggil fungsi logout yang sudah diperbaiki
                    authViewModel.logout()

                    // 2. Navigasi paksa ke Login dan hapus semua history layar sebelumnya
                    navController.navigate("login") {
                        // popUpTo(0) artinya hapus semua layar dari memori
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}