package com.mirage.reverie

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.mirage.reverie.data.model.Diary
import com.mirage.reverie.data.model.DiaryCover
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

    suspend fun getDiaryCover(diaryCoverId: String): DiaryCover?
    suspend fun getAllDiaryCovers(): List<DiaryCover>
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
    val DIARY_COVER_COLLECTION = "diaryCovers"
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
        val user = getUser(userId) ?: return
        user.diaryIds.forEach { diaryId -> deleteDiary(diaryId) }

        firestore.collection(USER_COLLECTION).document(userId).delete().await()
    }


    override suspend fun getDiary(diaryId: String): Diary? =
        firestore.collection(DIARY_COLLECTION).document(diaryId).get().await().toObject<Diary?>()
            ?.copy(id = diaryId)

    override suspend fun saveDiary(diary: Diary): Diary {
        // TODO: bad...
        val user = getUser(diary.userId) ?: return Diary()

        val diaryId = firestore.collection(DIARY_COLLECTION).add(diary.toFirestoreMap()).await().id

        val diaryIds = user.diaryIds.toMutableList()
        diaryIds.add(diaryId)
        updateUser(user.copy(diaryIds = diaryIds))

        return diary.copy(id = diaryId)
    }

    override suspend fun updateDiary(diary: Diary) {
        firestore.collection(DIARY_COLLECTION).document(diary.id).set(diary.toFirestoreMap()).await()
    }

    override suspend fun updateDiaryTitle(diaryId: String, title: String) {
        firestore.collection(DIARY_COLLECTION).document(diaryId).update("title", title).await()
    }

    override suspend fun deleteDiary(diaryId: String) {
        val diary = getDiary(diaryId) ?: return
        diary.pageIds.forEach { pageId -> deletePage(pageId) }

        firestore.collection(DIARY_COLLECTION).document(diaryId).delete().await()

        val user = getUser(diary.userId) ?: return
        val diaryIds = user.diaryIds.toMutableList()
        diaryIds.remove(diaryId)
        updateUser(user.copy(diaryIds = diaryIds))
    }


    override suspend fun getPage(pageId: String): DiaryPage? =
        firestore.collection(PAGE_COLLECTION).document(pageId).get().await().toObject<DiaryPage?>()
            ?.copy(id = pageId)

    override suspend fun savePage(page: DiaryPage): DiaryPage {
        // TODO: bad...
        val diary = getDiary(page.diaryId) ?: return DiaryPage()

        val pageId = firestore.collection(PAGE_COLLECTION).add(page.toFirestoreMap()).await().id

        val pageIds = diary.pageIds.toMutableList()
        pageIds.add(pageId)
        updateDiary(diary.copy(pageIds = pageIds))

        return page.copy(id = pageId)
    }

    override suspend fun updatePage(page: DiaryPage) {
        firestore.collection(PAGE_COLLECTION).document(page.id).set(page.toFirestoreMap()).await()
    }

    override suspend fun deletePage(pageId: String) {
        val page = getPage(pageId) ?: return
        page.subPageIds.forEach { subPageId -> deleteSubPage(subPageId) }

        firestore.collection(PAGE_COLLECTION).document(pageId).delete().await()

        val diary = getDiary(page.diaryId) ?: return
        val pageIds = diary.pageIds.toMutableList()
        pageIds.remove(pageId)
        updateDiary(diary.copy(pageIds = pageIds))
    }


    override suspend fun getSubPage(subPageId: String): DiarySubPage? =
        firestore.collection(SUB_PAGE_COLLECTION).document(subPageId).get().await()
            .toObject<DiarySubPage?>()?.copy(id = subPageId)

    override suspend fun saveSubPage(subPage: DiarySubPage): DiarySubPage {
        // TODO: bad...
        val page = getPage(subPage.pageId) ?: return DiarySubPage()

        val subPageId = firestore.collection(SUB_PAGE_COLLECTION).add(subPage.toFirestoreMap()).await().id

        val subPageIds = page.subPageIds.toMutableList()
        subPageIds.add(subPageId)
        updatePage(page.copy(subPageIds = subPageIds))

        return subPage.copy(id = subPageId)
    }

    override suspend fun updateSubPage(subPage: DiarySubPage) {
        firestore.collection(SUB_PAGE_COLLECTION).document(subPage.id).set(subPage.toFirestoreMap()).await()
    }

    override suspend fun deleteSubPage(subPageId: String) {
        val subPage = getSubPage(subPageId) ?: return
        subPage.imageIds.forEach { imageId -> deleteDiaryImage(imageId) }

        firestore.collection(SUB_PAGE_COLLECTION).document(subPageId).delete().await()

        val page = getPage(subPage.pageId) ?: return
        val subPageIds = page.subPageIds.toMutableList()
        subPageIds.remove(subPageId)
        updatePage(page.copy(subPageIds = subPageIds))
    }


    override suspend fun getDiaryImage(diaryImageId: String): DiaryImage? =
        firestore.collection(DIARY_IMAGE_COLLECTION).document(diaryImageId).get().await()
            .toObject<DiaryImage?>()?.copy(id = diaryImageId)

    override suspend fun getAllDiaryImages(diaryId: String): List<DiaryImage> =
        firestore.collectionGroup(DIARY_IMAGE_COLLECTION)
            .whereEqualTo("diaryId", diaryId)
            .get().await()
            .mapNotNull{ image -> image.toObject<DiaryImage?>()?.copy(id = image.id) }

    override suspend fun saveDiaryImage(diaryImage: DiaryImage): DiaryImage {
        // TODO: bad...
        val subPage = getSubPage(diaryImage.subPageId) ?: return DiaryImage()

        val diaryImageId = firestore.collection(DIARY_IMAGE_COLLECTION).add(diaryImage.toFirestoreMap()).await().id

        val imageIds = subPage.imageIds.toMutableList()
        imageIds.add(diaryImageId)
        updateSubPage(subPage.copy(imageIds = imageIds))

        return diaryImage.copy(id = diaryImageId)
    }

    override suspend fun updateDiaryImage(diaryImage: DiaryImage) {
        firestore.collection(DIARY_IMAGE_COLLECTION).document(diaryImage.id).set(diaryImage.toFirestoreMap()).await()
    }

    override suspend fun deleteDiaryImage(diaryImageId: String) {
        val image = getDiaryImage(diaryImageId) ?: return

        firestore.collection(DIARY_IMAGE_COLLECTION).document(diaryImageId).delete().await()

        val subPage = getSubPage(image.subPageId) ?: return
        val imageIds = subPage.imageIds.toMutableList()
        imageIds.remove(diaryImageId)
        updateSubPage(subPage.copy(imageIds = imageIds))
    }


    override suspend fun getDiaryCover(diaryCoverId: String): DiaryCover? =
        firestore.collection(DIARY_COVER_COLLECTION).document(diaryCoverId).get().await()
            .toObject<DiaryCover?>()?.copy(id = diaryCoverId)


    override suspend fun getAllDiaryCovers(): List<DiaryCover> =
        firestore.collection(DIARY_COVER_COLLECTION)
            .get().await().mapNotNull{ diary -> diary.toObject<DiaryCover?>()?.copy(id = diary.id) }
}