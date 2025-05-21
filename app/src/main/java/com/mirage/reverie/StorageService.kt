package com.mirage.reverie

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.mirage.reverie.data.model.Diary
import com.mirage.reverie.data.model.DiaryImage
import com.mirage.reverie.data.model.DiaryPage
import com.mirage.reverie.data.model.DiarySubPage
import com.mirage.reverie.data.model.User
import com.mirage.reverie.data.toFirestoreMap
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton


// example of Database connection
interface StorageService {
    //val diaries: Flow<List<Diary>>

    suspend fun getUser(userId: String): User?
    suspend fun saveUser(user: User): User
    suspend fun updateUser(user: User)
    suspend fun deleteUser(userId: String)

    suspend fun getDiary(diaryId: String): Diary?
    suspend fun saveDiary(diary: Diary): Diary
    suspend fun updateDiary(diary: Diary)
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
    suspend fun saveDiaryImage(diaryImage: DiaryImage): DiaryImage
    suspend fun updateDiaryImage(diaryImage: DiaryImage)
    suspend fun deleteDiaryImage(diaryImageId: String)
}

@Singleton
class StorageServiceImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: AccountService,
) : StorageService {
    val USER_COLLECTION = "users"
    val DIARY_COLLECTION = "diaries"
    val PAGE_COLLECTION = "pages"
    val SUB_PAGE_COLLECTION = "subPages"
    val DIARY_IMAGE_COLLECTION = "diaryImages"

    val USER_ID_FIELD = "userId"

    /*override val diaries: Flow<List<Diary>>
        get() =
            auth.currentUser.flatMapLatest { user ->
                firestore.collection(DIARY_COLLECTION).whereEqualTo(USER_ID_FIELD, user.id).dataObjects()
            }
     */


    override suspend fun getUser(userId: String): User? =
        firestore.collection(USER_COLLECTION).document(userId).get().await().toObject<User?>()
            ?.copy(id = userId)

    override suspend fun saveUser(user: User): User {
        val userId = firestore.collection(USER_COLLECTION).add(user.toFirestoreMap()).await().id
        return user.copy(id = userId)
    }

    override suspend fun updateUser(user: User) {
        firestore.collection(USER_COLLECTION).document(user.id).set(user.toFirestoreMap()).await()
    }

    override suspend fun deleteUser(userId: String) {
        firestore.collection(USER_COLLECTION).document(userId).delete().await()
    }


    override suspend fun getDiary(diaryId: String): Diary? =
        firestore.collection(DIARY_COLLECTION).document(diaryId).get().await().toObject<Diary?>()
            ?.copy(id = diaryId)

    override suspend fun saveDiary(diary: Diary): Diary {
        val diaryId = firestore.collection(DIARY_COLLECTION).add(diary.toFirestoreMap()).await().id
        return diary.copy(id = diaryId)
    }

    override suspend fun updateDiary(diary: Diary) {
        firestore.collection(DIARY_COLLECTION).document(diary.id).set(diary.toFirestoreMap()).await()
    }

    override suspend fun deleteDiary(diaryId: String) {
        firestore.collection(DIARY_COLLECTION).document(diaryId).delete().await()
    }


    override suspend fun getPage(pageId: String): DiaryPage? =
        firestore.collection(PAGE_COLLECTION).document(pageId).get().await().toObject<DiaryPage?>()
            ?.copy(id = pageId)

    override suspend fun savePage(page: DiaryPage): DiaryPage {
        val pageId = firestore.collection(PAGE_COLLECTION).add(page.toFirestoreMap()).await().id
        return page.copy(id = pageId)
    }

    override suspend fun updatePage(page: DiaryPage) {
        firestore.collection(PAGE_COLLECTION).document(page.id).set(page.toFirestoreMap()).await()
    }

    override suspend fun deletePage(pageId: String) {
        firestore.collection(PAGE_COLLECTION).document(pageId).delete().await()
    }


    override suspend fun getSubPage(subPageId: String): DiarySubPage? =
        firestore.collection(SUB_PAGE_COLLECTION).document(subPageId).get().await()
            .toObject<DiarySubPage?>()?.copy(id = subPageId)

    override suspend fun saveSubPage(subPage: DiarySubPage): DiarySubPage {
        val subPageId = firestore.collection(SUB_PAGE_COLLECTION).add(subPage.toFirestoreMap()).await().id
        val page = getPage(subPage.pageId)
        if (page != null) {
            val subPageIds = page.subPageIds.toMutableList()
            subPageIds.add(subPageId)
            updatePage(page.copy(subPageIds = subPageIds))
        }
        return subPage.copy(id = subPageId)
    }

    override suspend fun updateSubPage(subPage: DiarySubPage) {
        firestore.collection(SUB_PAGE_COLLECTION).document(subPage.id).set(subPage.toFirestoreMap()).await()
    }

    override suspend fun deleteSubPage(subPageId: String) {
        firestore.collection(SUB_PAGE_COLLECTION).document(subPageId).delete().await()
    }


    override suspend fun getDiaryImage(diaryImageId: String): DiaryImage? =
        firestore.collection(DIARY_IMAGE_COLLECTION).document(diaryImageId).get().await()
            .toObject<DiaryImage?>()?.copy(id = diaryImageId)

    override suspend fun saveDiaryImage(diaryImage: DiaryImage): DiaryImage {
        val diaryImageId = firestore.collection(DIARY_IMAGE_COLLECTION).add(diaryImage.toFirestoreMap()).await().id
        return diaryImage.copy(id = diaryImageId)
    }

    override suspend fun updateDiaryImage(diaryImage: DiaryImage) {
        firestore.collection(DIARY_IMAGE_COLLECTION).document(diaryImage.id).set(diaryImage.toFirestoreMap()).await()
    }

    override suspend fun deleteDiaryImage(diaryImageId: String) {
        firestore.collection(DIARY_IMAGE_COLLECTION).document(diaryImageId).delete().await()
    }
}