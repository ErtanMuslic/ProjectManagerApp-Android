package com.ertan.projecrmanagerapp.data.model

data class BoardDetail(
    val id: Int,
    val name: String,
    val columns: List<ColumnDetail>
)

data class ColumnDetail(
    val id: Int,
    val name: String,
    val order: Int,
    val cardLimit: Int?,
    val cards: List<CardDetail>,
    val subColumns: List<ColumnDetail>
)

data class CardDetail(
    val id: Int,
    val title: String,
    val description: String?,
    val order: Int,
    val assignedUserId: Int?,
    val assignedUserName: String?,
    val dueDate: String?,
    val priority: String,
    val commentCount: Int
)

data class CreateColumnRequest(
    val name: String,
    val order: Int,
    val cardLimit: Int? = null,
    val parentColumnId: Int? = null
)

data class CreateCardRequest(
    val title: String,
    val description: String? = null,
    val assignedUserId: Int? = null,
    val dueDate: String? = null,
    val priority: String = "Medium"
)

data class MoveCardRequest(
    val newColumnId: Int,
    val newOrder: Int
)

data class UpdateColumnRequest(
    val name: String? = null,
    val order: Int? = null,
    val cardLimit: Int? = null,
    val clearCardLimit: Boolean = false
)

data class ReorderColumnsRequest(
    val columns: List<ColumnOrderItem>
)

data class ColumnOrderItem(
    val columnId: Int,
    val newOrder: Int
)

data class MyTaskResponse(
    val cardId: Int,
    val title: String,
    val description: String?,
    val priority: String,
    val dueDate: String?,
    val boardId: Int,
    val boardName: String,
    val columnName: String
)