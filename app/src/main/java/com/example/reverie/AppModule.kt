package com.example.reverie

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    //@Singleton
    fun provideApiService(): ApiService {
        return ApiService.create()
    }

    @Provides
    //@Singleton
    fun provideDiaryRepository(apiService: ApiService): DiaryRepository {
        return DiaryRepository(apiService)
    }
}
