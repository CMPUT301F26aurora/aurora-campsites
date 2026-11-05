package com.example.auroracampsites

/**
 * A data class that represents a user.
 *
 * Used to handle the basics of campsite information.
 *
 * @property id The unique ID of the user.
 * @property username The username of the user.
 * @property email The email of the user.
 * @property phoneNumber The phone number of the user
 * @property isEntrant If the user is an entrant or not.
 * @property isOwner If the user is an owner or not.
 * @property isAdmin If the user is an admin or not.
 * @property notificationsEnabled If the user has notifications enabled or not.
 */
data class User (
    val id : String="",
    val username: String="",
    val email: String="",
    val phoneNumber: String="",
    val isEntrant: Boolean = true,
    val isOwner: Boolean = false,
    val isAdmin: Boolean = false,
    val notificationsEnabled: Boolean =true
)