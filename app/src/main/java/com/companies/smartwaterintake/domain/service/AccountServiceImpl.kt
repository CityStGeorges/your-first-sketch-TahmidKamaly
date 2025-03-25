package com.stellerbyte.uptodo.services.implementation

import android.util.Log
import com.companies.smartwaterintake.data.UserProfileData
import com.companies.smartwaterintake.domain.service.AccountService
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountServiceImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AccountService {

    override val currentUserId: String
        get() = auth.currentUser?.uid.orEmpty()

    override val hasUser: Boolean
        get() = auth.currentUser != null


    override val currentUser: Flow<UserProfileData>
        get() = callbackFlow {
            val listener = FirebaseAuth.AuthStateListener { auth ->
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    // Fetch username from Firestore (if required)
                    fetchUsernameFromFirestore(currentUser.uid) { username ->
                        val userProfile = UserProfileData(currentUser.uid, username = username!!)
                        trySend(userProfile)
                    }
                } else {
                    // Handle the case when the user is not logged in (no user available)
                    trySend(UserProfileData()) // Default empty user profile
                }
            }
            auth.addAuthStateListener(listener)
            awaitClose { auth.removeAuthStateListener(listener) }
        }

    override fun isAnonymousUser(): Boolean {
        return auth.currentUser?.isAnonymous ?: true
    }

    override fun getUserId(): String {
        return auth.currentUser?.uid.orEmpty()
    }

    override fun authenticate(email: String, password: String, onResult: (Throwable?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(null) // No error, login successful
                } else {
                    onResult(task.exception) // Pass the exception to the ViewModel
                }
            }
    }

    override fun createAccount(
        username: String,
        email: String,
        password: String,
        confirmPassword: String,
        onResult: (Throwable?) -> Unit
    ) {
        val userProfileData = UserProfileData().apply {
            this.email = email
            this.username = username
            this.password = password
            this.confirmPassword = confirmPassword
        }
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.exception is FirebaseAuthUserCollisionException) {
                    onResult(it.exception)
                } else {
                    userProfileData.id = it.result.user?.uid!!
                    Log.d("Account", "createAccount: $userProfileData")
                    saveUsernameToFirestore(userProfileData)
                    onResult(it.exception)
                }
            }
    }

    override fun saveUsernameToFirestore(userProfileData: UserProfileData) {
        val userId = userProfileData.id
        val userRef = firestore.collection("users").document(userId)
        userRef.set(userProfileData)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    // Successfully saved the profile
                } else {
                    // Handle error
                }
            }
    }

    override fun updateUsername(newUsername: String, onResult: (Throwable?) -> Unit) {
        // Assuming you store user profile in Firestore or Realtime Database
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userProfileRef =
                firestore.collection("users").document(userId)
            userProfileRef.update("username", newUsername)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        // Successfully updated the username
                        onResult(null)  // No error
                    } else {
                        onResult(it.exception)  // Pass the exception if there's an error
                    }
                }
        } else {
            onResult(Exception("User not logged in"))
        }
    }

    // Example function to fetch username from Firestore
    override fun fetchUsernameFromFirestore(uid: String, callback: (String?) -> Unit) {
        firestore.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                val username = document.getString("username").toString()
                Log.d("TAG", "fetchUsernameFromFirestore: ${username ?: "hmm"}")

                callback(username)
            }
            .addOnFailureListener {
                callback(null) // In case of failure, return null for username
            }
    }


    override fun linkAccount(email: String, password: String, onResult: (Throwable?) -> Unit) {
        val credential = EmailAuthProvider.getCredential(email, password)
        auth.currentUser!!.linkWithCredential(credential)
            .addOnCompleteListener {
                onResult(it.exception)
            }
    }


    override fun registerAccount(email: String, password: String, onResult: (Throwable?) -> Unit) {
        val credential = EmailAuthProvider.getCredential(email, password)
        auth.currentUser!!.linkWithCredential(credential)
            .addOnCompleteListener { onResult(it.exception) }
    }

    override fun logout(onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        try {
            auth.signOut()
            onSuccess()
        } catch (e: Exception) {
            onError(e)
        }
    }

    override fun deleteAccount(email: String, password: String, onResult: (Throwable?) -> Unit) {
        val currentUser = auth.currentUser
        val credential = EmailAuthProvider.getCredential(email, password)

        currentUser?.reauthenticate(credential)?.addOnCompleteListener {
            if (it.isSuccessful) {
                currentUser.delete().addOnCompleteListener { its ->
                    onResult(its.exception)
                    Log.d("TAG", "deleteAccount: deleted Account")
                }
            }
        }
    }
}

