package com.example.auroracampsites.data.models
import com.google.firebase.Timestamp

/**
 * A data class that represents a reservation
 *
 * Used to handle the basics of campsite reservations
 *
 * @property id The unique ID of the message.
 * @property userId The ID of the user. Maps to user data class.
 * @property campsiteId The ID of the campsite. Maps to campsite data class.
 * @property  The time the campsite was reserved at.
 * @property price The price of the campsite.
 */

data class Reservation(
    val id: String="",
    //this will map to the ID in the user data class
    val userId: String="",
    //this will map to the ID in campsite data class
    val campsiteId: String="",
    val campsiteName: String = "",
    val location: String = "",
    val maximumOccupancy: Int = 1,
    val price: Double = 0.0,
    val startDate: String = "",
    val endDate: String = "",
    val status: String = "ACTIVE" // Valid values: "ACTIVE", "CANCELLED"
)


