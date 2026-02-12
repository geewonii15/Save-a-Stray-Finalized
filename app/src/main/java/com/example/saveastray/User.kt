package com.example.saveastray

data class User(
    var uid: String = "",
    val email: String = "",
    val fullName: String = "",
    val role: String = "adopter",
    val status: String = "approved"
)