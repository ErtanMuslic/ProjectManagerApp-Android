package com.ertan.projecrmanagerapp.data.model

data class UserSummary(
    val id: Int,
    val name: String,
    val email: String,
    val seniority: String?,
    val createdAt: String
)

data class UpdateCardRequest(
    val title: String? = null,
    val description: String? = null,
    val assignedUserId: Int? = null,
    val dueDate: String? = null,
    val priority: String? = null
)

data class CommentResponse(
    val id: Int,
    val content: String,
    val userId: Int,
    val userName: String,
    val createdAt: String
)

data class CreateCommentRequest(
    val content: String
)

data class UpdateSeniorityRequest(
    val seniority: String
)