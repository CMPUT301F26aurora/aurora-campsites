package com.example.auroracampsites.data.models

/** Stores the configuration of one campsite lottery. */
data class Lottery(
    val id: String = "",
    val campsiteId: String = "",
    val ownerId: String = "",
    val registrationStart: String = "",
    val registrationEnd: String = "",
    val bookingStart: String = "",
    val bookingEnd: String = "",
    val numberOfWinners: Int = 0,
    val status: String = "REGISTRATION"
)
