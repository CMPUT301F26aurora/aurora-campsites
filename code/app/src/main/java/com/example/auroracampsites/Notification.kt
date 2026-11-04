package com.example.auroracampsites
import com.google.firebase.Timestamp


data class Notification(
    val id: String="",
    //will map to id in user data class
    val recipientID: String="",
    //will map to id in user data class
    val entrantID: String="",
    val message: String="",
    val sentAt: Timestamp? =null
)
