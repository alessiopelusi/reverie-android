package com.mirage.reverie.data.repository

import com.mirage.reverie.StorageService
import com.mirage.reverie.data.model.TimeCapsule
import javax.inject.Inject
import javax.inject.Provider

interface TimeCapsuleRepository {
    suspend fun getUserSentTimeCapsules(userId: String): List<TimeCapsule>
    suspend fun getUserReceivedTimeCapsules(userId: String): List<TimeCapsule>
    suspend fun getTimeCapsule(timeCapsuleId: String): TimeCapsule

    suspend fun saveTimeCapsule(timeCapsule: TimeCapsule): TimeCapsule
    suspend fun deleteTimeCapsule(timeCapsuleId: String)
//
//    suspend fun addTimeCapsuleProfileReceiver(userId: String)
//    suspend fun addTimeCapsuleEmailReceiver(email: String)
//    suspend fun addTimeCapsulePhoneReceiver(phone: String)
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
            ?: throw NoSuchElementException("TimeCapsule with ID $timeCapsuleId does not exists")
    }

    override suspend fun saveTimeCapsule(timeCapsule: TimeCapsule): TimeCapsule {
        val savedTimeCapsule = storageService.saveTimeCapsule(timeCapsule)

        // add time capsule to current user
        val user = userRepository.getUser(timeCapsule.userId)

        val sentTimeCapsuleIds = user.sentTimeCapsuleIds.toMutableList()
        sentTimeCapsuleIds.add(savedTimeCapsule.id)
        userRepository.updateUser(user.copy(sentTimeCapsuleIds = sentTimeCapsuleIds))

        if (user.id in savedTimeCapsule.receiversIds){
            val receivedTimeCapsuleIds = user.sentTimeCapsuleIds.toMutableList()
            receivedTimeCapsuleIds.add(savedTimeCapsule.id)
            userRepository.updateUser(user.copy(receivedTimeCapsuleIds = receivedTimeCapsuleIds))
        }

        return getTimeCapsule(savedTimeCapsule.id)
    }

    override suspend fun deleteTimeCapsule(timeCapsuleId: String) {
        val timeCapsule = getTimeCapsule(timeCapsuleId)

        val sendingUser = userRepository.getUser(timeCapsule.userId)
        val updatedSentTimeCapsuleIds = sendingUser.sentTimeCapsuleIds.toMutableList()
        updatedSentTimeCapsuleIds.remove(timeCapsuleId)

        val updatedSendingUser = sendingUser.copy(sentTimeCapsuleIds = updatedSentTimeCapsuleIds)
        userRepository.updateUser(updatedSendingUser)

        timeCapsule.receiversIds.forEach { receiverId ->
            val user = userRepository.getUser(receiverId)
            val updatedReceivedTimeCapsuleIds = user.receivedTimeCapsuleIds.toMutableList()
            updatedReceivedTimeCapsuleIds.remove(timeCapsuleId)

            val updatedUser = user.copy(receivedTimeCapsuleIds = updatedReceivedTimeCapsuleIds)
            userRepository.updateUser(updatedUser)
        }

        storageService.deleteTimeCapsule(timeCapsuleId)
    }
}