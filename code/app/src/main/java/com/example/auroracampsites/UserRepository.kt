package com.example.auroracampsites

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import java.util.function.Predicate.not

/**
 * A class that handles firebase storage of users. Used to manage users information.
 */
class UserRepository() {
    private val db = Firebase.firestore
    private val userRef = db.collection("users")
    private val _users = mutableStateListOf(
        User("Admin1","placeholder@email.com","placeholder@email.com","582-110-2224",true,true,true,true )
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
     * updateUserUsername: Updates user username.
     *
     * Updates a username, when given a new value.
     *
     * @param user A user value.
     * @param newUsername A provided username string.
     */
    fun updateUserUsername(user:User,newUsername:String){
        userRef.document(user.id).update("username",newUsername)
    }

    /**
     * updateUserEmail: Updates user email.
     *
     * Updates a username, when given a new value.
     *
     * @param user A user value.
     * @param newEmail A provided username string.
     */
    fun updateUserEmail(user:User,newEmail:String){
        userRef.document(user.id).update("email",newEmail)
    }
    /**
     * updateUserPhoneNumber: Updates user email.
     *
     * Updates a phone number, when given a new value.
     *
     * @param user A user value.
     * @param newPhoneNumber A provided phone number string.
     */
    fun updateUserPhoneNumber(user:User,newPhoneNumber:String){
        userRef.document(user.id).update("phoneNumber",newPhoneNumber)
    }

    /**
     * updateUserPhoneNumber: Updates user email.
     *
     * Flips the permissions for notifications.
     *
     * @param user A user value.
     */
    fun updateUserNotifs(user:User){
        userRef.document(user.id).get()
            .addOnSuccessListener { document ->
                val currentBoolean =  document.getBoolean("notificationsEnabled")
                val newBoolean=currentBoolean?.not()
                userRef.document(user.id).update("notificationsEnabled",newBoolean)
            }

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