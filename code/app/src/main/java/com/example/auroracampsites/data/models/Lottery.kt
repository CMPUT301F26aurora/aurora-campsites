package com.example.auroracampsites.data.models

/**
 * A data class that represents a lottery.
 *
 * Used to handle a lottery and what it stores
 *
 * @property id The unique ID of the message.
 * @property ownerId The ID of the owner of the lottery.
 * @property entrants A list of lottery entrants in the form of their IDs.
 * @property campsites A list of campsites entered into the lottery.
 * @property isActive If the lottery is active.
 * @property endTime The time when the lottery ends and a winner is drawn.
 * @property hasWinner If the lottery has a winner
 * @property lotteryWinner The winner of the lottery as a String.
 */
data class Lottery (
    val id: String = "",
    val ownerId: String = "", //this holds the info for the owner of the lottery
    val entrants: List<String> = emptyList(), //holds the info of users entered in
    val campsites: List<String> = emptyList(), //holds the info of campsites entered in
    val isActive: Boolean = false,
    val endTime: String = "",
    val hasWinner: Boolean = false,
    val lotteryWinner: String = "" //holds the winner of the lottery
)