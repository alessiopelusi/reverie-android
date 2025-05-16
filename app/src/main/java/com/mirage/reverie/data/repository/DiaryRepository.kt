package com.mirage.reverie.data.repository

import com.mirage.reverie.StorageService
import com.mirage.reverie.data.model.Diary
import com.mirage.reverie.data.model.DiaryImage
import com.mirage.reverie.data.model.DiaryPage
import com.mirage.reverie.data.model.DiarySubPage
import javax.inject.Inject

interface DiaryRepository {
    suspend fun getDiary(diaryId: String): Diary
    suspend fun saveDiary(diary: Diary): Diary
    suspend fun updateDiary(diary: Diary)
    suspend fun deleteDiary(diaryId: String)

    suspend fun getPage(pageId: String): DiaryPage
    suspend fun getPages(pageIds: List<String>): List<DiaryPage>
    suspend fun savePage(page: DiaryPage): DiaryPage
    suspend fun updatePage(page: DiaryPage)
    suspend fun deletePage(pageId: String)

    suspend fun getSubPage(subPageId: String): DiarySubPage
    suspend fun getSubPages(subPageIds: List<String>): List<DiarySubPage>
    suspend fun saveSubPage(subPage: DiarySubPage): DiarySubPage
    suspend fun updateSubPage(subPage: DiarySubPage)
    suspend fun deleteSubPage(subPageId: String)

    suspend fun getDiaryImage(diaryImageId: String): DiaryImage
    suspend fun saveDiaryImage(diaryImage: DiaryImage): DiaryImage
    suspend fun updateDiaryImage(diaryImage: DiaryImage)
    suspend fun deleteDiaryImage(diaryImageId: String)
}


// Using Hilt we inject a dependency (apiSevice)
class DiaryRepositoryImpl @Inject constructor(
    private val storageService: StorageService
): DiaryRepository {
    override suspend fun getDiary(diaryId: String): Diary {
        return storageService.getDiary(diaryId)
            ?: throw NoSuchElementException("Diary with ID $diaryId does not exists")
    }

    override suspend fun saveDiary(diary: Diary): Diary {
        return storageService.saveDiary(diary)
    }

    override suspend fun updateDiary(diary: Diary) {
        storageService.updateDiary(diary)
    }

    override suspend fun deleteDiary(diaryId: String) {
        storageService.deleteDiary(diaryId)
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
        return storageService.savePage(page)
    }

    override suspend fun updatePage(page: DiaryPage) {
        storageService.updatePage(page)
    }

    override suspend fun deletePage(pageId: String) {
        storageService.deletePage(pageId)
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
        return storageService.saveSubPage(subPage)
    }

    override suspend fun updateSubPage(subPage: DiarySubPage) {
        storageService.updateSubPage(subPage)
    }

    override suspend fun deleteSubPage(subPageId: String) {
        storageService.deleteSubPage(subPageId)
    }

    override suspend fun getDiaryImage(diaryImageId: String): DiaryImage {
        return storageService.getDiaryImage(diaryImageId)
            ?: throw NoSuchElementException("DiaryImage with ID $diaryImageId does not exists")
    }

    override suspend fun saveDiaryImage(diaryImage: DiaryImage): DiaryImage {
        return storageService.saveDiaryImage(diaryImage)
    }

    override suspend fun updateDiaryImage(diaryImage: DiaryImage) {
        storageService.updateDiaryImage(diaryImage)
    }

    override suspend fun deleteDiaryImage(diaryImageId: String) {
        storageService.deleteDiaryImage(diaryImageId)
    }

    /*
    // TODO improve retrieval
    fun getAllProfileDiaries(profileId: String): List<StateFlow<DiaryState>> {
        // We fetch only the missing diaries
        val profileDiaries = storageService.getAllProfileDiaries(
            profileId,
            _diaries.filter{ it.value.value.profileId == profileId }.map{ it.key}
        )
        _diaries.putAll(profileDiaries.associateBy(
            { diary -> diary.id },
            { diary -> MutableStateFlow(diary) })
        )
        return _diaries.filter{ it.value.value.profileId == profileId }.map{ it.value.asStateFlow() }
    }
    }*/
}
