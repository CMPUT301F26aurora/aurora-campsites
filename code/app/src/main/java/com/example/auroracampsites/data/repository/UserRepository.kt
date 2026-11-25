package com.example.auroracampsites.data.repository

import androidx.compose.runtime.mutableStateListOf
import com.example.auroracampsites.data.models.User
import com.example.auroracampsites.data.models.UserRole
import com.google.firebase.Firebase
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

/**
- A class that handles firebase storage of users. Used to manage users information.
 */
class UserRepository {
    private val db = Firebase.firestore
    private val userRef = db.collection("users")
    private val _users = mutableStateListOf<User>()

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
     * Gets a user from Firebase using their unique ID.
     */
    suspend fun getUserById(userId: String): User? {
        if (userId.isBlank()) return null

        val document = userRef.document(userId).get().await()
        return document.toObject(User::class.java)
            ?.copy(id = document.id)
    }
    /**
     * Gets all user profiles from Firebase.
     */
    suspend fun getAllUsers(): List<User> {
        val snapshot = userRef.get().await()

        return snapshot.documents.mapNotNull { document ->
            document.toObject(User::class.java)
                ?.copy(id = document.id)
        }
    }
    /**
     * Loads an existing user or creates a new user profile.
     */
    suspend fun getOrCreateUser(userId: String): User {
        require(userId.isNotBlank())

        val documentRef = userRef.document(userId)

        return db.runTransaction { transaction ->
            val document = transaction.get(documentRef)

            if (document.exists()) {
                requireNotNull(document.toObject(User::class.java))
                    .copy(id = document.id)
            } else {
                val newUser = User(
                    id = userId,
                    role = UserRole.USER
                )

                transaction.set(documentRef, newUser)
                newUser
            }
        }.await()
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
    /** Updates an existing profile without changing its role. */
    suspend fun updateProfileField(userId: String, field: String, value: Any): User {
        require(userId.isNotBlank())
        require(field in setOf("username", "email", "phoneNumber", "notificationsEnabled"))
        require(if (field == "notificationsEnabled") value is Boolean else value is String)
        val ref = userRef.document(userId)
        return db.runTransaction { transaction ->
            val snapshot = transaction.get(ref)
            val user = requireNotNull(snapshot.toObject(User::class.java)).copy(id = snapshot.id)
            val updated = when (field) {
                "username" -> user.copy(username = value as String)
                "email" -> user.copy(email = value as String)
                "phoneNumber" -> user.copy(phoneNumber = value as String)
                else -> user.copy(notificationsEnabled = value as Boolean)
            }
            transaction.update(ref, field, value)
            updated
        }.await()
    }

    /** Deletes only the profile document; related records are retained. */
    suspend fun deleteProfile(userId: String) {
        require(userId.isNotBlank())
        userRef.document(userId).delete().await()
    }
}

