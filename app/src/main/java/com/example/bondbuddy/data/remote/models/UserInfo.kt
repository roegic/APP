package com.example.bondbuddy.data.remote.models

data class UserInfo(
    var userId: Int,
    val firstName: String,
    val lastName: String,
    val age: Int? = null,
    val bio: String? = null,
    val country: String? = null,
    val city: String? = null,
    val occupation: String? = null,
    val languages: String? = null,
    val photo: ByteArray? = null,
    val interests: String? = null
)
