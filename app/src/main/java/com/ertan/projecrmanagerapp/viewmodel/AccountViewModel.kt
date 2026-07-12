package com.ertan.projecrmanagerapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ertan.projecrmanagerapp.data.local.TokenManager
import com.ertan.projecrmanagerapp.data.model.AccountInfoResponse
import com.ertan.projecrmanagerapp.data.model.UpdateAccountRequest
import com.ertan.projecrmanagerapp.data.remote.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AccountState {
    object Loading : AccountState()
    data class Success(val account: AccountInfoResponse) : AccountState()
    data class Error(val message: String) : AccountState()
}

sealed class AccountActionResult {
    object Idle : AccountActionResult()
    object Success : AccountActionResult()
    data class Error(val message: String) : AccountActionResult()
}

class AccountViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)

    private val _state = MutableStateFlow<AccountState>(AccountState.Loading)
    val state: StateFlow<AccountState> = _state

    private val _actionResult = MutableStateFlow<AccountActionResult>(AccountActionResult.Idle)
    val actionResult: StateFlow<AccountActionResult> = _actionResult

    init {
        loadAccount()
    }

    fun loadAccount() {
        _state.value = AccountState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getMyAccount()
                if (response.isSuccessful && response.body() != null) {
                    _state.value = AccountState.Success(response.body()!!)
                } else {
                    _state.value = AccountState.Error("Failed to load account info.")
                }
            } catch (e: Exception) {
                _state.value = AccountState.Error("Error: ${e.message}")
            }
        }
    }

    fun updateName(newName: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.updateMyAccount(UpdateAccountRequest(name = newName))
                if (response.isSuccessful) {
                    _actionResult.value = AccountActionResult.Success
                    loadAccount()
                } else {
                    _actionResult.value = AccountActionResult.Error("Failed to update name.")
                }
            } catch (e: Exception) {
                _actionResult.value = AccountActionResult.Error("Error: ${e.message}")
            }
        }
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.updateMyAccount(
                    UpdateAccountRequest(currentPassword = currentPassword, newPassword = newPassword)
                )
                if (response.isSuccessful) {
                    _actionResult.value = AccountActionResult.Success
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Failed to change password."
                    _actionResult.value = AccountActionResult.Error(errorMsg)
                }
            } catch (e: Exception) {
                _actionResult.value = AccountActionResult.Error("Error: ${e.message}")
            }
        }
    }

    fun deleteAccount(onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.deleteMyAccount()
                if (response.isSuccessful) {
                    tokenManager.clear()
                    onComplete(true)
                } else {
                    onComplete(false)
                }
            } catch (e: Exception) {
                onComplete(false)
            }
        }
    }

    fun resetActionResult() {
        _actionResult.value = AccountActionResult.Idle
    }
}