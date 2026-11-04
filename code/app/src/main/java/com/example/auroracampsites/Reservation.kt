package com.example.auroracampsites
import com.google.firebase.Timestamp

data class Reservation(
    val id: String="",
    //this will map to the ID in campsite data class
    val campsiteID: String="",
    //this will map to the ID in the user data class
    val entrantID: String="",
    val reservedAt: Timestamp? = null,
    val price: Double = 0.0
)

