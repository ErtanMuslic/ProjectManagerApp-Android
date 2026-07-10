package com.ertan.projecrmanagerapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ertan.projecrmanagerapp.data.model.UpdateSeniorityRequest
import com.ertan.projecrmanagerapp.data.model.UserSummary
import com.ertan.projecrmanagerapp.data.remote.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class UserManagementState {
    object Loading : UserManagementState()
    data class Success(val users: List<UserSummary>) : UserManagementState()
    data class Error(val message: String) : UserManagementState()
}

class UserManagementViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow<UserManagementState>(UserManagementState.Loading)
    val state: StateFlow<UserManagementState> = _state

    init {
        loadUsers()
    }

    fun loadUsers() {
        _state.value = UserManagementState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getAllUsers()
                if (response.isSuccessful && response.body() != null) {
                    _state.value = UserManagementState.Success(response.body()!!)
                } else {
                    _state.value = UserManagementState.Error("Failed to load users.")
                }
            } catch (e: Exception) {
                _state.value = UserManagementState.Error("Error: ${e.message}")
            }
        }
    }

    fun updateSeniority(userId: Int, seniority: String) {
        viewModelScope.launch {
            try {
                RetrofitInstance.api.updateSeniority(userId, UpdateSeniorityRequest(seniority))
                loadUsers() // refresh after update
            } catch (e: Exception) {
                // Silently ignore for now; could show a snackbar later
            }
        }
    }
}