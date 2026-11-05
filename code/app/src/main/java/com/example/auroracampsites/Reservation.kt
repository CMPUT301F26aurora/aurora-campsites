package com.example.auroracampsites
import com.google.firebase.Timestamp

/**
 * A data class that represents a reservation
 *
 * Used to handle the basics of campsite reservations
 *
 * @property id The unique ID of the message.
 * @property entrantID The ID of the user. Maps to user data class.
 * @property campsiteID The ID of the campsite. Maps to campsite data class.
 * @property reservedAt The time the campsite was reserved at.
 * @property price The price of the campsite.
 */

data class Reservation(
    val id: String="",
    //this will map to the ID in campsite data class
    val campsiteID: String="",
    //this will map to the ID in the user data class
    val entrantID: String="",
    val reservedAt: Timestamp? = null,
    val price: Double = 0.0
)

