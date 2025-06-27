package com.mirage.reverie.data.repository

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.mirage.reverie.StorageService
import com.mirage.reverie.data.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Provider

interface UserRepository {
    val currentUserId: String
    val hasUser: Boolean
    val currentUser: Flow<User>

    fun createAnonymousAccount(onResult: (Throwable?) -> Unit)
    suspend fun authenticate(email: String, password: String): Boolean
    suspend fun createAccount(user: User, password: String): User?
    fun sendPasswordResetEmail(email: String, onResult: (Throwable?) -> Unit)
    fun linkAccount(email: String, password: String, onResult: (Throwable?) -> Unit)

    suspend fun getUser(userId: String): User
    suspend fun updateUser(user: User)
    suspend fun deleteUser(userId: String)
    suspend fun isUsernameTaken(username: String): Boolean
    suspend fun isEmailTaken(email: String): Boolean

    suspend fun getUsersMatchingPartialUsername(partialUsername: String): List<User>
}


// Using Hilt we inject a dependency (apiSevice)
class UserRepositoryImpl @Inject constructor(
    private val storageService: StorageService,
    private val diaryRepositoryProvider: Provider<DiaryRepository>,
    private val auth: FirebaseAuth
): UserRepository {
    private val diaryRepository
        get() = diaryRepositoryProvider.get()

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

    override suspend fun authenticate(email: String, password: String): Boolean {
        try {
            auth.signInWithEmailAndPassword(email, password).await()
            return true
        } catch (e: Exception) {
            return false
        }
    }

    override suspend fun createAccount(user: User, password: String): User? {
        val userWithId = storageService.saveUser(user) ?: return null

        try {
            auth.createUserWithEmailAndPassword(user.email, password).await()
            return userWithId
        } catch (e: Exception) {
            return null
        }
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

    override suspend fun getUser(userId: String): User {
        return storageService.getUser(userId)
            ?: throw NoSuchElementException("User with ID $userId does not exists")
    }

    override suspend fun updateUser(user: User) {
        storageService.updateUser(user)
    }

    override suspend fun deleteUser(userId: String) {
        val user = getUser(userId)
        user.diaryIds.forEach { diaryId -> diaryRepository.deleteDiary(diaryId) }

        storageService.deleteUser(userId)
    }

    override suspend fun isUsernameTaken(username: String): Boolean =
        storageService.isUsernameTaken(username)

    override suspend fun isEmailTaken(email: String): Boolean =
        storageService.isEmailTaken(email)

    override suspend fun getUsersMatchingPartialUsername(partialUsername: String): List<User> {
        return storageService.getUsersMatchingPartialUsername(partialUsername)
    }
}
