package com.mirage.reverie.data.repository

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.mirage.reverie.StorageService
import com.mirage.reverie.data.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

interface AccountRepository {
    val currentUserId: String
    val hasUser: Boolean
    val currentUser: Flow<User>

    fun createAnonymousAccount(onResult: (Throwable?) -> Unit)
    fun authenticate(email: String, password: String, onResult: (Throwable?) -> Unit)
    suspend fun createAccount(username: String, email: String, password: String, onResult: (Throwable?) -> Unit)
    fun sendPasswordResetEmail(email: String, onResult: (Throwable?) -> Unit)
    fun linkAccount(email: String, password: String, onResult: (Throwable?) -> Unit)
}


@Singleton
class AccountRepositoryImpl @Inject constructor(
    private val storageService: StorageService,
    private val auth: FirebaseAuth
) : AccountRepository {
    override val currentUserId: String
        get() = auth.currentUser?.uid.orEmpty()

    override val hasUser: Boolean
        get() = auth.currentUser != null

    override val currentUser: Flow<User>
        get() = callbackFlow {
            val listener =
                FirebaseAuth.AuthStateListener { auth ->
                    this.trySend(auth.currentUser?.let { User(it.uid) } ?: User())
                }
            auth.addAuthStateListener(listener)
            awaitClose { auth.removeAuthStateListener(listener) }
        }

    override fun createAnonymousAccount(onResult: (Throwable?) -> Unit) {
        auth.signInAnonymously()
            .addOnCompleteListener { onResult(it.exception) }
    }

    override fun authenticate(email: String, password: String, onResult: (Throwable?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { onResult(it.exception) }
    }

    override suspend fun createAccount(username: String, email: String, password: String, onResult: (Throwable?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { onResult(it.exception) }

        storageService.saveUser(
            User(
                email = email,
                username = username
            )
        )
    }

    override fun sendPasswordResetEmail(email: String, onResult: (Throwable?) -> Unit) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { onResult(it.exception) }
    }

    override fun linkAccount(email: String, password: String, onResult: (Throwable?) -> Unit) {
        val credential = EmailAuthProvider.getCredential(email, password)

        auth.currentUser!!.linkWithCredential(credential)
            .addOnCompleteListener { onResult(it.exception) }
    }
}
