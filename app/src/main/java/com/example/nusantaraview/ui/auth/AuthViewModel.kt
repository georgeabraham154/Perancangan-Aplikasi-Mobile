// File: app/src/main/java/com/example/nusantaraview/ui/auth/AuthViewModel.kt

package com.example.nusantaraview.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nusantaraview.data.remote.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // Fungsi untuk cek session saat app dibuka
    init {
        checkLoginStatus()
    }

    fun checkLoginStatus() {
        viewModelScope.launch {
            try {
                val session = SupabaseClient.client.auth.currentSessionOrNull()
                val user = SupabaseClient.client.auth.currentUserOrNull()

                Log.d("AuthViewModel", "=== CHECK LOGIN STATUS ===")
                Log.d("AuthViewModel", "Session: ${if (session != null) "EXISTS" else "NULL"}")
                Log.d("AuthViewModel", "User ID: ${user?.id}")
                Log.d("AuthViewModel", "User Email: ${user?.email}")
                Log.d("AuthViewModel", "==========================")

                // 👇 SET STATE: Jika session & user ada = logged in
                _isLoggedIn.value = session != null && user != null
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Check login error: ${e.message}")
                _isLoggedIn.value = false
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                Log.d("AuthViewModel", "Attempting login with: $email")

                SupabaseClient.client.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }

                val user = SupabaseClient.client.auth.currentUserOrNull()
                Log.d("AuthViewModel", "✅ Login SUCCESS!")
                Log.d("AuthViewModel", "User ID: ${user?.id}")
                Log.d("AuthViewModel", "Email: ${user?.email}")

                _isLoggedIn.value = true
                _authState.value = AuthState.Success
                _errorMessage.value = null
            } catch (e: Exception) {
                Log.e("AuthViewModel", "❌ Login FAILED: ${e.message}")
                _authState.value = AuthState.Error
                _errorMessage.value = parseErrorMessage(e.message)
            }
        }
    }

    fun register(emailInput: String, passwordInput: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                Log.d("AuthViewModel", "Attempting register with: $emailInput")

                SupabaseClient.client.auth.signUpWith(Email) {
                    this.email = emailInput
                    this.password = passwordInput
                }

                Log.d("AuthViewModel", "✅ Register SUCCESS! Check email for verification.")

                _authState.value = AuthState.Success
                _errorMessage.value = null
            } catch (e: Exception) {
                Log.e("AuthViewModel", "❌ Register FAILED: ${e.message}")
                _authState.value = AuthState.Error
                _errorMessage.value = parseErrorMessage(e.message)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                Log.d("AuthViewModel", "Attempting logout...")
                SupabaseClient.client.auth.signOut()
                Log.d("AuthViewModel", "✅ Logout SUCCESS")
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Logout error: ${e.message}")
            } finally {
                // 👇 PENTING: Set ke false agar UI update
                _isLoggedIn.value = false
                _authState.value = AuthState.Idle
                _errorMessage.value = null
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
        _authState.value = AuthState.Idle
    }

    private fun parseErrorMessage(message: String?): String {
        return when {
            message == null -> "Terjadi kesalahan"
            message.contains("Invalid login credentials", ignoreCase = true) ->
                "Email atau password salah"
            message.contains("already registered", ignoreCase = true) ||
                    message.contains("User already registered", ignoreCase = true) ->
                "Email sudah terdaftar"
            message.contains("Password should be", ignoreCase = true) ->
                "Password minimal 6 karakter"
            message.contains("invalid email", ignoreCase = true) ||
                    message.contains("Unable to validate email", ignoreCase = true) ->
                "Format email tidak valid"
            message.contains("network", ignoreCase = true) ||
                    message.contains("connection", ignoreCase = true) ->
                "Koneksi internet bermasalah"
            else -> "Email atau password salah"
        }
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    object Error : AuthState()
}