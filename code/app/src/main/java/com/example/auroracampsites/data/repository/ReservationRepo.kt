package com.example.auroracampsites.data.repository

import com.example.auroracampsites.data.models.Reservation
import com.example.auroracampsites.data.models.Campsite
import com.example.auroracampsites.data.models.Lottery
import java.time.LocalDate
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ReservationRepo(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    /** Claims a winning entry and locks one available date atomically. */
    suspend fun saveReservation(reservation: Reservation): String {
        require(reservation.userId.isNotBlank() && reservation.campsiteId.isNotBlank() && reservation.lotteryId.isNotBlank())
        val date = LocalDate.parse(reservation.startDate)
        require(reservation.startDate == date.toString() && reservation.endDate == reservation.startDate) {
            "Select one valid reservation date."
        }
        val siteRef = db.collection("campsites").document(reservation.campsiteId)
        val roundRef = db.collection("lotteries").document(reservation.lotteryId)
        val entryRef = roundRef.collection("waitlist").document(reservation.userId)
        val ref = db.collection("reservations").document()
        return db.runTransaction { tx ->
            val site = requireNotNull(tx.get(siteRef).toObject(Campsite::class.java)) { "Campsite not found." }
            val round = requireNotNull(tx.get(roundRef).toObject(Lottery::class.java)) { "Lottery not found." }
            val entry = tx.get(entryRef)
            val today = LocalDate.now()
            check(!site.isDeleted && tx.get(siteRef).getBoolean("isDeleted") != true && tx.get(siteRef).getBoolean("deleted") != true) { "Campsite removed." }
            check(site.activeLotteryId == reservation.lotteryId && round.campsiteId == reservation.campsiteId && round.ownerId == site.ownerID) { "Lottery is no longer active." }
            check(round.status == "DRAWN") { "Lottery has not been drawn." }
            check(!today.isBefore(LocalDate.parse(round.bookingStart)) && !today.isAfter(LocalDate.parse(round.bookingEnd))) { "Booking period is closed." }
            check(entry.getString("entrantID") == reservation.userId && entry.getString("status") == "Won") { "You must win the lottery before booking." }
            check(entry.getString("reservationId").isNullOrBlank()) { "You have already claimed this lottery." }
            check(!date.isBefore(today) && reservation.startDate in site.availableDates && reservation.startDate !in site.bookedDates) { "Date unavailable." }
            tx.set(ref, reservation.copy(id = ref.id, campsiteName = site.name,
                location = site.location, maximumOccupancy = site.maxOccupancy,
                price = site.price, status = "ACTIVE"))
            tx.update(siteRef, "bookedDates", (site.bookedDates + reservation.startDate).distinct())
            tx.update(entryRef, "reservationId", ref.id)
            ref.id
        }.await()
    }

    suspend fun getReservationById(id: String, userId: String): Reservation? {
        require(id.isNotBlank() && userId.isNotBlank())
        val doc = db.collection("reservations").document(id).get().await()
        if (doc.getString("userId") != userId) return null
        return doc.toObject(Reservation::class.java)?.copy(id = doc.id)
    }

    /**
     * Saves a new reservation to Firestore.
     */
    suspend fun addReservation(reservation: Reservation): Boolean {
        return try {
            saveReservation(reservation)
            true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Real-time listener for a user's reservations.
     */
    suspend fun getAllReservationsOnce(): List<Reservation> {
        return try {
            val snapshot = db.collection("reservations").get().await()
            snapshot.documents.mapNotNull { doc ->
                try {
                    Reservation(
                        id = doc.id,
                        userId = doc.getString("userId") ?: "",
                        campsiteId = doc.getString("campsiteId") ?: "",
                        campsiteName = doc.getString("campsiteName") ?: "",
                        location = doc.getString("location") ?: "",
                        maximumOccupancy = (doc.get("maximumOccupancy") as? Number)?.toInt() ?: 1,
                        price = (doc.get("price") as? Number)?.toDouble() ?: 0.0,
                        startDate = doc.getString("startDate") ?: "",
                        endDate = doc.getString("endDate") ?: "",
                        status = doc.getString("status") ?: "ACTIVE",
                        lotteryId = doc.getString("lotteryId") ?: ""
                    )
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /** Legacy entry point cannot safely cancel without a caller identity. */
    @Deprecated("Use cancelUserReservation with the current user ID")
    suspend fun cancelReservation(reservationId: String): Boolean = false

    /** Loads only the current user's reservations. */
    suspend fun getUserReservations(userId: String): List<Reservation> {
        require(userId.isNotBlank())
        return db.collection("reservations").whereEqualTo("userId", userId)
            .get().await().documents.map { doc ->
                requireNotNull(doc.toObject(Reservation::class.java)).copy(id = doc.id)
            }
    }

    /** Cancels a single-date lottery reservation and releases its date atomically. */
    suspend fun cancelUserReservation(reservationId: String, userId: String) {
        require(reservationId.isNotBlank() && userId.isNotBlank())
        val ref = db.collection("reservations").document(reservationId)
        db.runTransaction { transaction ->
            val reservation = requireNotNull(transaction.get(ref).toObject(Reservation::class.java)) {
                "Reservation not found."
            }
            check(reservation.userId == userId) { "Reservation not found." }
            check(reservation.status == "ACTIVE") { "Reservation is not active." }
            val date = LocalDate.parse(reservation.startDate)
            check(!date.isBefore(LocalDate.now())) { "Past reservations cannot be cancelled." }
            check(reservation.lotteryId.isNotBlank() && reservation.campsiteId.isNotBlank() &&
                    reservation.startDate == date.toString() && reservation.endDate == reservation.startDate) {
                "Legacy reservation requires manual reconciliation."
            }
            val siteRef = db.collection("campsites").document(reservation.campsiteId)
            val entryRef = db.collection("lotteries").document(reservation.lotteryId)
                .collection("waitlist").document(userId)
            val site = requireNotNull(transaction.get(siteRef).toObject(Campsite::class.java)) {
                "Campsite not found."
            }
            val entry = transaction.get(entryRef)
            check(entry.getString("entrantID") == userId && entry.getString("reservationId") == reservationId) {
                "Reservation claim could not be verified."
            }
            check(reservation.startDate in site.bookedDates) { "Date lock could not be verified." }
            transaction.update(ref, "status", "CANCELLED")
            transaction.update(siteRef, "bookedDates", site.bookedDates.filterNot { it == reservation.startDate })
            // Keep the claim: cancellation does not grant another booking in this round.
        }.await()
    }
}
