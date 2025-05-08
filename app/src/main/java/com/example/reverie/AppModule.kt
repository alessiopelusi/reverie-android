package com.example.reverie

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

// object used to provide dependencies to other classes
// when we call inject we use those methods (based on result type)
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
