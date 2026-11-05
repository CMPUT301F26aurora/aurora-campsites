package com.example.auroracampsites

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class UserRepository {
    private val db = Firebase.firestore
    private val userRef = db.collection("users")
    private val _users = mutableStateListOf<User>()
    val users: List<User> get() = _users

    //Add user to firestore
    fun addUser(user: User){
        userRef.document(user.username).set(user)
    }

    //delete user
    fun deleteUser(user: User){
        userRef.document(user.username).delete()
    }
}