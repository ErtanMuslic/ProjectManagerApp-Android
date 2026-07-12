package com.ertan.projecrmanagerapp.data.remote

import com.ertan.projecrmanagerapp.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {


    //Users
    @GET("api/users")
    suspend fun getAllUsers(): Response<List<UserSummary>>

    @PUT("api/users/{id}/seniority")
    suspend fun updateSeniority(
        @Path("id") userId: Int,
        @Body request: UpdateSeniorityRequest
    ): Response<Map<String, Any>>

    //Auth
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/google")
    suspend fun googleLogin(@Body request: GoogleLoginRequest): Response<AuthResponse>

    @GET("api/auth/me")
    suspend fun getMyAccount(): Response<AccountInfoResponse>

    @PUT("api/auth/me")
    suspend fun updateMyAccount(@Body request: UpdateAccountRequest): Response<Map<String, Any>>

    @DELETE("api/auth/me")
    suspend fun deleteMyAccount(): Response<Map<String, Any>>

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

    @PUT("api/boards/{id}")
    suspend fun updateBoard(
        @Path("id") boardId: Int,
        @Body request: CreateBoardRequest
    ): Response<Map<String, Any>>

    @DELETE("api/boards/{id}")
    suspend fun deleteBoard(@Path("id") boardId: Int): Response<Map<String, Any>>




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

    @PUT("api/cards/{id}")
    suspend fun updateCard(
        @Path("id") cardId: Int,
        @Body request: UpdateCardRequest
    ): Response<Map<String, Any>>

    @GET("api/cards/{cardId}/comments")
    suspend fun getComments(@Path("cardId") cardId: Int): Response<List<CommentResponse>>

    @POST("api/cards/{cardId}/comments")
    suspend fun addComment(
        @Path("cardId") cardId: Int,
        @Body request: CreateCommentRequest
    ): Response<CommentResponse>

    @DELETE("api/cards/{cardId}/comments/{commentId}")
    suspend fun deleteComment(
        @Path("cardId") cardId: Int,
        @Path("commentId") commentId: Int
    ): Response<Map<String, Any>>

    @DELETE("api/cards/{id}")
    suspend fun deleteCard(@Path("id") cardId: Int): Response<Map<String, Any>>

    @GET("api/cards/my-tasks")
    suspend fun getMyTasks(): Response<List<MyTaskResponse>>


    //Columns
    @PUT("api/boards/columns/{columnId}")
    suspend fun updateColumn(
        @Path("columnId") columnId: Int,
        @Body request: UpdateColumnRequest
    ): Response<Map<String, Any>>

    @PUT("api/boards/{boardId}/columns/reorder")
    suspend fun reorderColumns(
        @Path("boardId") boardId: Int,
        @Body request: ReorderColumnsRequest
    ): Response<Map<String, Any>>

}