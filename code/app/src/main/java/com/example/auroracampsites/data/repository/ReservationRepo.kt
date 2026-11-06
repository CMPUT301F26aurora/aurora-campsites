package com.example.auroracampsites.data.repository

import com.example.auroracampsites.data.models.Reservation
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ReservationRepo(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    /**
     * Saves a new reservation to Firestore.
     */
    suspend fun addReservation(reservation: Reservation): Boolean {
        return try {
            val docRef = db.collection("reservations").document()
            val reservationWithId = reservation.copy(id = docRef.id)
            docRef.set(reservationWithId).await()
            true
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
                        status = doc.getString("status") ?: "ACTIVE"
                    )
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Updates reservation status to CANCElLED.
     */
    suspend fun cancelReservation(reservationId: String): Boolean {
        return try {
            db.collection("reservations").document(reservationId)
                .update("status", "CANCELLED").await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}