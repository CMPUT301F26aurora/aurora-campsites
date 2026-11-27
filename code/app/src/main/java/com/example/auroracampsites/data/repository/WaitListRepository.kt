package com.example.auroracampsites.data.repository

import com.example.auroracampsites.data.models.Campsite
import com.example.auroracampsites.data.models.WaitList
import com.example.auroracampsites.data.models.Lottery
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

/** Stores entrants separately for each lottery round. */
class WaitListRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {
    private fun lotteryRef(id: String) = db.collection("lotteries").document(id)

    suspend fun getActiveLottery(campsiteId: String): Lottery? {
        require(campsiteId.isNotBlank())
        val site = db.collection("campsites").document(campsiteId).get().await()
        if (site.getBoolean("isDeleted") == true || site.getBoolean("deleted") == true) return null
        val id = site.getString("activeLotteryId")?.takeIf { it.isNotBlank() } ?: return null
        val lottery = lotteryRef(id).get().await().toObject(Lottery::class.java)?.copy(id = id)
        return lottery?.takeIf { it.campsiteId == campsiteId }
    }

    fun registrationOpen(lottery: Lottery): Boolean {
        val today = LocalDate.now()
        return lottery.status == "REGISTRATION" &&
                !today.isBefore(LocalDate.parse(lottery.registrationStart)) &&
                !today.isAfter(LocalDate.parse(lottery.registrationEnd))
    }

    suspend fun joinWaitList(campsiteId: String, userId: String, lotteryId: String) {
        changeEntry(campsiteId, userId, lotteryId, true)
    }

    suspend fun leaveWaitList(campsiteId: String, userId: String, lotteryId: String) {
        changeEntry(campsiteId, userId, lotteryId, false)
    }

    private suspend fun changeEntry(campsiteId: String, userId: String, lotteryId: String, join: Boolean) {
        require(campsiteId.isNotBlank() && userId.isNotBlank() && lotteryId.isNotBlank())
        val siteRef = db.collection("campsites").document(campsiteId)
        val roundRef = lotteryRef(lotteryId)
        val entryRef = roundRef.collection("waitlist").document(userId)
        db.runTransaction { tx ->
            val site = tx.get(siteRef)
            val round = requireNotNull(tx.get(roundRef).toObject(Lottery::class.java))
            val entry = tx.get(entryRef)
            check(site.getBoolean("isDeleted") != true && site.getBoolean("deleted") != true) { "Campsite removed." }
            check(site.getString("activeLotteryId") == lotteryId && round.campsiteId == campsiteId)
            check(registrationOpen(round)) { "Registration is closed." }
            if (join && !entry.exists()) {
                tx.set(entryRef, WaitList(userId, userId, Timestamp.now(), "Waiting"))
            } else if (!join && entry.exists()) {
                check(entry.getString("status") == "Waiting")
                tx.delete(entryRef)
            }
            // Update the parent so a future draw can detect concurrent membership changes.
            if ((join && !entry.exists()) || (!join && entry.exists())) {
                tx.update(roundRef, "membershipVersion", com.google.firebase.firestore.FieldValue.increment(1L))
            }
        }.await()
    }

    suspend fun getWaitListEntry(campsiteId: String, userId: String, lotteryId: String? = null): WaitList? {
        require(userId.isNotBlank())
        val id = lotteryId ?: getActiveLottery(campsiteId)?.id ?: return null
        val round = lotteryRef(id).get().await()
        if (round.getString("campsiteId") != campsiteId) return null
        val doc = lotteryRef(id).collection("waitlist").document(userId).get().await()
        return doc.toObject(WaitList::class.java)?.copy(id = doc.id)
    }

    /**
     * Reads one owned lottery's entrants directly from the server.
     * The ownership check scopes the client request. Firestore rules still control access.
     */
    suspend fun getOwnerWaitlist(campsiteId: String, lotteryId: String, ownerId: String): List<WaitList> {
        require(campsiteId.isNotBlank() && lotteryId.isNotBlank() && ownerId.isNotBlank())
        val campsiteDocument = db.collection("campsites").document(campsiteId).get(Source.SERVER).await()
        val campsite = campsiteDocument.takeUnless { it.getBoolean("deleted") == true }
            ?.toObject(Campsite::class.java)?.copy(id = campsiteDocument.id)
        val roundRef = lotteryRef(lotteryId)
        val lotteryDocument = roundRef.get(Source.SERVER).await()
        val lottery = lotteryDocument.takeUnless {
            it.getBoolean("deleted") == true || it.getBoolean("isDeleted") == true
        }?.toObject(Lottery::class.java)
        requireOwnedLottery(campsite, lottery, ownerId)

        return roundRef.collection("waitlist").get(Source.SERVER).await().documents.map { document ->
            requireNotNull(document.toObject(WaitList::class.java)).copy(id = document.id)
        }
    }

    suspend fun getUserWaitListEntries(userId: String): List<Pair<Lottery, WaitList>> {
        require(userId.isNotBlank())
        val docs = db.collectionGroup("waitlist").whereEqualTo("entrantID", userId).get().await()
        val results = mutableListOf<Pair<Lottery, WaitList>>()
        for (doc in docs.documents) {
            val parent = doc.reference.parent.parent ?: continue
            if (parent.parent.path != "lotteries") continue
            val round = parent.get().await().toObject(Lottery::class.java)?.copy(id = parent.id) ?: continue
            val entry = doc.toObject(WaitList::class.java)?.copy(id = doc.id) ?: continue
            results.add(round to entry)
        }
        return results
    }
}

