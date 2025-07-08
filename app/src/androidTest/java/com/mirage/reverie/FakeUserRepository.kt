package com.mirage.reverie

import com.mirage.reverie.data.model.User
import com.mirage.reverie.data.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.*

class FakeUserRepository : UserRepository {
    private val users = linkedMapOf<String, User>()
    private val usernameIndex = linkedMapOf<String, String>() // username -> userId
    private val emailIndex = linkedMapOf<String, String>() // email -> userId

    private var _currentUser: MutableStateFlow<User?> = MutableStateFlow(null)
    override val currentUser: Flow<User> = MutableStateFlow(User())

    override val currentUserId: String
        get() = _currentUser.value?.id.orEmpty()

    override val hasUser: Boolean
        get() = _currentUser.value != null

    init {
        seedFakeUser()
    }

    private fun seedFakeUser() {
        val user = User(
            id = "test-user-id",
            email = "test@example.com",
            username = "testuser",
            diaryIds = listOf("test-diary-id-1", "test-diary-id-2", "test-diary-id-3"),
            sentTimeCapsuleIds = listOf("capsule-scheduled", "capsule-sent"),
            receivedTimeCapsuleIds = listOf("capsule-received")
        )
        users[user.id] = user
        usernameIndex[user.username] = user.id
        emailIndex[user.email] = user.id
        _currentUser.value = user
    }

    override suspend fun authenticate(email: String, password: String): Boolean {
        val userId = emailIndex[email] ?: return false
        _currentUser.value = users[userId]
        return true
    }

    override suspend fun createAccount(user: User, password: String): User? {
        val id = user.id.ifBlank { UUID.randomUUID().toString() }
        val newUser = user.copy(id = id)
        if (emailIndex.containsKey(newUser.email) || usernameIndex.containsKey(newUser.username)) return null

        users[id] = newUser
        emailIndex[newUser.email] = id
        usernameIndex[newUser.username] = id
        _currentUser.value = newUser
        return newUser
    }

    override fun sendPasswordResetEmail(email: String, onResult: (Throwable?) -> Unit) {
        if (!emailIndex.containsKey(email)) {
            onResult(IllegalArgumentException("Email not found"))
        } else {
            onResult(null)
        }
    }

    override suspend fun getUser(userId: String): User {
        return users[userId] ?: throw NoSuchElementException("User with ID $userId does not exist")
    }

    override suspend fun updateUser(user: User) {
        users[user.id] = user
        user.username.takeIf { it.isNotBlank() }?.let { usernameIndex[it] = user.id }
        user.email.takeIf { it.isNotBlank() }?.let { emailIndex[it] = user.id }
        if (_currentUser.value?.id == user.id) {
            _currentUser.value = user
        }
    }

    override suspend fun deleteUser(user: User) {
        users.remove(user.id)
        user.username.takeIf { it.isNotBlank() }?.let { usernameIndex.remove(it) }
        user.email.takeIf { it.isNotBlank() }?.let { emailIndex.remove(it) }
        if (_currentUser.value?.id == user.id) {
            _currentUser.value = null
        }
    }

    override suspend fun isUsernameTaken(username: String): Boolean {
        return usernameIndex.containsKey(username)
    }

    override suspend fun isEmailTaken(email: String): Boolean {
        return emailIndex.containsKey(email)
    }

    override suspend fun getUsersMatchingPartialUsername(partialUsername: String): List<User> {
        return users.values.filter { it.username.contains(partialUsername, ignoreCase = true) }
    }

    // Utility method to inject fake users in tests
    fun addFakeUser(user: User) {
        users[user.id] = user
        usernameIndex[user.username] = user.id
        emailIndex[user.email] = user.id
    }
}
