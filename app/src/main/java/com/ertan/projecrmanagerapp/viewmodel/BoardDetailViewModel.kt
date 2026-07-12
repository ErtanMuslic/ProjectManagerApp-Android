package com.ertan.projecrmanagerapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ertan.projecrmanagerapp.data.model.BoardDetail
import com.ertan.projecrmanagerapp.data.model.ColumnDetail
import com.ertan.projecrmanagerapp.data.model.ColumnOrderItem
import com.ertan.projecrmanagerapp.data.model.CreateCardRequest
import com.ertan.projecrmanagerapp.data.model.CreateColumnRequest
import com.ertan.projecrmanagerapp.data.model.MoveCardRequest
import com.ertan.projecrmanagerapp.data.model.ReorderColumnsRequest
import com.ertan.projecrmanagerapp.data.model.UpdateColumnRequest
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

    fun createColumn(name: String, parentColumnId: Int?, cardLimit: Int?, onComplete: (Boolean) -> Unit) {
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
                    CreateColumnRequest(name, order, parentColumnId, cardLimit)
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

    fun updateColumnLimit(columnId: Int, newLimit: Int?, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val request = if (newLimit == null) {
                    UpdateColumnRequest(clearCardLimit = true)
                } else {
                    UpdateColumnRequest(cardLimit = newLimit)
                }
                val response = RetrofitInstance.api.updateColumn(columnId, request)
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

    fun moveColumnLeft(columnId: Int) {
        swapColumnOrder(columnId, moveRight = false)
    }

    fun moveColumnRight(columnId: Int) {
        swapColumnOrder(columnId, moveRight = true)
    }

    private fun swapColumnOrder(columnId: Int, moveRight: Boolean) {
        val currentState = _state.value
        if (currentState !is BoardDetailState.Success) return

        val mainColumns = currentState.board.columns.sortedBy { it.order }
        val mainIndex = mainColumns.indexOfFirst { it.id == columnId }

        val siblings: List<ColumnDetail>

        if (mainIndex != -1) {
            siblings = mainColumns
        } else {
            val parent = mainColumns.find { parent -> parent.subColumns.any { it.id == columnId } }
            siblings = parent?.subColumns?.sortedBy { it.order } ?: return
        }

        val currentIndex = siblings.indexOfFirst { it.id == columnId }
        val swapIndex = if (moveRight) currentIndex + 1 else currentIndex - 1

        if (currentIndex == -1 || swapIndex < 0 || swapIndex >= siblings.size) return

        // Rebuild the sibling order list from scratch (0..n-1), based on current on-screen position,
        // then swap the two target positions. This avoids issues if the stored "order" values
        // in the database are duplicated or inconsistent.
        val reordered = siblings.toMutableList()
        val temp = reordered[currentIndex]
        reordered[currentIndex] = reordered[swapIndex]
        reordered[swapIndex] = temp

        val orderItems = reordered.mapIndexed { index, col ->
            ColumnOrderItem(col.id, index)
        }

        val request = ReorderColumnsRequest(columns = orderItems)

        viewModelScope.launch {
            try {
                RetrofitInstance.api.reorderColumns(currentBoardId, request)
                loadBoard(currentBoardId)
            } catch (e: Exception) {
                // Silently ignore for now
            }
        }
    }
}