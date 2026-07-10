package com.ertan.projecrmanagerapp.data.model

data class BoardSummary(
    val id: Int,
    val name: String,
    val createdAt: String
)

data class CreateBoardRequest(
    val name: String
)