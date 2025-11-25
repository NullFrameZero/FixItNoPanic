package com.example.fixitnopanic

data class RequestItem(
    val id: Long,
    val client: String,
    val phone: String,
    val model: String,
    val problem: String,
    val dateCreated: String,
    val dateCompleted: String?,
    val status: String
)