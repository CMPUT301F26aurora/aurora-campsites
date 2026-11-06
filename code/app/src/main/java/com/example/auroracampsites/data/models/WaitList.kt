package com.example.auroracampsites.data.models
import com.google.firebase.Timestamp


data class WaitList (
    val id : String="",
    //this will map to ID in user data class
    val entrantID: String="",
    val joinedAt: Timestamp?=null,
    val status: String = "Waiting"
)