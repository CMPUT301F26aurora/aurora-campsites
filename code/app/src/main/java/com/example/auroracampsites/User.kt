package com.example.auroracampsites

data class User (
    val id : String="",
    val username: String="",
    val email: String="",
    val phoneNumber: String="",
    val isEntrant: Boolean = true,
    val permissionLevel: Int = 0, // 0 = user, 1 = owner 2 = admin
    val isOwner: Boolean = false,
    val isAdmin: Boolean = false,
    val notificationsEnabled: Boolean =true
)