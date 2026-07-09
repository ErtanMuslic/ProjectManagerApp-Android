package com.ertan.projecrmanagerapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ertan.projecrmanagerapp.data.local.TokenManager
import com.ertan.projecrmanagerapp.data.model.LoginRequest
import com.ertan.projecrmanagerapp.data.model.RegisterRequest
import com.ertan.projecrmanagerapp.data.remote.RetrofitInstance
import com.ertan.projecrmanagerapp.data.model.GoogleLoginRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun login(email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.login(LoginRequest(email, password))
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    tokenManager.saveAuthData(body.token, body.role, body.name)
                    _authState.value = AuthState.Success
                } else {
                    _authState.value = AuthState.Error("Wrong email or password.")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Error: ${e.message}")
            }
        }
    }

    fun register(name: String, email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.register(RegisterRequest(name, email, password))
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    tokenManager.saveAuthData(body.token, body.role, body.name)
                    _authState.value = AuthState.Success
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Registration unsuccessfull."
                    _authState.value = AuthState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Error: ${e.message}")
            }
        }
    }

    fun loginWithGoogle(idToken: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.googleLogin(GoogleLoginRequest(idToken))
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    tokenManager.saveAuthData(body.token, body.role, body.name)
                    _authState.value = AuthState.Success
                } else {
                    _authState.value = AuthState.Error("Google login failed.")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Error: ${e.message}")
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }


}