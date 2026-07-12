package com.ertan.projecrmanagerapp.data.model

data class AccountInfoResponse(
    val id: Int,
    val name: String,
    val email: String,
    val role: String,
    val seniority: String?,
    val hasPassword: Boolean
)

data class UpdateAccountRequest(
    val name: String? = null,
    val currentPassword: String? = null,
    val newPassword: String? = null
)