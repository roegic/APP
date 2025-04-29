package com.example.bondbuddy.data.remote.models

data class ChatUserInfo(
    val otherUserId: Int,
    val firstName: String,
    val lastName: String,
    val photo: ByteArray?,
    val isRead: Boolean
)
