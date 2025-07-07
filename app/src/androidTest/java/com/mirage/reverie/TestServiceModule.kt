package com.mirage.reverie

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton
import com.mirage.reverie.data.repository.DiaryRepository
import com.mirage.reverie.data.repository.TimeCapsuleRepository
import com.mirage.reverie.data.repository.UserRepository
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

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
    fun provideDiaryRepository(): DiaryRepository {
        return FakeDiaryRepository()
    }

    @Provides
    @Singleton
    fun provideTimeCapsuleRepository(): TimeCapsuleRepository {
        return FakeTimeCapsuleRepository()
    }

    @Provides
    @Singleton
    fun provideUserRepository(): UserRepository {
        return FakeUserRepository()
    }

    @Singleton
    @Provides
    fun provideApplicationContext(
        @ApplicationContext appContext: Context
    ): Context = appContext
}
