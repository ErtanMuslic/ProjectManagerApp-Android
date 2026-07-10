package com.ertan.projecrmanagerapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ertan.projecrmanagerapp.data.model.BoardDetail
import com.ertan.projecrmanagerapp.data.model.CreateCardRequest
import com.ertan.projecrmanagerapp.data.model.CreateColumnRequest
import com.ertan.projecrmanagerapp.data.model.MoveCardRequest
import com.ertan.projecrmanagerapp.data.remote.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class BoardDetailState {
    object Loading : BoardDetailState()
    data class Success(val board: BoardDetail) : BoardDetailState()
    data class Error(val message: String) : BoardDetailState()
}

class BoardDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow<BoardDetailState>(BoardDetailState.Loading)
    val state: StateFlow<BoardDetailState> = _state

    private var currentBoardId: Int = -1

    fun loadBoard(boardId: Int) {
        currentBoardId = boardId
        _state.value = BoardDetailState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getBoardDetail(boardId)
                if (response.isSuccessful && response.body() != null) {
                    _state.value = BoardDetailState.Success(response.body()!!)
                } else {
                    _state.value = BoardDetailState.Error("Failed to load board.")
                }
            } catch (e: Exception) {
                _state.value = BoardDetailState.Error("Error: ${e.message}")
            }
        }
    }

    fun createColumn(name: String, parentColumnId: Int?, onComplete: (Boolean) -> Unit) {
        val currentState = _state.value
        if (currentState !is BoardDetailState.Success) {
            onComplete(false)
            return
        }

        val order = if (parentColumnId == null) {
            currentState.board.columns.size
        } else {
            val parent = currentState.board.columns.find { it.id == parentColumnId }
            parent?.subColumns?.size ?: 0
        }

        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.createColumn(
                    currentBoardId,
                    CreateColumnRequest(name, order, parentColumnId)
                )
                if (response.isSuccessful) {
                    loadBoard(currentBoardId)
                    onComplete(true)
                } else {
                    onComplete(false)
                }
            } catch (e: Exception) {
                onComplete(false)
            }
        }
    }

    fun createCard(columnId: Int, title: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.createCard(
                    columnId,
                    CreateCardRequest(title = title)
                )
                if (response.isSuccessful) {
                    loadBoard(currentBoardId)
                    onComplete(true)
                } else {
                    onComplete(false)
                }
            } catch (e: Exception) {
                onComplete(false)
            }
        }
    }

    fun moveCard(cardId: Int, newColumnId: Int, newOrder: Int) {
        viewModelScope.launch {
            try {
                RetrofitInstance.api.moveCard(cardId, MoveCardRequest(newColumnId, newOrder))
                loadBoard(currentBoardId)
            } catch (e: Exception) {
                // Silently fail for now; could show a snackbar later
            }
        }
    }
}