package com.mirage.reverie.data.repository

import com.mirage.reverie.StorageService
import com.mirage.reverie.data.model.Diary
import com.mirage.reverie.data.model.TimeCapsule
import javax.inject.Inject
import javax.inject.Provider

interface TimeCapsuleRepository {
    suspend fun getUserSentTimeCapsules(userId: String): List<TimeCapsule>
    suspend fun getUserReceivedTimeCapsules(userId: String): List<TimeCapsule>
    suspend fun getTimeCapsule(timeCapsuleId: String): TimeCapsule

//    suspend fun saveTimeCapsule(timeCapsule: TimeCapsule): TimeCapsule
//    suspend fun deleteTimeCapsule(timeCapsuleId: String)
//
//    suspend fun addTimeCapsuleProfileReceiver(userId: String)
//    suspend fun addTimeCapsuleEmailReceiver(email: String)
//    suspend fun addTimeCapsulePhoneReceiver(phone: String)
//
//    suspend fun addTimeCapsuleVisualizedBy(userId: String)
}

class TimeCapsuleRepositoryImpl @Inject constructor(
    private val storageService: StorageService,
    private val userRepositoryProvider: Provider<UserRepository>
): TimeCapsuleRepository {
    private val userRepository
        get() = userRepositoryProvider.get()

    override suspend fun getUserSentTimeCapsules(userId: String): List<TimeCapsule> {
        return userRepository.getUser(userId).sentTimeCapsuleIds.map { timeCapsuleId -> getTimeCapsule(timeCapsuleId) }
    }

    override suspend fun getUserReceivedTimeCapsules(userId: String): List<TimeCapsule> {
        return userRepository.getUser(userId).receivedTimeCapsuleIds.map { timeCapsuleId -> getTimeCapsule(timeCapsuleId) }
    }

    override suspend fun getTimeCapsule(timeCapsuleId: String): TimeCapsule {
        return storageService.getTimeCapsule(timeCapsuleId)
            ?: throw NoSuchElementException("Diary with ID $timeCapsuleId does not exists")
    }

}