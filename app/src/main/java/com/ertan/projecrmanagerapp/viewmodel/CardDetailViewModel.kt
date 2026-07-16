package com.ertan.projecrmanagerapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ertan.projecrmanagerapp.data.model.*
import com.ertan.projecrmanagerapp.data.remote.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class CardDetailUiState {
    object Loading : CardDetailUiState()
    data class Success(
        val card: CardDetail,
        val allColumns: List<ColumnDetail>,
        val users: List<UserSummary>,
        val comments: List<CommentResponse>
    ) : CardDetailUiState()
    data class Error(val message: String) : CardDetailUiState()
}

class CardDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow<CardDetailUiState>(CardDetailUiState.Loading)
    val state: StateFlow<CardDetailUiState> = _state

    private var boardId: Int = -1
    private var cardId: Int = -1

    fun load(boardId: Int, cardId: Int) {
        this.boardId = boardId
        this.cardId = cardId
        _state.value = CardDetailUiState.Loading

        viewModelScope.launch {
            try {
                val boardResponse = RetrofitInstance.api.getBoardDetail(boardId)
                val usersResponse = RetrofitInstance.api.getAllUsers()
                val commentsResponse = RetrofitInstance.api.getComments(cardId)

                if (boardResponse.isSuccessful && boardResponse.body() != null) {
                    val board = boardResponse.body()!!
                    val card = findCard(board.columns, cardId)

                    if (card != null) {
                        val users = if (usersResponse.isSuccessful) usersResponse.body() ?: emptyList() else emptyList()
                        val comments = if (commentsResponse.isSuccessful) commentsResponse.body() ?: emptyList() else emptyList()
                        _state.value = CardDetailUiState.Success(card, board.columns, users, comments)
                    } else {
                        _state.value = CardDetailUiState.Error("Card not found.")
                    }
                } else {
                    _state.value = CardDetailUiState.Error("Failed to load card.")
                }
            } catch (e: Exception) {
                _state.value = CardDetailUiState.Error("Error: ${e.message}")
            }
        }
    }

    // Recursively searches main columns and their sub-columns for a card with the given id
    private fun findCard(columns: List<ColumnDetail>, cardId: Int): CardDetail? {
        for (column in columns) {
            column.cards.find { it.id == cardId }?.let { return it }
            findCard(column.subColumns, cardId)?.let { return it }
        }
        return null
    }

    fun updateCard(request: UpdateCardRequest, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.updateCard(cardId, request)
                if (response.isSuccessful) {
                    load(boardId, cardId)
                    onComplete(true)
                } else {
                    onComplete(false)
                }
            } catch (e: Exception) {
                onComplete(false)
            }
        }
    }

    fun moveCard(newColumnId: Int) {
        viewModelScope.launch {
            try {
                RetrofitInstance.api.moveCard(cardId, MoveCardRequest(newColumnId, 0))
                load(boardId, cardId)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun addComment(content: String) {
        viewModelScope.launch {
            try {
                RetrofitInstance.api.addComment(cardId, CreateCommentRequest(content))
                load(boardId, cardId)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun deleteComment(commentId: Int) {
        viewModelScope.launch {
            try {
                RetrofitInstance.api.deleteComment(cardId, commentId)
                load(boardId, cardId)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun deleteCard(onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.deleteCard(cardId)
                onComplete(response.isSuccessful)
            } catch (e: Exception) {
                onComplete(false)
            }
        }
    }

    fun assignToMe(onComplete: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.assignToMe(cardId)
                if (response.isSuccessful) {
                    load(boardId, cardId)
                    onComplete(true, null)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Failed to assign card."
                    onComplete(false, errorMsg)
                }
            } catch (e: Exception) {
                onComplete(false, "Error: ${e.message}")
            }
        }
    }

    fun unassignMe(onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.unassignMe(cardId)
                if (response.isSuccessful) {
                    load(boardId, cardId)
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