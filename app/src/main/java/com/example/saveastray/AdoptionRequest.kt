package com.example.saveastray

data class AdoptionRequest(
    var id: String = "",
    var userId: String = "",
    var userEmail: String = "",
    var catId: String = "",
    var catName: String = "",
    var catImageUrl: String = "",
    var status: String = ""
)