package com.example.auroracampsites

/**
 * A data class that represents a campsite rating.
 *
 * Used to handle the basics of ratings.
 *
 * @property id The unique ID of the message.
 * @property userID The ID of the user. Maps to user data class.
 * @property campsiteID The ID of the campsite. Maps to user data class.
 * @property rating The rating, as a double.
 */
class Rating (
    val id : String="",
    //this will map to the id in the user data class
    val userID : String="",
    //this will map to the id in the campsite data class
    val campsiteID: String="",
    val rating : Double = 0.0
)