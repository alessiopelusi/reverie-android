package com.mirage.reverie

import android.content.Context
import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.mirage.reverie.data.model.Diary
import com.mirage.reverie.data.model.DiaryCover
import com.mirage.reverie.data.model.DiaryImage
import com.mirage.reverie.data.model.DiaryPage
import com.mirage.reverie.data.model.DiarySubPage
import com.mirage.reverie.data.model.Email
import com.mirage.reverie.data.model.TimeCapsule
import com.mirage.reverie.data.model.User
import com.mirage.reverie.data.model.Username
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.upload
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton


// example of Database connection
interface StorageService {
    //val diaries: Flow<List<Diary>>

    suspend fun getUser(userId: String): User?
    suspend fun getUserUsername(userId: String): String?
    suspend fun getUserEmail(userId: String): String?
    suspend fun saveUser(user: User): User?
    suspend fun saveUsername(username: Username): Result<Boolean>
    suspend fun saveEmail(email: Email): Result<Boolean>
    suspend fun updateUser(user: User)
    suspend fun deleteUser(userId: String)
    suspend fun isUsernameTaken(username: String): Boolean
    suspend fun isEmailTaken(email: String): Boolean

    suspend fun getDiary(diaryId: String): Diary?
    suspend fun saveDiary(diary: Diary): Diary
    suspend fun updateDiary(diary: Diary)
    suspend fun updateDiaryTitle(diaryId: String, title: String)
    suspend fun deleteDiary(diaryId: String)

    suspend fun getPage(pageId: String): DiaryPage?
    suspend fun savePage(page: DiaryPage): DiaryPage
    suspend fun updatePage(page: DiaryPage)
    suspend fun deletePage(pageId: String)

    suspend fun getSubPage(subPageId: String): DiarySubPage?
    suspend fun saveSubPage(subPage: DiarySubPage): DiarySubPage
    suspend fun updateSubPage(subPage: DiarySubPage)
    suspend fun deleteSubPage(subPageId: String)

    suspend fun getDiaryImage(diaryImageId: String): DiaryImage?
    suspend fun getAllDiaryImages(diaryId: String): List<DiaryImage>
    suspend fun saveDiaryImage(diaryImage: DiaryImage): DiaryImage
    suspend fun updateDiaryImage(diaryImage: DiaryImage)
    suspend fun deleteDiaryImage(diaryImageId: String)

    suspend fun saveImage(imageUri: Uri): String

    suspend fun getDiaryCover(diaryCoverId: String): DiaryCover?
    suspend fun getAllDiaryCovers(): List<DiaryCover>

    suspend fun getTimeCapsule(timeCapsuleId: String): TimeCapsule?
    suspend fun saveTimeCapsule(timeCapsule: TimeCapsule): TimeCapsule
    suspend fun deleteTimeCapsule(timeCapsuleId: String)
}

@Singleton
class StorageServiceImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: Storage, // Supabase
    private val context: Context
) : StorageService {
    val USERS_COLLECTION = "users"
    val DIARIES_COLLECTION = "diaries"
    val PAGES_COLLECTION = "pages"
    val SUB_PAGES_COLLECTION = "subPages"
    val DIARY_IMAGES_COLLECTION = "diaryImages"
    val DIARY_COVERS_COLLECTION = "diaryCovers"
    val USERNAMES_COLLECTION = "usernames"
    val EMAILS_COLLECTION = "emails"
    val USER_ID_FIELD = "uid"

    val DIARY_IMAGE_BUCKET = "diary-images"

    val TIME_CAPSULE_COLLECTION = "timeCapsules"

    /*override val diaries: Flow<List<Diary>>
        get() =
            auth.currentUser.flatMapLatest { user ->
                firestore.collection(DIARY_COLLECTION).whereEqualTo(USER_ID_FIELD, user.id).dataObjects()
            }
     */


    override suspend fun getUser(userId: String): User? =
        firestore.collection(USERS_COLLECTION).document(userId).get().await().toObject<User?>()
            ?.copy(id = userId)
        /*val username = getUserUsername(userId) ?: return null
        val email = getUserEmail(userId) ?: return null

        return firestore.collection(USERS_COLLECTION).document(userId).get().await().toObject<User?>()
                ?.copy(id = userId, username = username, email = email)*/

    override suspend fun getUserUsername(userId: String): String? =
        getUser(userId)?.username
        /*firestore.collection(USERNAMES_COLLECTION).whereEqualTo(USER_ID_FIELD, userId).limit(1)
            .get().await().documents[0].toObject<Username?>()?.username*/

    override suspend fun getUserEmail(userId: String): String? =
        getUser(userId)?.email
        /*firestore.collection(EMAILS_COLLECTION).whereEqualTo(USER_ID_FIELD, userId).limit(1)
            .get().await().documents[0].toObject<Email?>()?.email*/

    // we need a transaction to ensure the correct creation of username and email documents.
    // if one fails, the whole signup needs to fail, without committing anything
    override suspend fun saveUser(user: User): User? {
        return try {
            val userRef = firestore.collection(USERS_COLLECTION).document()

            firestore.runTransaction { transaction ->
                // Update the user object with the generated ID
                val userWithId = user.copy(id = userRef.id)

                // Check if the username exists
                val usernameRef = firestore.collection(USERNAMES_COLLECTION).document(userWithId.username)
                if (transaction.get(usernameRef).exists()) {
                    throw IllegalStateException(context.getString(R.string.username_already_taken))
                }

                // Check if the email exists
                val emailRef = firestore.collection(EMAILS_COLLECTION).document(userWithId.email)
                if (transaction.get(emailRef).exists()) {
                    throw IllegalStateException(context.getString(R.string.email_already_taken))
                }

                // Set the username document
                transaction.set(usernameRef, Username(uid = userRef.id))

                // Set the email document
                transaction.set(emailRef, Email(uid = userRef.id))

                // Add the user document
                transaction.set(userRef, userWithId)
            }.await()

            user.copy(id = userRef.id)
        } catch (e: Exception) {
            e.printStackTrace()
            null // Return null if the transaction fails
        }
    }

    override suspend fun saveUsername(username: Username): Result<Boolean> {
        return try {
            firestore.collection(USERNAMES_COLLECTION).document(username.username).set(Username(uid = username.uid)).await()
            Result.success(true)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun saveEmail(email: Email): Result<Boolean> {
        return try {
            firestore.collection(EMAILS_COLLECTION).document(email.email).set(Username(uid = email.uid)).await()
            Result.success(true)
        } catch (e: Exception) { Result.failure(e) }
    }

    // atomic transaction to update username
    override suspend fun updateUser(user: User) {
        firestore.runTransaction { transaction ->
            val userRef = firestore.collection(USERS_COLLECTION).document(user.id)
            val oldUser = transaction.get(userRef).toObject<User?>()

            if (oldUser != null && user.username != oldUser.username) {
                // Check if the username exists
                val usernameRef = firestore.collection(USERNAMES_COLLECTION).document(user.username)
                if (transaction.get(usernameRef).exists()) {
                    throw IllegalStateException(context.getString(R.string.username_already_taken))
                }

                val oldUsernameRef = firestore.collection(USERNAMES_COLLECTION).document(oldUser.username)
                transaction.delete(oldUsernameRef)

                transaction.set(usernameRef, Username(uid = user.id))
            }

            // Add the user document
            transaction.set(userRef, user)
        }.await()
        firestore.collection(USERS_COLLECTION).document(user.id).set(user).await()
    }

    override suspend fun deleteUser(userId: String) {
        firestore.collection(USERS_COLLECTION).document(userId).delete().await()
    }

    override suspend fun isUsernameTaken(username: String): Boolean =
        firestore.collection(USERNAMES_COLLECTION).document(username).get().await().toObject<Username?>() != null

    override suspend fun isEmailTaken(email: String): Boolean =
        firestore.collection(EMAILS_COLLECTION).document(email).get().await().toObject<Email?>() != null


    override suspend fun getDiary(diaryId: String): Diary? =
        firestore.collection(DIARIES_COLLECTION).document(diaryId).get().await().toObject<Diary?>()
            ?.copy(id = diaryId)

    override suspend fun saveDiary(diary: Diary): Diary {
        val diaryId = firestore.collection(DIARIES_COLLECTION).add(diary).await().id
        return diary.copy(id = diaryId)
    }

    override suspend fun updateDiary(diary: Diary) {
        firestore.collection(DIARIES_COLLECTION).document(diary.id).set(diary).await()
    }

    override suspend fun updateDiaryTitle(diaryId: String, title: String) {
        firestore.collection(DIARIES_COLLECTION).document(diaryId).update("title", title).await()
    }

    override suspend fun deleteDiary(diaryId: String) {
        firestore.collection(DIARIES_COLLECTION).document(diaryId).delete().await()
    }


    override suspend fun getPage(pageId: String): DiaryPage? =
        firestore.collection(PAGES_COLLECTION).document(pageId).get().await().toObject<DiaryPage?>()
            ?.copy(id = pageId)

    override suspend fun savePage(page: DiaryPage): DiaryPage {
        val pageId = firestore.collection(PAGES_COLLECTION).add(page).await().id
        return page.copy(id = pageId)
    }

    override suspend fun updatePage(page: DiaryPage) {
        firestore.collection(PAGES_COLLECTION).document(page.id).set(page).await()
    }

    override suspend fun deletePage(pageId: String) {
        firestore.collection(PAGES_COLLECTION).document(pageId).delete().await()
    }


    override suspend fun getSubPage(subPageId: String): DiarySubPage? =
        firestore.collection(SUB_PAGES_COLLECTION).document(subPageId).get().await()
            .toObject<DiarySubPage?>()?.copy(id = subPageId)

    override suspend fun saveSubPage(subPage: DiarySubPage): DiarySubPage {
        val subPageId = firestore.collection(SUB_PAGES_COLLECTION).add(subPage).await().id
        return subPage.copy(id = subPageId)
    }

    override suspend fun updateSubPage(subPage: DiarySubPage) {
        firestore.collection(SUB_PAGES_COLLECTION).document(subPage.id).set(subPage).await()
    }

    override suspend fun deleteSubPage(subPageId: String) {
        firestore.collection(SUB_PAGES_COLLECTION).document(subPageId).delete().await()
    }


    override suspend fun getDiaryImage(diaryImageId: String): DiaryImage? =
        firestore.collection(DIARY_IMAGES_COLLECTION).document(diaryImageId).get().await()
            .toObject<DiaryImage?>()?.copy(id = diaryImageId)

    override suspend fun getAllDiaryImages(diaryId: String): List<DiaryImage> {
        val subPages = mutableMapOf<String, DiarySubPage>()
        val pages = mutableMapOf<String, DiaryPage>()
        val diary = getDiary(diaryId)

        // sorting images based on position in the diary
        val images = firestore.collectionGroup(DIARY_IMAGES_COLLECTION)
            .whereEqualTo("diaryId", diaryId)
            .get().await()
            .mapNotNull{ image -> image.toObject<DiaryImage?>()?.copy(id = image.id) }
            .onEach { image ->
                val subPage = getSubPage(image.subPageId)
                if (subPage != null) {
                    subPages[image.subPageId] = subPage
                    val page = getPage(subPage.pageId)

                    if (page != null) pages[subPage.pageId] = page
                }
            }
            .sortedWith(
                compareBy (
                    {
                        val pageId = subPages.getValue(it.subPageId).pageId
                        diary?.pageIds?.indexOf(pageId)
                    },
                    {
                        val pageId = subPages.getValue(it.subPageId).pageId
                        pages.getValue(pageId).subPageIds.indexOf(it.subPageId)
                    },
                    {
                        subPages.getValue(it.subPageId).imageIds.indexOf(it.id)
                    }
                )
            )

        return images
    }

    override suspend fun saveDiaryImage(diaryImage: DiaryImage): DiaryImage {
        val diaryImageId = firestore.collection(DIARY_IMAGES_COLLECTION).add(diaryImage).await().id
        return diaryImage.copy(id = diaryImageId)
    }

    override suspend fun updateDiaryImage(diaryImage: DiaryImage) {
        firestore.collection(DIARY_IMAGES_COLLECTION).document(diaryImage.id).set(diaryImage).await()
    }

    override suspend fun deleteDiaryImage(diaryImageId: String) {
        firestore.collection(DIARY_IMAGES_COLLECTION).document(diaryImageId).delete().await()
    }


    override suspend fun saveImage(imageUri: Uri): String {
        val bucket = storage.from(DIARY_IMAGE_BUCKET)

        val fileName = "${UUID.randomUUID()}"
        bucket.upload(fileName, imageUri) {
            upsert = false
        }

        return bucket.publicUrl(fileName)
    }


    override suspend fun getDiaryCover(diaryCoverId: String): DiaryCover? =
        firestore.collection(DIARY_COVERS_COLLECTION).document(diaryCoverId).get().await()
            .toObject<DiaryCover?>()?.copy(id = diaryCoverId)


    override suspend fun getAllDiaryCovers(): List<DiaryCover> =
        firestore.collection(DIARY_COVERS_COLLECTION)
            .get().await().mapNotNull{ diary -> diary.toObject<DiaryCover?>()?.copy(id = diary.id) }

    override suspend fun getTimeCapsule(timeCapsuleId: String): TimeCapsule? =
        firestore.collection(TIME_CAPSULE_COLLECTION).document(timeCapsuleId).get().await().toObject<TimeCapsule?>()
            ?.copy(id = timeCapsuleId)

    override suspend fun saveTimeCapsule(timeCapsule: TimeCapsule): TimeCapsule {
        val timeCapsuleId = firestore.collection(TIME_CAPSULE_COLLECTION).add(timeCapsule).await().id
        return timeCapsule.copy(id = timeCapsuleId)
    }

    override suspend fun deleteTimeCapsule(timeCapsuleId: String) {
        firestore.collection(TIME_CAPSULE_COLLECTION).document(timeCapsuleId).delete().await()
    }
}