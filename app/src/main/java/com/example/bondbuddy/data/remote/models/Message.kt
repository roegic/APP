package com.example.bondbuddy.data.remote.models

data class Message(
    val chatId: Int,
    val senderId: Int,
    val text: String,
    val timestamp: Long,
    val isRead: Boolean
)
