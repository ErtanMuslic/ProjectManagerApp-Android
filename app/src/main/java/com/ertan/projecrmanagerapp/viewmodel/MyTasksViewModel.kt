package com.ertan.projecrmanagerapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ertan.projecrmanagerapp.data.model.MyTaskResponse
import com.ertan.projecrmanagerapp.data.remote.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class MyTasksState {
    object Loading : MyTasksState()
    data class Success(val tasks: List<MyTaskResponse>) : MyTasksState()
    data class Error(val message: String) : MyTasksState()
}

class MyTasksViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow<MyTasksState>(MyTasksState.Loading)
    val state: StateFlow<MyTasksState> = _state

    init {
        loadTasks()
    }

    fun loadTasks() {
        _state.value = MyTasksState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getMyTasks()
                if (response.isSuccessful && response.body() != null) {
                    _state.value = MyTasksState.Success(response.body()!!)
                } else {
                    _state.value = MyTasksState.Error("Failed to load your tasks.")
                }
            } catch (e: Exception) {
                _state.value = MyTasksState.Error("Error: ${e.message}")
            }
        }
    }
}