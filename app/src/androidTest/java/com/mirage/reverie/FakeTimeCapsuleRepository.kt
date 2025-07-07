package com.mirage.reverie

import com.google.firebase.Timestamp
import com.mirage.reverie.data.model.TimeCapsule
import com.mirage.reverie.data.repository.TimeCapsuleRepository
import java.util.Calendar
import java.util.UUID

class FakeTimeCapsuleRepository : TimeCapsuleRepository {
    private val timeCapsules = linkedMapOf<String, TimeCapsule>()
    private val userSentCapsules = linkedMapOf<String, MutableList<String>>() // userId -> timeCapsuleIds
    private val userReceivedCapsules = linkedMapOf<String, MutableList<String>>() // userId -> timeCapsuleIds

    init {
        initDefaults()
    }

    private fun initDefaults() {
        val testUserId = "test-user-id"
        val testReceiverId = "test-receiver-id"

        val now = Calendar.getInstance()

        // Scheduled (future)
        val scheduledCapsule = TimeCapsule(
            id = "capsule-scheduled",
            userId = testUserId,
            receiversIds = listOf(testReceiverId),
            title = "Future Capsule",
            content = "To be opened tomorrow.",
            creationDate = Timestamp(now.time),
            deadline = Timestamp(now.apply { add(Calendar.DAY_OF_YEAR, 1) }.time)
        )

        val sentCapsule = TimeCapsule(
            id = "capsule-sent",
            userId = testUserId,
            receiversIds = listOf(testReceiverId),
            title = "Past Capsule",
            content = "Already opened capsule.",
            creationDate = Timestamp(now.time),
            deadline = Timestamp(now.apply { add(Calendar.DAY_OF_YEAR, -2) }.time)
        )

        val receivedCapsule = TimeCapsule(
            id = "capsule-received",
            userId = testReceiverId,
            receiversIds = listOf(testUserId),
            title = "Received Capsule",
            content = "Sent to test user.",
            creationDate = Timestamp(now.time),
            deadline = Timestamp(now.apply { add(Calendar.DAY_OF_YEAR, -3) }.time)
        )

        timeCapsules[scheduledCapsule.id] = scheduledCapsule
        timeCapsules[sentCapsule.id] = sentCapsule
        timeCapsules[receivedCapsule.id] = receivedCapsule

        userSentCapsules.getOrPut(testUserId) { mutableListOf() }.addAll(
            listOf(scheduledCapsule.id, sentCapsule.id)
        )
        userReceivedCapsules.getOrPut(testUserId) { mutableListOf() }.add(receivedCapsule.id)
    }

    override suspend fun getUserSentTimeCapsules(userId: String): List<TimeCapsule> {
        return userSentCapsules[userId]?.mapNotNull { timeCapsules[it] } ?: emptyList()
    }

    override suspend fun getUserReceivedTimeCapsules(userId: String): List<TimeCapsule> {
        return userReceivedCapsules[userId]?.mapNotNull { timeCapsules[it] } ?: emptyList()
    }

    override suspend fun getTimeCapsule(timeCapsuleId: String): TimeCapsule {
        return timeCapsules[timeCapsuleId]
            ?: throw NoSuchElementException("TimeCapsule with ID $timeCapsuleId does not exist")
    }

    override suspend fun saveTimeCapsule(timeCapsule: TimeCapsule): TimeCapsule {
        val id = timeCapsule.id.ifBlank { UUID.randomUUID().toString() }
        val saved = timeCapsule.copy(id = id)
        timeCapsules[id] = saved

        userSentCapsules.getOrPut(saved.userId) { mutableListOf() }.add(id)
        saved.receiversIds.forEach { receiverId ->
            userReceivedCapsules.getOrPut(receiverId) { mutableListOf() }.add(id)
        }

        return saved
    }

    override suspend fun deleteTimeCapsule(timeCapsule: TimeCapsule) {
        timeCapsules.remove(timeCapsule.id)
        userSentCapsules[timeCapsule.userId]?.remove(timeCapsule.id)
        timeCapsule.receiversIds.forEach { receiverId ->
            userReceivedCapsules[receiverId]?.remove(timeCapsule.id)
        }
    }

    // Optional: Utility for injecting test data
    fun addFakeTimeCapsule(capsule: TimeCapsule) {
        timeCapsules[capsule.id] = capsule
        userSentCapsules.getOrPut(capsule.userId) { mutableListOf() }.add(capsule.id)
        capsule.receiversIds.forEach { receiverId ->
            userReceivedCapsules.getOrPut(receiverId) { mutableListOf() }.add(capsule.id)
        }
    }
}
