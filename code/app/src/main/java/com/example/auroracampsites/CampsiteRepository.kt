package com.example.auroracampsites

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

/**
 * The repository for the campsites. Manages functions relating to manipulating campsites
 */
class MyClass(
    val name: String
)
class CampsiteRepository() {
    private val db = Firebase.firestore
    private val campsitesRef = db.collection("campsites")
    private val _campsites = mutableStateListOf(
        Campsite("Happy Meadows", "56 Acreage Road","John Doe")
    )

    //get campsite list
    val campsites: List<Campsite>
        get() = _campsites

    /**
     * addCampsite: Adds a campsite to the database.
     *
     * Takes in a campsite value, and adds it to the database using its name
     *
     * @param campsite A campsite object.
     */
    //add campsite
    fun addCampsite(campsite: Campsite){
        campsitesRef.document(campsite.id).set(campsite)
    }

    /**
     * deleteCampsite: Removes a campsite from the database
     *
     * Takes in a campsite value, and removes the campsite from the database.
     *
     * @param campsite A campsite object.
     */
    //delete campsite
    fun deleteCampsite(campsite: Campsite){
        campsitesRef.document(campsite.id).delete()
    }
}