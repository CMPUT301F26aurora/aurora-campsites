package com.example.auroracampsites.data.models


enum class UserRole {
    USER,
    OWNER,
    ADMIN
}
/**
 * Represents a user  within the app, tracking profile details,
 * permission levels, role flags, and notification preferences.
 *
 * @property id The unique identifier for the user.
 * @property username The display name or username of the user.
 * @property email The email address associated with the user account.
 * @property phoneNumber The contact phone number of the user.
 * @property isEntrant Flag indicating whether the user holds entrant privileges.
 * @property permissionLevel The numerical permission tier (0 = User, 1 = Owner, 2 = Admin).
 * @property isOwner Flag indicating whether the user holds campsite owner privileges.
 * @property isAdmin Flag indicating whether the user holds administrative privileges.
 * @property notificationsEnabled Flag indicating whether the user has push notifications enabled.
 */

data class User(
    val id: String = "",
    val username: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val role: UserRole = UserRole.USER,
    val notificationsEnabled: Boolean = true
)

