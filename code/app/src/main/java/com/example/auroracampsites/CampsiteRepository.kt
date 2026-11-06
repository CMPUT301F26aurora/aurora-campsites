package com.example.auroracampsites

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import kotlin.String

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
        Campsite("1","Happy Meadows","56 Acreage Road, Banff, Alberta, Canada",10,500.0,4.0,"Admin1","No Littering","No Cancellations","Beautiful scenic campsite with modern facilities.",listOf("Fire Pit", "Picnic Table", "Water Hookup"),"2026-06-01",false,false,false,false)
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
     * updateCampsiteStringField: Updates a string value within a campsite object.
     *
     * @param campsite A campsite object.
     * @param targetField The name of the target field.
     * @param newValue A new value.
     */
    fun updateCampsiteStringField(campsite: Campsite,targetField:String, newValue:String){
        campsitesRef.document(campsite.id).update(targetField,newValue)
    }

    /**
     * updateCampsiteIntegerField: Updates a Integer value within a campsite object.
     *
     * @param campsite A campsite object.
     * @param targetField The name of the target field.
     * @param newValue A new value.
     */
    fun updateCampsiteIntField(campsite: Campsite,targetField:String, newValue: Int){
        campsitesRef.document(campsite.id).update(targetField,newValue)
    }

    /**
     * updateCampsiteDoubleField: Updates a double value within a campsite object.
     *
     * @param campsite A campsite object.
     * @param targetField The name of the target field.
     * @param newValue A new value.
     */
    fun updateCampsiteDoubleField(campsite: Campsite,targetField:String, newValue: Double){
        campsitesRef.document(campsite.id).update(targetField,newValue)
    }

    /**
     * addCampsiteAmenity: Adds an amenity to the list.
     *
     * @param campsite A campsite object.
     * @param newAmenity A new value.
     */
    fun addCampsiteAmenity(campsite: Campsite,newAmenity:String){
        campsitesRef.document(campsite.id).update(
            "amenities", FieldValue.arrayUnion(newAmenity)
        )
    }

    /**
     * addCampsiteAmenity: Removes an amenity from the list.
     *
     * @param campsite A campsite object.
     * @param removeAmenity A new value.
     */
    fun removeCampsiteAmenity(campsite: Campsite,removeAmenity:String){
        campsitesRef.document(campsite.id).update(
            "amenities", FieldValue.arrayRemove(removeAmenity)
        )
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