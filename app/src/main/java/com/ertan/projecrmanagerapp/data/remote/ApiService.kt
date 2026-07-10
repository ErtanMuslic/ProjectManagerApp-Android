package com.ertan.projecrmanagerapp.data.remote

import com.ertan.projecrmanagerapp.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {


    //Auth
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/google")
    suspend fun googleLogin(@Body request: GoogleLoginRequest): Response<AuthResponse>


    //Board
    @GET("api/boards")
    suspend fun getAllBoards(): Response<List<BoardSummary>>

    @POST("api/boards")
    suspend fun createBoard(@Body request: CreateBoardRequest): Response<Map<String, Any>>

    @GET("api/boards/{id}")
    suspend fun getBoardDetail(@Path("id") boardId: Int): Response<BoardDetail>

    @POST("api/boards/{boardId}/columns")
    suspend fun createColumn(
        @Path("boardId") boardId: Int,
        @Body request: CreateColumnRequest
    ): Response<Map<String, Any>>


    //Cards
    @POST("api/columns/{columnId}/cards")
    suspend fun createCard(
        @Path("columnId") columnId: Int,
        @Body request: CreateCardRequest
    ): Response<Map<String, Any>>

    @PUT("api/cards/{id}/move")
    suspend fun moveCard(
        @Path("id") cardId: Int,
        @Body request: MoveCardRequest
    ): Response<Map<String, Any>>

}