package com.example.auroracampsites

class Rating (
    val id : String="",
    //this will map to the id in the user data class
    val userID : String="",
    //this will map to the id in the campsite data class
    val campsiteID: String="",
    val rating : Double = 0.0
)