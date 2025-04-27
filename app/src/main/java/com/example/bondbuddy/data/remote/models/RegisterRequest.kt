package com.example.bondbuddy.data.remote.models

data class RegisterRequest(
    val firstName: String,
    val lastName: String,
    val email:String,
    val username:String,
    val password:String
)
