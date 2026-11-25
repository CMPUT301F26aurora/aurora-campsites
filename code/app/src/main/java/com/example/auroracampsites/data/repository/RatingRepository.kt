package com.example.auroracampsites.data.repository

import com.example.auroracampsites.data.models.Rating
import com.example.auroracampsites.data.models.Reservation
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

/** US 01.13.01: Save one review per completed reservation and update its campsite rating. */
class RatingRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {
    suspend fun hasReview(reservationId: String): Boolean {
        require(reservationId.isNotBlank())
        return db.collection("ratings").document(reservationId).get().await().exists()
    }

    // Initialize legacy ratings once; concurrent new reviews use the same aggregate marker.
    suspend fun initializeRatingSummary(campsiteId: String) {
        require(campsiteId.isNotBlank())
        val ref = db.collection("campsites").document(campsiteId)
        val before = ref.get(Source.SERVER).await()
        if (!before.exists() || before.getBoolean("isDeleted") == true || before.getBoolean("deleted") == true) return
        if (before.getLong("ratingCount") != null && before.getDouble("ratingSum") != null) return
        val scores = db.collection("ratings").whereEqualTo("campsiteID", campsiteId)
            .get(Source.SERVER).await().documents.map { doc ->
                requireNotNull(doc.getDouble("rating")).also { check(it in 1.0..5.0) }
            }
        if (scores.isEmpty()) return
        db.runTransaction { tx ->
            val site = tx.get(ref)
            if (site.exists() && site.getBoolean("isDeleted") != true && site.getBoolean("deleted") != true) {
                val count = site.getLong("ratingCount")
                val sum = site.getDouble("ratingSum")
                check((count == null) == (sum == null)) { "Incomplete rating aggregate." }
                if (count == null) {
                    tx.update(ref, mapOf("ratingCount" to scores.size.toLong(),
                        "ratingSum" to scores.sum(), "rating" to scores.average()))
                }
            }
            Unit
        }.await()
    }

    suspend fun postReview(reservationId: String, userId: String, rating: Int, comment: String) {
        require(reservationId.isNotBlank() && userId.isNotBlank() && rating in 1..5)
        require(comment.length <= 5000)
        val reservationRef = db.collection("reservations").document(reservationId)
        val reviewRef = db.collection("ratings").document(reservationId)
        val initial = requireNotNull(reservationRef.get(Source.SERVER).await().toObject(Reservation::class.java))
        check(initial.userId == userId && initial.campsiteId.isNotBlank())
        val campsiteRef = db.collection("campsites").document(initial.campsiteId)
        // Read the aggregate marker before querying legacy reviews. A concurrent first review
        // initializes the marker in the same transaction, so retries use its stored totals.
        val before = campsiteRef.get(Source.SERVER).await()
        val legacy = if (before.getLong("ratingCount") == null) {
            db.collection("ratings").whereEqualTo("campsiteID", initial.campsiteId)
                .get(Source.SERVER).await().documents.map { doc ->
                    requireNotNull(doc.getDouble("rating")).also { check(it in 1.0..5.0) }
                }
        } else emptyList()
        db.runTransaction { transaction ->
            val reservation = requireNotNull(transaction.get(reservationRef).toObject(Reservation::class.java))
            check(reservation.userId == userId && reservation.status == "ACTIVE")
            check(LocalDate.parse(reservation.endDate).isBefore(LocalDate.now()))
            check(reservation.campsiteId == initial.campsiteId)
            val campsite = transaction.get(campsiteRef)
            check(campsite.exists() && campsite.getBoolean("deleted") != true && campsite.getBoolean("isDeleted") != true)
            check(!transaction.get(reviewRef).exists()) { "Review already exists." }
            val storedCount = campsite.getLong("ratingCount")
            val storedSum = campsite.getDouble("ratingSum")
            check((storedCount == null) == (storedSum == null)) { "Incomplete rating aggregate." }
            val count = (storedCount ?: legacy.size.toLong()) + 1L
            val sum = (storedSum ?: legacy.sum()) + rating.toDouble()
            transaction.set(reviewRef, Rating(reservationId, userId, reservation.campsiteId,
                rating.toDouble(), comment.trim(), reservationId))
            transaction.update(campsiteRef, mapOf("ratingCount" to count, "ratingSum" to sum, "rating" to sum / count))
            Unit
        }.await()
    }
}
