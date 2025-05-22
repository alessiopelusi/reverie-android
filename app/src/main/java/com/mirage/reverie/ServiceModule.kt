package com.mirage.reverie

import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.mirage.reverie.data.repository.DiaryRepository
import com.mirage.reverie.data.repository.DiaryRepositoryImpl
import com.mirage.reverie.data.repository.UserRepository
import com.mirage.reverie.data.repository.UserRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// object used to provide dependencies to other classes
// when we call inject we use those methods (based on result type)
@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {

    @Provides
    @Singleton
    fun provideStorageService(firestore: FirebaseFirestore, auth: AccountService): StorageService {
        return StorageServiceImpl(firestore, auth)
    }

    @Provides
    @Singleton
    fun provideAccountService(auth: FirebaseAuth): AccountService {
        return AccountServiceImpl(auth)
    }

    @Provides
    @Singleton
    fun provideDiaryRepository(storageService: StorageService, userRepository: UserRepository): DiaryRepository {
        return DiaryRepositoryImpl(storageService, userRepository)
    }

    @Provides
    @Singleton
    fun provideUserRepository(storageService: StorageService): UserRepository {
        return UserRepositoryImpl(storageService)
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

}
