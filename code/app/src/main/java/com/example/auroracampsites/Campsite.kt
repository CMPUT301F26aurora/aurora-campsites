package com.example.auroracampsites

data class Campsite (
    val id: String="",
    val name: String="",
    val location: String="",
    val maxOccupancy: Int=0,
    val price:Double=00,
    //ownerID will map to UserID of owners
    val ownerID: String="",
    val rating:Double=0.0,
    val rules: String="",
    val cancellationPolicy="",
    val campsiteOwner: ""
)