package com.example.auroracampsites.data.repository

import com.example.auroracampsites.data.models.Campsite
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class CampsiteRepository {
    private val db = Firebase.firestore
    private val campsitesRef = db.collection("campsites")

    /**
     * One-time fetch of all campsites from Firestore using Kotlin Coroutines.
     */
    suspend fun fetchCampsites(): List<Campsite> {
        return try {
            val snapshot = campsitesRef.get().await()
            snapshot.documents.filterNot { it.getBoolean("deleted") == true || it.getBoolean("isDeleted") == true }.mapNotNull { it.toObject(Campsite::class.java)?.copy(id = it.id) } .filterNot { it.isDeleted }
        } catch (e: CancellationException) { throw e }
        catch (e: Exception) {
            emptyList()
        }
    }
    /**
     * Gets a specific campsite from Firebase using its ID.
     */
    suspend fun getCampsiteById(campsiteId: String): Campsite? {
        if (campsiteId.isBlank()) return null

        var document = campsitesRef.document(campsiteId).get().await()
        if (document.exists() && document.getBoolean("isDeleted") != true &&
            document.getBoolean("deleted") != true && document.getLong("ratingCount") == null) {
            try {
                RatingRepository(db).initializeRatingSummary(campsiteId)
                document = campsitesRef.document(campsiteId).get().await()
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) {
                android.util.Log.e("CampsiteRepository", "Rating initialization failed", e)
            }
        }

        if (document.getBoolean("deleted") == true || document.getBoolean("isDeleted") == true) return null
        return document.toObject(Campsite::class.java)
            ?.copy(id = document.id)?.takeUnless { it.isDeleted }
    }

    /**
     * Real-time listener: Returns a Flow that emits updated campsite lists whenever Firestore updates.
     */
    fun getCampsitesRealtime(): Flow<List<Campsite>> = observe(campsitesRef)

    fun getOwnerCampsitesRealtime(ownerId: String): Flow<List<Campsite>> {
        require(ownerId.isNotBlank())
        return observe(campsitesRef.whereEqualTo("ownerID", ownerId))
    }

    private fun observe(query: Query): Flow<List<Campsite>> = callbackFlow {
        val subscription = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val campsites = snapshot.documents.filterNot { it.getBoolean("deleted") == true || it.getBoolean("isDeleted") == true }.mapNotNull { it.toObject(Campsite::class.java)?.copy(id = it.id) } .filterNot { it.isDeleted }
                trySend(campsites)
            }
        }
        awaitClose { subscription.remove() }
    }

    /**
     * Adds or updates a campsite in Firestore.
     */
    suspend fun addCampsite(campsite: Campsite) {
        val ref = campsitesRef.document(campsite.id)
        db.runTransaction { tx ->
            check(!tx.get(ref).exists()) { "Campsite already exists." }
            tx.set(ref, campsite)
        }.await()
    }

    /**
     * Deletes a campsite from Firestore.
     */
    suspend fun deleteCampsite(campsite: Campsite, actorId: String, admin: Boolean = false) {
        require(campsite.id.isNotBlank() && actorId.isNotBlank())
        val ref = campsitesRef.document(campsite.id)
        // Legacy reservations may not have a date lock. Refuse removal if any current booking exists.
        val reservations = db.collection("reservations").whereEqualTo("campsiteId", campsite.id)
            .get(com.google.firebase.firestore.Source.SERVER).await()
        check(reservations.documents.none {
            it.getString("status") != "CANCELLED" &&
                    (runCatching { java.time.LocalDate.parse(it.getString("endDate")) }.getOrNull()
                        ?.isBefore(java.time.LocalDate.now()) != true)
        }) { "This campsite has active reservations." }
        db.runTransaction { tx ->
            val snapshot = tx.get(ref)
            check(snapshot.getBoolean("deleted") != true || snapshot.getBoolean("isDeleted") == true) { "Campsite removed." }
            val current = requireNotNull(snapshot.toObject(Campsite::class.java))
            if (admin) {
                val actor = tx.get(db.collection("users").document(actorId))
                check(actor.getString("role") == "ADMIN") { "Administrator required." }
            } else check(current.ownerID == actorId) { "Not your campsite." }
            check(!current.isDeleted) { "Campsite already removed." }
            check(current.activeLotteryId.isBlank()) { "Close the active lottery first." }
            check(current.bookedDates.none { date ->
                runCatching { java.time.LocalDate.parse(date).isBefore(java.time.LocalDate.now()) }.getOrDefault(false).not()
            }) { "This campsite has booked dates." }
            tx.update(ref, mapOf("deleted" to true, "isDeleted" to true, "hasActiveLottery" to false))
        }.await()
    }
    /** Updates editable fields without overwriting reservation or lottery state. */
    suspend fun updateOwnerCampsite(ownerId: String, campsite: Campsite) {
        require(ownerId.isNotBlank() && campsite.id.isNotBlank())
        val ref = campsitesRef.document(campsite.id)
        db.runTransaction { transaction ->
            val current = requireNotNull(transaction.get(ref).toObject(Campsite::class.java))
            check(!current.isDeleted && transaction.get(ref).getBoolean("isDeleted") != true && transaction.get(ref).getBoolean("deleted") != true) { "Campsite removed." }
            check(current.ownerID == ownerId) { "Not your campsite." }
            check(campsite.availableDates.containsAll(current.bookedDates)) { "Keep all booked dates." }
            transaction.update(ref, mapOf<String, Any>(
                "name" to campsite.name, "location" to campsite.location,
                "description" to campsite.description, "maxOccupancy" to campsite.maxOccupancy,
                "price" to campsite.price, "availableDates" to campsite.availableDates,
                "cancellationPolicy" to campsite.cancellationPolicy, "rules" to campsite.rules,
                "amenities" to campsite.amenities
            ))
        }.await()
    }
}
