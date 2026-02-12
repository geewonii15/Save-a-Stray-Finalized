package com.example.saveastray

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Cat(
    var id: String = "",
    val name: String = "",
    val breed: String = "",
    val age: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val status: String = "",
    val personality_energy: Int = 0,
    val personality_social: Int = 0,
    val personality_play: Int = 0,
    val personality_interaction: Int = 0
) : Parcelable