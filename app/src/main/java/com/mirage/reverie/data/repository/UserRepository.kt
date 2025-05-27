package com.mirage.reverie.data.repository

import com.mirage.reverie.StorageService
import com.mirage.reverie.data.model.User
import javax.inject.Inject
import javax.inject.Provider

interface UserRepository {
    suspend fun getUser(userId: String): User
    suspend fun saveUser(user: User): User
    suspend fun updateUser(user: User)
    suspend fun deleteUser(userId: String)
}


// Using Hilt we inject a dependency (apiSevice)
class UserRepositoryImpl @Inject constructor(
    private val storageService: StorageService,
    private val diaryRepositoryProvider: Provider<DiaryRepository>
): UserRepository {
    private val diaryRepository
        get() = diaryRepositoryProvider.get()

    override suspend fun getUser(userId: String): User {
        return storageService.getUser(userId)
            ?: throw NoSuchElementException("User with ID $userId does not exists")
    }

    override suspend fun saveUser(user: User): User {
        return storageService.saveUser(user)
    }

    override suspend fun updateUser(user: User) {
        storageService.updateUser(user)
    }

    override suspend fun deleteUser(userId: String) {
        val user = getUser(userId)
        user.diaryIds.forEach { diaryId -> diaryRepository.deleteDiary(diaryId) }

        storageService.deleteUser(userId)
    }
}
