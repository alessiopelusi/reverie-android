package com.mirage.reverie

import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.mirage.reverie.data.repository.DiaryRepository
import com.mirage.reverie.data.repository.DiaryRepositoryImpl
import com.mirage.reverie.data.repository.TimeCapsuleRepository
import com.mirage.reverie.data.repository.TimeCapsuleRepositoryImpl
import com.mirage.reverie.data.repository.UserRepository
import com.mirage.reverie.data.repository.UserRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import javax.inject.Provider
import javax.inject.Singleton

// object used to provide dependencies to other classes
// when we call inject we use those methods (based on result type)
@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {
    // TODO: unsafe to push on github?
    private val supabase = createSupabaseClient(
        supabaseUrl = "https://wjecfnvsxxnvgheqdnpx.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6IndqZWNmbnZzeHhudmdoZXFkbnB4Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDc5MDg0MzIsImV4cCI6MjA2MzQ4NDQzMn0.LrI65dbt0L0WkM6uEFzoKpvzNt8Xy6_QI8LbWj9LVxE"
    ) {
        install(Storage)
    }

    @Provides
    @Singleton
    fun provideStorageService(firestore: FirebaseFirestore, storage: Storage): StorageService {
        return StorageServiceImpl(firestore, storage)
    }

    // We use Provider to break circular dependency
    @Provides
    @Singleton
    fun provideDiaryRepository(
        storageService: StorageService,
        userRepository: Provider<UserRepository>
    ): DiaryRepository {
        return DiaryRepositoryImpl(storageService, userRepository)
    }

    @Provides
    @Singleton
    fun provideTimeCapsuleRepository(
        storageService: StorageService,
        userRepository: Provider<UserRepository>
    ): TimeCapsuleRepository {
        return TimeCapsuleRepositoryImpl(storageService, userRepository)
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        storageService: StorageService,
        diaryRepository: Provider<DiaryRepository>,
        auth: FirebaseAuth
    ): UserRepository {
        return UserRepositoryImpl(storageService, diaryRepository, auth)
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return Firebase.auth
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return Firebase.firestore
    }

    @Singleton
    @Provides
    fun provideApplicationContext(
        @ApplicationContext appContext: Context
    ): Context = appContext

    @Singleton
    @Provides
    fun provideStorage(): Storage {
        return supabase.storage
    }
}
