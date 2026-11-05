package com.example.auroracampsites
import com.google.firebase.Timestamp

/**
 * A data class that represents a notification
 *
 * Used to handle the basics of notifications.
 *
 * @property id The unique ID of the message.
 * @property entrantID The ID of the sender. Maps to user data class.
 * @property recipientID The ID of the recipient. Maps to user data class.
 * @property message The message, as a string.
 * @property sentAt The timestamp of the message.
 */
data class Notification(
    val id: String="",
    //will map to id in user data class
    val recipientID: String="",
    //will map to id in user data class
    val entrantID: String="",
    val message: String="",
    val sentAt: Timestamp? =null
)
