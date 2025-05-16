package com.mirage.reverie

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// object used to provide dependencies to other classes
// when we call inject we use those methods (based on result type)
@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {

    @Singleton
    @Provides
    fun provideStorageService(firestore: FirebaseFirestore, auth: AccountService): StorageService {
        return StorageServiceImpl(firestore, auth)
    }

    @Singleton
    @Provides
    fun provideAccountService(auth: FirebaseAuth): AccountServiceImpl {
        return AccountServiceImpl(auth)
    }

    @Singleton
    @Provides
    fun provideDiaryRepository(storageService: StorageService): DiaryRepository {
        return DiaryRepositoryImpl(storageService)
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

}
