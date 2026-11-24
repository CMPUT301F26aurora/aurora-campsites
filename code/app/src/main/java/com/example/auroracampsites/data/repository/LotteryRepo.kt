package com.example.auroracampsites.data.repository

import com.example.auroracampsites.data.models.Lottery
import com.example.auroracampsites.data.models.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class LotteryRepo (
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
){
    private val userRef = db.collection("users")
    private val campsitesRef = db.collection("campsites")

    private val lotteryRef = db.collection("lotteries")

    /**
     * Saves a new lottery to Firestore.
     */
    suspend fun createLottery(lottery: Lottery, user: User):Boolean {
        return try {
            val lottoRef = lotteryRef.document()
            val lotteryWithId = lottery.copy(id = lottoRef.id)
            lottoRef.set(lotteryWithId).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Draws a lottery winner
     *
     * ALERT: this still needs testing done.
     */
    fun drawLotteryWinner(lottery: Lottery, lottoID: String): String{
        val lottoRef = lotteryRef.document(lottoID).get().addOnSuccessListener { document ->
            val userList = document.get("entrants") as? List<*>
            val userListLength = userList?.size ?: 0
            val randomWinnerNum = (0.. userListLength).random()
            return@addOnSuccessListener userList?.get(randomWinnerNum) as Unit
        }.addOnFailureListener { exception ->
            println("Error found: $exception")
        }
        return "0"

    }
}