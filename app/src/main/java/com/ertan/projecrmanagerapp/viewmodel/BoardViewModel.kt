package com.ertan.projecrmanagerapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ertan.projecrmanagerapp.data.model.BoardSummary
import com.ertan.projecrmanagerapp.data.model.CreateBoardRequest
import com.ertan.projecrmanagerapp.data.remote.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class BoardListState {
    object Loading : BoardListState()
    data class Success(val boards: List<BoardSummary>) : BoardListState()
    data class Error(val message: String) : BoardListState()
}

class BoardViewModel(application: Application) : AndroidViewModel(application) {

    private val _boardListState = MutableStateFlow<BoardListState>(BoardListState.Loading)
    val boardListState: StateFlow<BoardListState> = _boardListState

    init {
        loadBoards()
    }

    fun loadBoards() {
        _boardListState.value = BoardListState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getAllBoards()
                if (response.isSuccessful && response.body() != null) {
                    _boardListState.value = BoardListState.Success(response.body()!!)
                } else {
                    _boardListState.value = BoardListState.Error("Failed to load boards.")
                }
            } catch (e: Exception) {
                _boardListState.value = BoardListState.Error("Error: ${e.message}")
            }
        }
    }

    fun createBoard(name: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.createBoard(CreateBoardRequest(name))
                if (response.isSuccessful) {
                    loadBoards() // refresh the list after creating
                    onComplete(true)
                } else {
                    onComplete(false)
                }
            } catch (e: Exception) {
                onComplete(false)
            }
        }
    }

    fun updateBoard(boardId: Int, name: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.updateBoard(boardId, CreateBoardRequest(name))
                if (response.isSuccessful) {
                    loadBoards()
                    onComplete(true)
                } else {
                    onComplete(false)
                }
            } catch (e: Exception) {
                onComplete(false)
            }
        }
    }

    fun deleteBoard(boardId: Int, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.deleteBoard(boardId)
                if (response.isSuccessful) {
                    loadBoards()
                    onComplete(true)
                } else {
                    onComplete(false)
                }
            } catch (e: Exception) {
                onComplete(false)
            }
        }
    }
}