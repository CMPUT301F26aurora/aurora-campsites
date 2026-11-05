package com.example.auroracampsites

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

/**
 * A class that handles firebase storage of users. Used to manage users information.
 */
class UserRepository() {
    private val db = Firebase.firestore
    private val userRef = db.collection("users")
    private val _users = mutableStateListOf(
        User("Admin1","placeholder@email.com","123-456-7890","123-456-7890")
    )

    //get user list
    val users: List<User>
        get() = _users

    /**
     * addUser: Registers a user to the database
     *
     * Takes in a user value, and adds it to the database using its ID.
     *
     * @param user A user value.
     */
    fun addUser(user: User){
        userRef.document(user.id).set(user)
    }

    /**
     * deleteUser: Deletes a user from the database.
     *
     * Takes in a user value, and deletes it from the database using its ID.
     *
     * @param user A user value.
     */
    fun deleteUser(user: User){
        userRef.document(user.id).delete()
    }
}