package com.pavlovalexey.pleinair

import android.content.Context
import android.content.SharedPreferences
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.pavlovalexey.pleinair.event.data.EventRepository
import com.pavlovalexey.pleinair.map.data.ImageRepository
import com.pavlovalexey.pleinair.profile.data.UserRepository
import com.pavlovalexey.pleinair.settings.data.SettingsRepositoryImpl
import com.pavlovalexey.pleinair.settings.domain.SettingsInteractor
import com.pavlovalexey.pleinair.settings.domain.SettingsInteractorImpl
import com.pavlovalexey.pleinair.settings.domain.SettingsRepository
import com.pavlovalexey.pleinair.utils.AppPreferencesKeys
import com.pavlovalexey.pleinair.utils.firebase.FirebaseUserManager
import com.pavlovalexey.pleinair.utils.firebase.LoginAndUserUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext appContext: Context): SharedPreferences {
        return appContext.getSharedPreferences(AppPreferencesKeys.PREFS_NAME, Context.MODE_PRIVATE)
    }

////////// Firebase
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }
    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }
    @Provides
    @Singleton
    fun provideGoogleSignInClient(@ApplicationContext context: Context): GoogleSignInClient {
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, googleSignInOptions)
    }

    ////////// Repository
    @Provides
    @Singleton
    fun provideSettingsRepository(
        @ApplicationContext context: Context,
        sharedPreferences: SharedPreferences
    ): SettingsRepository {
        return SettingsRepositoryImpl(context, sharedPreferences)
    }
    @Provides
    fun provideEventRepository(
        firebase: FirebaseFirestore
    ): EventRepository {
        return EventRepository(firebase)
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        firebase: FirebaseFirestore,
    ): UserRepository {
        return UserRepository(firebase)
    }

    @Provides
    fun provideImageRepository(): ImageRepository {
        return ImageRepository()
    }

    ////////// Interactor
    @Provides
    @Singleton
    fun provideSettingsInteractor(settingsRepository: SettingsRepository): SettingsInteractor {
        return SettingsInteractorImpl(settingsRepository)
    }


////////// Utils
    @Provides
    @Singleton
    fun provideLoginAndUserUtils(
        @ApplicationContext context: Context,
        firebaseAuth: FirebaseAuth,
        firebaseFirestore: FirebaseFirestore,
        sharedPreferences: SharedPreferences
    ): LoginAndUserUtils {
        return LoginAndUserUtils(context, firebaseAuth, firebaseFirestore, sharedPreferences)
    }
    @Provides
    @Singleton
    fun provideFirebaseUserManager(
        @ApplicationContext appContext: Context,
        firestore: FirebaseFirestore,
        storage: FirebaseStorage,
        loginAndUserUtils: LoginAndUserUtils
    ): FirebaseUserManager {
        return FirebaseUserManager(appContext, firestore, storage, loginAndUserUtils)
    }
}