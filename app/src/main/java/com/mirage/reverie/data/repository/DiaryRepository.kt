package com.mirage.reverie.data.repository

import android.net.Uri
import com.mirage.reverie.StorageService
import com.mirage.reverie.data.model.Diary
import com.mirage.reverie.data.model.DiaryCover
import com.mirage.reverie.data.model.DiaryImage
import com.mirage.reverie.data.model.DiaryPage
import com.mirage.reverie.data.model.DiarySubPage
import javax.inject.Inject
import javax.inject.Provider

interface DiaryRepository {
    suspend fun getDiary(diaryId: String): Diary
    suspend fun getDiaries(diaryIds: List<String>): List<Diary>
    suspend fun getUserDiaries(userId: String): List<Diary>
    suspend fun saveDiary(diary: Diary): Diary
    suspend fun updateDiary(diary: Diary)
    suspend fun deleteDiary(diary: Diary)

    suspend fun getPage(pageId: String): DiaryPage
    suspend fun getPages(pageIds: List<String>): List<DiaryPage>
    suspend fun savePage(page: DiaryPage): DiaryPage
    suspend fun updatePage(page: DiaryPage)
    suspend fun deletePage(page: DiaryPage)

    suspend fun getSubPage(subPageId: String): DiarySubPage
    suspend fun getSubPages(subPageIds: List<String>): List<DiarySubPage>
    suspend fun saveSubPage(subPage: DiarySubPage): DiarySubPage
    suspend fun updateSubPage(subPage: DiarySubPage)
    suspend fun deleteSubPage(subPage: DiarySubPage)

    suspend fun getDiaryImage(diaryImageId: String): DiaryImage
    suspend fun getAllDiaryImages(diaryId: String): List<DiaryImage>
    suspend fun saveDiaryImage(diaryImage: DiaryImage, imageUri: Uri): DiaryImage
    suspend fun updateDiaryImage(diaryImage: DiaryImage)
    suspend fun deleteDiaryImage(diaryImage: DiaryImage)

    suspend fun getDiaryCover(diaryCoverId: String): DiaryCover
    suspend fun getAllDiaryCovers(): List<DiaryCover>
}


// Using Hilt we inject a dependency (apiSevice)
class DiaryRepositoryImpl @Inject constructor(
    private val storageService: StorageService,
    private val userRepositoryProvider: Provider<UserRepository>
): DiaryRepository {
    private val userRepository
        get() = userRepositoryProvider.get()

    override suspend fun getDiary(diaryId: String): Diary {
        return storageService.getDiary(diaryId)
            ?: throw NoSuchElementException("Diary with ID $diaryId does not exists")
    }

    override suspend fun getDiaries(diaryIds: List<String>): List<Diary> {
        return diaryIds.mapNotNull { diaryId ->
            storageService.getDiary(diaryId)
        }
    }

    override suspend fun getUserDiaries(userId: String): List<Diary> {
        return userRepository.getUser(userId).diaryIds.map { diaryId -> getDiary(diaryId) }
    }

    override suspend fun saveDiary(diary: Diary): Diary {
        val savedDiary = storageService.saveDiary(diary)

        // add first page
        savePage(DiaryPage(diaryId = savedDiary.id))

        // add diary to current user
        val user = userRepository.getUser(diary.uid)
        val diaryIds = user.diaryIds.toMutableList()
        diaryIds.add(savedDiary.id)
        userRepository.updateUser(user.copy(diaryIds = diaryIds))

        return getDiary(savedDiary.id)
    }

    override suspend fun updateDiary(diary: Diary) {
        storageService.updateDiary(diary)
    }

    override suspend fun deleteDiary(diary: Diary) {
        diary.pageIds.forEach { pageId -> deletePage(getPage(pageId)) }
        storageService.deleteDiary(diary)
    }


    override suspend fun getPage(pageId: String): DiaryPage {
        return storageService.getPage(pageId)
            ?: throw NoSuchElementException("Page with ID $pageId does not exists")
    }

    override suspend fun getPages(pageIds: List<String>): List<DiaryPage> {
        return pageIds.mapNotNull { pageId ->
            storageService.getPage(pageId)
        }
    }

    override suspend fun savePage(page: DiaryPage): DiaryPage {
        val savedPage = storageService.savePage(page)

        // add first subPage
        saveSubPage(DiarySubPage(pageId = savedPage.id, diaryId = page.diaryId))

        // add page to diary
        val diary = getDiary(page.diaryId)
        val pageIds = diary.pageIds.toMutableList()
        pageIds.add(savedPage.id)
        updateDiary(diary.copy(pageIds = pageIds))

        return getPage(savedPage.id)
    }

    override suspend fun updatePage(page: DiaryPage) {
        storageService.updatePage(page)
    }

    override suspend fun deletePage(page: DiaryPage) {
        page.subPageIds.forEach { subPageId -> deleteSubPage(getSubPage(subPageId)) }
        storageService.deletePage(page)
    }


    override suspend fun getSubPage(subPageId: String): DiarySubPage {
        return storageService.getSubPage(subPageId)
            ?: throw NoSuchElementException("SubPage with ID $subPageId does not exists")
    }

    override suspend fun getSubPages(subPageIds: List<String>): List<DiarySubPage> {
        return subPageIds.mapNotNull { subPageId ->
            storageService.getSubPage(subPageId)
        }
    }

    override suspend fun saveSubPage(subPage: DiarySubPage): DiarySubPage {
        val savedSubPage = storageService.saveSubPage(subPage)

        val page = getPage(subPage.pageId)
        val subPageIds = page.subPageIds.toMutableList()
        subPageIds.add(savedSubPage.id)
        updatePage(page.copy(subPageIds = subPageIds))

        return savedSubPage
    }

    override suspend fun updateSubPage(subPage: DiarySubPage) {
        storageService.updateSubPage(subPage)
    }

    override suspend fun deleteSubPage(subPage: DiarySubPage) {
        subPage.imageIds.forEach { imageId -> deleteDiaryImage(getDiaryImage(imageId)) }
        storageService.deleteSubPage(subPage)
    }

    override suspend fun getDiaryImage(diaryImageId: String): DiaryImage =
        storageService.getDiaryImage(diaryImageId)
            ?: throw NoSuchElementException("DiaryImage with ID $diaryImageId does not exists")

    override suspend fun getAllDiaryImages(diaryId: String): List<DiaryImage> =
        storageService.getAllDiaryImages(diaryId)

    override suspend fun saveDiaryImage(diaryImage: DiaryImage, imageUri: Uri): DiaryImage {
        val publicUrl = storageService.saveImage(imageUri)

        val diaryImageWithUrl = diaryImage.copy(url = publicUrl)

        val savedDiaryImage = storageService.saveDiaryImage(diaryImageWithUrl)

        val subPage = getSubPage(diaryImageWithUrl.subPageId)
        val imageIds = subPage.imageIds.toMutableList()
        imageIds.add(savedDiaryImage.id)
        updateSubPage(subPage.copy(imageIds = imageIds))

        return diaryImageWithUrl.copy(id = savedDiaryImage.id)
    }

    override suspend fun updateDiaryImage(diaryImage: DiaryImage) {
        storageService.updateDiaryImage(diaryImage)
    }

    override suspend fun deleteDiaryImage(diaryImage: DiaryImage) {
        storageService.deleteDiaryImage(diaryImage)
    }


    override suspend fun getDiaryCover(diaryCoverId: String): DiaryCover =
        storageService.getDiaryCover(diaryCoverId)
            ?: throw NoSuchElementException("DiaryCover with ID $diaryCoverId does not exists")


    override suspend fun getAllDiaryCovers(): List<DiaryCover> =
        storageService.getAllDiaryCovers()
}
