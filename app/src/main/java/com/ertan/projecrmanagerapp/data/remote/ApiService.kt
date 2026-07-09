package com.ertan.projecrmanagerapp.data.remote

import com.ertan.projecrmanagerapp.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/google")
    suspend fun googleLogin(@Body request: GoogleLoginRequest): Response<AuthResponse>

}