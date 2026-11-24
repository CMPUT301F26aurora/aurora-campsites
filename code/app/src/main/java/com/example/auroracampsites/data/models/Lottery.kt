package com.example.auroracampsites.data.models

data class Lottery (
    val id: String = "",
    val ownerId: String = "", //this holds the info for the owner of the lottery
    val entrants: List<String> = emptyList(), //holds the info of users entered in
    val isActive: Boolean = false,
    val hasWinner: Boolean = false,
    val lotteryWinner: String = "" //holds the winner of the lottery
)