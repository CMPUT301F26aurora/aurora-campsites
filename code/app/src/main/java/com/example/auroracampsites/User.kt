package com.example.auroracampsites

data class User (
    val username: String="",
    val email: String="",
    val phonenumber: String="",
    val permissionlevel: Int=0 // 0 = regular user, 1 = owner, 2 = admin
)