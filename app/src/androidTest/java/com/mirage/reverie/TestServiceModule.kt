package com.mirage.reverie

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton
import com.google.firebase.firestore.FirebaseFirestore
import com.mirage.reverie.data.repository.DiaryRepository
import com.mirage.reverie.data.repository.TimeCapsuleRepository
import com.mirage.reverie.data.repository.TimeCapsuleRepositoryImpl
import com.mirage.reverie.data.repository.UserRepository
import com.mirage.reverie.data.repository.UserRepositoryImpl
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import javax.inject.Provider

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [ServiceModule::class]
)
object TestServiceModule {
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        val testUserId = "test-user-id"

        val mockAuth = mock(FirebaseAuth::class.java)
        val mockUser = mock(FirebaseUser::class.java)

        `when`(mockUser.uid).thenReturn(testUserId)
        `when`(mockAuth.currentUser).thenReturn(mockUser)
        `when`(mockAuth.uid).thenReturn(testUserId)

        return mockAuth
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        val mockFirestore = mock(FirebaseFirestore::class.java)
        // configure mock behavior as needed
        return mockFirestore
    }

    // Provide other dependencies from ServiceModule as well:
    @Provides
    @Singleton
    fun provideStorageService(
        firestore: FirebaseFirestore,
        storage: Storage,
        @ApplicationContext context: Context
    ): StorageService {
        return StorageServiceImpl(firestore, storage, context)
    }

    @Provides
    @Singleton
    fun provideDiaryRepository(): DiaryRepository {
        return FakeDiaryRepository()
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

    @Singleton
    @Provides
    fun provideApplicationContext(
        @ApplicationContext appContext: Context
    ): Context = appContext

    @Singleton
    @Provides
    fun provideStorage(): Storage {
        // You can keep real or mock Storage depending on your needs
        return createSupabaseClient(
            supabaseUrl = "https://wjecfnvsxxnvgheqdnpx.supabase.co",
            supabaseKey = "your_test_key"
        ) {
            install(Storage)
        }.storage
    }
}
