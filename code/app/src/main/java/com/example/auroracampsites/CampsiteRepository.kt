package com.example.auroracampsites

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class CampsiteRepository(val name: String, val location: String, var rating: Int) {
    private val db = Firebase.firestore
    private val campsitesRef = db.collection("campsites")
    private val _campsites = mutableStateListOf(
        Campsite("Happy Meadows", "56 Acreage Road","John Doe")
    )

    //get campsite list
    val campsites: List<Campsite>
        get() = _campsites

    //add campsite
    fun addCampsite(campsite: Campsite){
        campsitesRef.document(campsite.name).set(campsite)
    }

    //delete campsite
    fun deleteCampsite(campsite: Campsite){
        campsitesRef.document(campsite.name).delete()
    }
}