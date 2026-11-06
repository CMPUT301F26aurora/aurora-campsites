package com.example.auroracampsites.data.models
import com.google.firebase.Timestamp

/**
 * A data class that represents a message, specifically about messages.
 *
 * Used to handle the basics of messages.
 *
 * @property id The unique ID of the message.
 * @property reservationID The ID of the reservation. Maps to reservation data class
 * @property senderID The ID of the sender. Maps to user data class.
 * @property recipientID The ID of the recipient. Maps to user data class.
 * @property message The message, as a string.
 * @property timeSent The timestamp of the message.
 */
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