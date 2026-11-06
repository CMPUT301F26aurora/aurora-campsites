package com.example.auroracampsites.data.repository

import com.example.auroracampsites.data.models.Campsite
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
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
            snapshot.toObjects(Campsite::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Real-time listener: Returns a Flow that emits updated campsite lists whenever Firestore updates.
     */
    fun getCampsitesRealtime(): Flow<List<Campsite>> = callbackFlow {
        val subscription = campsitesRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val campsites = snapshot.toObjects(Campsite::class.java)
                trySend(campsites)
            }
        }
        awaitClose { subscription.remove() }
    }

    /**
     * Adds or updates a campsite in Firestore.
     */
    suspend fun addCampsite(campsite: Campsite) {
        campsitesRef.document(campsite.id).set(campsite).await()
    }

    /**
     * Deletes a campsite from Firestore.
     */
    suspend fun deleteCampsite(campsite: Campsite) {
        campsitesRef.document(campsite.id).delete().await()
    }
}