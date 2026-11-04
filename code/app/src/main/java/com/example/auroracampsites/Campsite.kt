package com.example.auroracampsites

data class Campsite (
    val id: String="",
    val name: String="",
    val location: String="",
    val maxOccupancy: Int=0,
    val price: Double=0.0,
    val rating: Double=0.0,
    //this will map to user ID where isOwner is set to true
    val ownerID: String="",
    val rules: String="",
    val cancellationPolicy: String=""
)