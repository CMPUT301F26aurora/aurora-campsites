package com.example.auroracampsites.data.repository

import com.example.auroracampsites.data.models.Lottery
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

/** Creates and reads lottery configurations in Firestore. */
class LotteryRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {
    suspend fun createLottery(lottery: Lottery): String {
        require(lottery.campsiteId.isNotBlank() && lottery.ownerId.isNotBlank())
        require(lottery.numberOfWinners > 0)
        val start = LocalDate.parse(lottery.registrationStart)
        val end = LocalDate.parse(lottery.registrationEnd)
        val bookingStart = LocalDate.parse(lottery.bookingStart)
        val bookingEnd = LocalDate.parse(lottery.bookingEnd)
        require(!end.isBefore(start)) { "Invalid registration period." }
        require(bookingStart.isAfter(end)) { "Booking must start after registration ends." }
        require(!bookingEnd.isBefore(bookingStart)) { "Invalid booking period." }
        require(!end.isBefore(LocalDate.now())) { "Registration has already ended." }
        val campsiteRef = db.collection("campsites").document(lottery.campsiteId)
        val lotteryRef = db.collection("lotteries").document()
        db.runTransaction { transaction ->
            val campsite = transaction.get(campsiteRef)
            check(campsite.getBoolean("isDeleted") != true && campsite.getBoolean("deleted") != true) { "Campsite removed." }
            check(campsite.exists() && campsite.getString("ownerID") == lottery.ownerId) {
                "Only the campsite owner can create a lottery."
            }
            check(campsite.getString("activeLotteryId").isNullOrBlank()) {
                "This campsite already has an active lottery."
            }
            transaction.set(lotteryRef, lottery.copy(id = lotteryRef.id, status = "REGISTRATION"))
            transaction.update(campsiteRef, mapOf("activeLotteryId" to lotteryRef.id, "hasActiveLottery" to true))
        }.await()
        return lotteryRef.id
    }

    suspend fun getLotteryById(id: String): Lottery? {
        require(id.isNotBlank())
        val snapshot = db.collection("lotteries").document(id).get().await()
        return snapshot.toObject(Lottery::class.java)?.copy(id = snapshot.id)
    }
    /** Draws one round atomically, rejecting changes to the entrant set. */
    suspend fun drawLottery(campsiteId: String, ownerId: String, lotteryId: String): Int {
        require(campsiteId.isNotBlank() && ownerId.isNotBlank() && lotteryId.isNotBlank())
        val roundRef = db.collection("lotteries").document(lotteryId)
        val siteRef = db.collection("campsites").document(campsiteId)
        val before = roundRef.get(com.google.firebase.firestore.Source.SERVER).await()
        val version = before.getLong("membershipVersion") ?: 0L
        val entrants = roundRef.collection("waitlist")
            .get(com.google.firebase.firestore.Source.SERVER).await().documents
        check(entrants.size <= 499) { "Too many entrants for a single atomic draw." }
        val shuffled = entrants.toMutableList().apply { java.util.Collections.shuffle(this, java.security.SecureRandom()) }
        return db.runTransaction { tx ->
            val site = tx.get(siteRef)
            val snapshot = tx.get(roundRef)
            val round = requireNotNull(snapshot.toObject(Lottery::class.java))
            check(site.getBoolean("isDeleted") != true && site.getBoolean("deleted") != true) { "Campsite removed." }
            check(site.getString("ownerID") == ownerId && round.ownerId == ownerId &&
                    round.campsiteId == campsiteId && site.getString("activeLotteryId") == lotteryId) {
                "Only the current campsite owner can draw this lottery."
            }
            check(round.status == "REGISTRATION") { "Lottery has already been drawn." }
            check(LocalDate.now().isAfter(LocalDate.parse(round.registrationEnd))) {
                "Registration has not ended."
            }
            check(!LocalDate.now().isAfter(LocalDate.parse(round.bookingEnd))) { "Booking period has ended." }
            check((snapshot.getLong("membershipVersion") ?: 0L) == version) { "Entrants changed. Retry the draw." }
            check(round.numberOfWinners > 0)
            val entries = shuffled.map { tx.get(it.reference) }
            check(entries.all { it.exists() && it.getString("status") == "Waiting" }) { "Entrant data changed." }
            val count = minOf(round.numberOfWinners, entries.size)
            entries.forEachIndexed { index, entry ->
                tx.update(entry.reference, "status", if (index < count) "Won" else "Lost")
            }
            tx.update(roundRef, mapOf("status" to "DRAWN", "drawnAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(), "winnerCount" to count))
            count
        }.await()
    }
    /** Closes an expired round without removing historical records or bookings. */
    suspend fun closeLottery(campsiteId: String, ownerId: String, lotteryId: String) {
        require(campsiteId.isNotBlank() && ownerId.isNotBlank() && lotteryId.isNotBlank())
        val siteRef = db.collection("campsites").document(campsiteId)
        val roundRef = db.collection("lotteries").document(lotteryId)
        db.runTransaction { tx ->
            val site = tx.get(siteRef)
            val round = requireNotNull(tx.get(roundRef).toObject(Lottery::class.java))
            check(site.exists() && site.getBoolean("isDeleted") != true && site.getBoolean("deleted") != true) { "Campsite removed." }
            check(site.getString("ownerID") == ownerId && round.ownerId == ownerId &&
                    round.campsiteId == campsiteId && site.getString("activeLotteryId") == lotteryId) {
                "Only the current campsite owner can close this lottery."
            }
            check(round.status in setOf("REGISTRATION", "DRAWN")) { "Lottery is not open." }
            check(LocalDate.now().isAfter(LocalDate.parse(round.bookingEnd))) { "Booking period has not ended." }
            tx.update(roundRef, mapOf("status" to "CLOSED", "closedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()))
            tx.update(siteRef, mapOf("activeLotteryId" to "", "hasActiveLottery" to false))
        }.await()
    }
}

