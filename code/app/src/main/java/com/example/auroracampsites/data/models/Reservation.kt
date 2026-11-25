package com.example.auroracampsites.data.models

/** Represents a campsite reservation. */
data class Reservation(
    val id: String = "",
    val userId: String = "",
    val campsiteId: String = "",
    val campsiteName: String = "",
    val location: String = "",
    val maximumOccupancy: Int = 1,
    val price: Double = 0.0,
    val startDate: String = "",
    val endDate: String = "",
    val status: String = "ACTIVE",
    val lotteryId: String = ""
)



