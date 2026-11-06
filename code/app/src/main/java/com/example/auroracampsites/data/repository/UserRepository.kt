package com.example.auroracampsites.data.repository

import androidx.compose.runtime.mutableStateListOf
import com.example.auroracampsites.data.models.User
import com.example.auroracampsites.data.models.UserRole
import com.google.firebase.Firebase
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore

/**
- A class that handles firebase storage of users. Used to manage users information.
 */
class UserRepository {
    private val db = Firebase.firestore
    private val userRef = db.collection("users")
    private val _users = mutableStateListOf(
        User(
            "Admin1",
            "admin_user",
            "placeholder@email.com",
            "582-110-2224",
            UserRole.ADMIN,
            true
        )
    )

    //get user list
    val users: List<User>
        get() = _users

    /**
     * addUser: Registers a user to the database
     */
    fun addUser(user: User) {
        userRef.document(user.id).set(user)
    }

    /**
     * updateUserUsername: Updates user username using merge to auto-create if missing.
     */
    fun updateUserUsername(user: User, newUsername: String) {
        userRef.document(user.id).set(mapOf("username" to newUsername), SetOptions.merge())
    }

    /**
     * updateUserEmail: Updates user email using merge.
     */
    fun updateUserEmail(user: User, newEmail: String) {
        userRef.document(user.id).set(mapOf("email" to newEmail), SetOptions.merge())
    }

    /**
     * updateUserPhoneNumber: Updates user phone number using merge.
     */
    fun updateUserPhoneNumber(user: User, newPhoneNumber: String) {
        userRef.document(user.id).set(mapOf("phoneNumber" to newPhoneNumber), SetOptions.merge())
    }

    /**
     * updateUserNotifs: Flips the permissions for notifications.
     */
    fun updateUserNotifs(user: User) {
        userRef.document(user.id).get()
            .addOnSuccessListener { document ->
                val currentBoolean = document.getBoolean("notificationsEnabled") ?: true
                val newBoolean = currentBoolean.not()
                userRef.document(user.id).set(mapOf("notificationsEnabled" to newBoolean), SetOptions.merge())
            }
    }

    /**
     * deleteUser: Deletes a user from the database.
     */
    fun deleteUser(user: User) {
        userRef.document(user.id).delete()
    }
}