package com.ertan.projecrmanagerapp.data.model

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class GoogleLoginRequest(
    val idToken: String
)

data class AuthResponse(
    val token: String,
    val userId: Int,
    val name: String,
    val email: String,
    val role: String,
    val seniority: String?
)