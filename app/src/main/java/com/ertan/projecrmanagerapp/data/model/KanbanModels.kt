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