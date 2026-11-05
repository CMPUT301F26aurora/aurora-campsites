package com.example.auroracampsites

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class UserRepository(val username: String, val email: String, val phonenumber: String, permissionlevel: Int) {
    private val db = Firebase.firestore
    private val userRef = db.collection("users")
    private val _users = mutableStateListOf(
        User("Admin1","placeholder@email.com","placeholder@email.com","582-110-2224" )
    )

    //get user list
    val users: List<User>
        get() = _users

    //add user
    fun addUser(user: User){
        userRef.document(user.username).set(user)
    }

    //delete user
    fun deleteUser(user: User){
        userRef.document(user.username).delete()
    }
}