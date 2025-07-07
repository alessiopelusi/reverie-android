package com.mirage.reverie

import android.net.Uri
import com.google.firebase.Timestamp
import com.mirage.reverie.data.model.*
import com.mirage.reverie.data.repository.DiaryRepository
import java.util.*

class FakeDiaryRepository : DiaryRepository {
    private val diaries = linkedMapOf<String, Diary>()
    private val pages = linkedMapOf<String, DiaryPage>()
    private val subPages = linkedMapOf<String, DiarySubPage>()
    private val images = linkedMapOf<String, DiaryImage>()
    private val covers = linkedMapOf<String, DiaryCover>()

    init {
        initDefaults()
    }

    private fun initDefaults() {
        val userId = "test-user-id"
        val diaryId = "test-diary-id-1"
        val pageId = "test-page-id"
        val subPageId = "test-subpage-id"
        val imageId = "test-image-id"
        val coverId = "test-cover-id"

        val cover = DiaryCover(
            id = coverId,
            name = "Default Cover",
            url = "https://picsum.photos/200/300"
        )

        val diary1 = Diary(
            id = diaryId,
            uid = userId,
            title = "Test Diary",
            description = "Test description",
            creationDate = Timestamp.now(),
            pageIds = listOf(pageId),
            coverId = coverId
        )

        val diary2 = Diary(
            id = "test-diary-id-2",
            uid = userId,
            title = "Second Diary",
            description = "Another diary for testing",
            creationDate = Timestamp.now(),
            pageIds = listOf(),
            coverId = coverId
        )

        val diary3 = Diary(
            id = "test-diary-id-3",
            uid = userId,
            title = "Third Diary",
            description = "Yet another one",
            creationDate = Timestamp.now(),
            pageIds = listOf(),
            coverId = coverId
        )

        val page = DiaryPage(
            id = pageId,
            diaryId = diaryId,
            subPageIds = listOf(subPageId),
            content = "Sample page text",
            timestamp = Timestamp.now()
        )

        val subPage = DiarySubPage(
            id = subPageId,
            pageId = pageId,
            imageIds = listOf(imageId),
        )

        val image = DiaryImage(
            id = imageId,
            subPageId = subPageId,
            diaryId = diaryId,
            url = "https://picsum.photos/200/300"
        )

        diaries[diary1.id] = diary1
        diaries[diary2.id] = diary2
        diaries[diary3.id] = diary3
        pages[pageId] = page
        subPages[subPageId] = subPage
        images[imageId] = image
        covers[coverId] = cover
    }

    override suspend fun getDiary(diaryId: String): Diary =
        diaries[diaryId] ?: throw NoSuchElementException("Diary $diaryId not found")

    override suspend fun getDiaries(diaryIds: List<String>): List<Diary> =
        diaryIds.mapNotNull { diaries[it] }

    override suspend fun getUserDiaries(userId: String): List<Diary> =
        diaries.values.filter { it.uid == userId }

    override suspend fun saveDiary(diary: Diary): Diary {
        val id = diary.id.ifBlank { UUID.randomUUID().toString() }
        val saved = diary.copy(id = id)
        diaries[id] = saved
        return saved
    }

    override suspend fun updateDiary(diary: Diary) {
        diaries[diary.id] = diary
    }

    override suspend fun deleteDiary(diary: Diary) {
        diary.pageIds.forEach { pages.remove(it) }
        diaries.remove(diary.id)
    }

    override suspend fun getPage(pageId: String): DiaryPage =
        pages[pageId] ?: throw NoSuchElementException("Page $pageId not found")

    override suspend fun getPages(pageIds: List<String>): List<DiaryPage> =
        pageIds.mapNotNull { pages[it] }

    override suspend fun savePage(page: DiaryPage): DiaryPage {
        val id = page.id.ifBlank { UUID.randomUUID().toString() }
        val saved = page.copy(id = id)
        pages[id] = saved

        val diary = diaries[page.diaryId] ?: throw NoSuchElementException("Diary not found")
        val updatedPages = diary.pageIds + id
        diaries[page.diaryId] = diary.copy(pageIds = updatedPages)

        return saved
    }

    override suspend fun updatePage(page: DiaryPage) {
        pages[page.id] = page
    }

    override suspend fun deletePage(page: DiaryPage) {
        page.subPageIds.forEach { subPages.remove(it) }
        pages.remove(page.id)
    }

    override suspend fun getSubPage(subPageId: String): DiarySubPage =
        subPages[subPageId] ?: throw NoSuchElementException("SubPage $subPageId not found")

    override suspend fun getSubPages(subPageIds: List<String>): List<DiarySubPage> =
        subPageIds.mapNotNull { subPages[it] }

    override suspend fun saveSubPage(subPage: DiarySubPage): DiarySubPage {
        val id = subPage.id.ifBlank { UUID.randomUUID().toString() }
        val saved = subPage.copy(id = id)
        subPages[id] = saved

        val page = pages[subPage.pageId] ?: throw NoSuchElementException("Page not found")
        val updatedSubPages = page.subPageIds + id
        pages[subPage.pageId] = page.copy(subPageIds = updatedSubPages)

        return saved
    }

    override suspend fun updateSubPage(subPage: DiarySubPage) {
        subPages[subPage.id] = subPage
    }

    override suspend fun deleteSubPage(subPage: DiarySubPage) {
        subPage.imageIds.forEach { images.remove(it) }
        subPages.remove(subPage.id)
    }

    override suspend fun getDiaryImage(diaryImageId: String): DiaryImage =
        images[diaryImageId] ?: throw NoSuchElementException("Image $diaryImageId not found")

    override suspend fun getAllDiaryImages(diaryId: String): List<DiaryImage> =
        images.values.filter { it.diaryId == diaryId }

    override suspend fun saveDiaryImage(diaryImage: DiaryImage, imageUri: Uri): DiaryImage {
        val id = diaryImage.id.ifBlank { UUID.randomUUID().toString() }
        val saved = diaryImage.copy(id = id, url = imageUri.toString())
        images[id] = saved

        val subPage = subPages[diaryImage.subPageId] ?: throw NoSuchElementException("SubPage not found")
        val updatedImages = subPage.imageIds + id
        subPages[diaryImage.subPageId] = subPage.copy(imageIds = updatedImages)

        return saved
    }

    override suspend fun updateDiaryImage(diaryImage: DiaryImage) {
        images[diaryImage.id] = diaryImage
    }

    override suspend fun deleteDiaryImage(diaryImage: DiaryImage) {
        images.remove(diaryImage.id)
    }

    override suspend fun getDiaryCover(diaryCoverId: String): DiaryCover =
        covers[diaryCoverId] ?: throw NoSuchElementException("Cover $diaryCoverId not found")

    override suspend fun getAllDiaryCovers(): List<DiaryCover> = covers.values.toList()

    // Optional: Add utility for prepopulating fake data in tests
    fun addFakeCover(cover: DiaryCover) {
        covers[cover.id] = cover
    }
}

