package com.mirage.reverie

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// object used to provide dependencies to other classes
// when we call inject we use those methods (based on result type)
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideApiService(): ApiService {
        return ApiService.create()
    }

    @Singleton
    @Provides
    fun provideDiaryRepository(apiService: ApiService): DiaryRepository {
        return DiaryRepository(apiService)
    }
}
