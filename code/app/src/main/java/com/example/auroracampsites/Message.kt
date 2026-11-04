package com.example.auroracampsites
import com.google.firebase.Timestamp

data class Message (
    val id : String = "",
    //this will map to the id in the reservation data class
    val reservationID : String = "",
    //this will map the id in the user data class
    val senderID: String = "",
    //this will map to the id in the user data class
    val recipientID: String = "",
    val message: String = "",
    val timeSent: Timestamp?= null
)