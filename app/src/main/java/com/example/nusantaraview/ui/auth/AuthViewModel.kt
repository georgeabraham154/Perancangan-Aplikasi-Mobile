package com.example.nusantaraview.ui.auth

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

    fun checkLoginStatus() {
        viewModelScope.launch {
            try {
                val session = SupabaseClient.client.auth.currentSessionOrNull()
                _isLoggedIn.value = session != null
            } catch (e: Exception) {
                _isLoggedIn.value = false
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                SupabaseClient.client.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                _isLoggedIn.value = true
                _authState.value = AuthState.Success
                _errorMessage.value = null
            } catch (e: Exception) {
                _authState.value = AuthState.Error
                _errorMessage.value = e.message ?: "Login failed"
            }
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                SupabaseClient.client.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                }
                _isLoggedIn.value = true
                _authState.value = AuthState.Success
                _errorMessage.value = null
            } catch (e: Exception) {
                _authState.value = AuthState.Error
                _errorMessage.value = e.message ?: "Registration failed"
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                SupabaseClient.client.auth.signOut()
                _isLoggedIn.value = false
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
        _authState.value = AuthState.Idle
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    object Error : AuthState()
}