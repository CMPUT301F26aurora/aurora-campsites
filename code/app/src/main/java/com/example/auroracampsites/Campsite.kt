package com.example.auroracampsites

/**
 * A data class that represents a campsite.
 *
 * Used to handle the basics of campsite information.
 *
 * @property id The unique ID of the campsite.
 * @property name The name of the campsite.
 * @property location The location/address of the campsite.
 * @property maxOccupancy The maximum occupancy of the campsite, as an integer.
 * @property price The price of the campsite.
 * @property rating The rating of the campsite.
 * @property ownerID The ID of the owner.
 * @property rules The unique ID of the campsite.
 * @property cancellationPolicy The policy for cancellation on the campsite.
 * @property description The description of the campsite
 * @property amenities The amenities available to the campsite.
 * @property availableDate The date that the campsite is available.
 * @property hasActiveLottery A boolean that indicates if the campsite is within a lottery or not.
 * @property isWaitlistFull A boolean that indicates if the waitlist is full or not.
 * @property isWinner If the campsite is the winner of a lottery pick.
 * @property isLoser If the campsite is the loser of a lottery pick.
 */
data class Campsite (
    val id: String = "",
    val name: String = "",
    val location: String = "",
    val maxOccupancy: Int = 0,
    val price: Double = 0.0,
    val rating: Double = 0.0,
    //this will map to user ID where isOwner is set to true
    val ownerID: String = "",
    val rules: String = "",
    val cancellationPolicy: String = "",
    val description: String = "Beautiful scenic campsite with modern facilities.",
    val amenities: List<String> = listOf("Fire Pit", "Picnic Table", "Water Hookup"),
    val availableDate: String = "2026-06-01",
    val hasActiveLottery: Boolean = true,
    val isWaitlistFull: Boolean = false,
    val isWinner: Boolean = false,
    val isLoser: Boolean = false
    
)
