package com.bytecause.lenslex.di

import com.bytecause.lenslex.data.local.datastore.userDataStore
import com.bytecause.lenslex.data.local.mlkit.TextRecognizer
import com.bytecause.lenslex.data.local.mlkit.TranslationModelManager
import com.bytecause.lenslex.data.local.mlkit.Translator
import com.bytecause.lenslex.data.remote.FirebaseCloudStorage
import com.bytecause.lenslex.data.remote.auth.FirebaseAuthClient
import com.bytecause.lenslex.data.remote.retrofit.VerifyOobCodeRestApiBuilder
import com.bytecause.lenslex.data.remote.retrofit.VerifyOobCodeRestApiService
import com.bytecause.lenslex.data.repository.FirebaseCloudRepositoryImpl
import com.bytecause.lenslex.data.repository.SupportedLanguagesRepository
import com.bytecause.lenslex.data.repository.TextLanguageRecognitionRepositoryImpl
import com.bytecause.lenslex.data.repository.TextRecognitionRepositoryImpl
import com.bytecause.lenslex.data.repository.TranslateRepositoryImpl
import com.bytecause.lenslex.data.repository.UserPrefsRepositoryImpl
import com.bytecause.lenslex.data.repository.UserRepositoryImpl
import com.bytecause.lenslex.data.repository.VerifyOobRepositoryImpl
import com.bytecause.lenslex.data.repository.WordsRepositoryImpl
import com.bytecause.lenslex.ui.screens.viewmodel.AccountSettingsViewModel
import com.bytecause.lenslex.ui.screens.viewmodel.AccountViewModel
import com.bytecause.lenslex.ui.screens.viewmodel.AddViewModel
import com.bytecause.lenslex.ui.screens.viewmodel.HomeViewModel
import com.bytecause.lenslex.ui.screens.viewmodel.LoginViewModel
import com.bytecause.lenslex.ui.screens.viewmodel.ModifiedImagePreviewViewModel
import com.bytecause.lenslex.ui.screens.viewmodel.ExtractedTextViewModel
import com.bytecause.lenslex.ui.screens.viewmodel.SendEmailResetViewModel
import com.bytecause.lenslex.ui.screens.viewmodel.UpdatePasswordViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED
import com.google.firebase.firestore.PersistentCacheSettings
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    // Firebase
    single<FirebaseFirestore> {
        FirebaseFirestore.getInstance().apply {
            val cacheSettings = PersistentCacheSettings.newBuilder()
                .setSizeBytes(CACHE_SIZE_UNLIMITED)
                .build()

            val settings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(cacheSettings)
                .build()

            firestoreSettings = settings
        }
    }

    single<FirebaseAuthClient> {
        FirebaseAuthClient()
    }

    single<FirebaseCloudStorage> {
        FirebaseCloudStorage()
    }

    single<TranslationModelManager> {
        TranslationModelManager()
    }

    single<Translator> {
        Translator()
    }

    single<TextRecognizer> {
        TextRecognizer(androidContext())
    }

    // Repositories
    single<SupportedLanguagesRepository> {
        SupportedLanguagesRepository()
    }

    single<UserRepositoryImpl> {
        UserRepositoryImpl(
            auth = get<FirebaseAuthClient>(),
            firestore = get<FirebaseFirestore>()
        )
    }

    single<WordsRepositoryImpl> {
        WordsRepositoryImpl(
            firestore = get<FirebaseFirestore>(),
            auth = get<FirebaseAuthClient>()
        )
    }

    single<UserPrefsRepositoryImpl> {
        UserPrefsRepositoryImpl(androidContext().userDataStore, Dispatchers.IO)
    }

    single<VerifyOobCodeRestApiService> {
        VerifyOobCodeRestApiBuilder().getVerifyOobCodeRestApiService()
    }

    single<VerifyOobRepositoryImpl> {
        VerifyOobRepositoryImpl(get(), Dispatchers.IO)
    }

    single<FirebaseCloudRepositoryImpl> {
        FirebaseCloudRepositoryImpl(get(), Dispatchers.IO)
    }

    single<TranslateRepositoryImpl> {
        TranslateRepositoryImpl(get())
    }

    single<TextRecognitionRepositoryImpl> {
        TextRecognitionRepositoryImpl(get<TextRecognizer>())
    }

    single<TextLanguageRecognitionRepositoryImpl> {
        TextLanguageRecognitionRepositoryImpl()
    }

    // ViewModels
    viewModel {
        HomeViewModel(
            wordsRepository = get<WordsRepositoryImpl>(),
            userPrefsRepository = get<UserPrefsRepositoryImpl>(),
            textRecognitionRepository = get<TextRecognitionRepositoryImpl>(),
            userRepository = get<UserRepositoryImpl>(),
            supportedLanguagesRepository = get(),
            translationModelManager = get<TranslationModelManager>(),
        )
    }

    viewModel {
        UpdatePasswordViewModel(get<FirebaseAuthClient>(), get<VerifyOobRepositoryImpl>())
    }

    viewModel {
        LoginViewModel(get<FirebaseAuthClient>())
    }

    viewModel {
        SendEmailResetViewModel(get<FirebaseAuthClient>())
    }

    viewModel {
        AccountViewModel(
            auth = get<FirebaseAuthClient>(),
            firebaseCloudRepository = get<FirebaseCloudRepositoryImpl>(),
            userRepository = get<UserRepositoryImpl>()
        )
    }

    viewModel {
        ModifiedImagePreviewViewModel(get<TextRecognitionRepositoryImpl>())
    }

    viewModel {
        AddViewModel(
            wordsRepository = get<WordsRepositoryImpl>(),
            translateRepository = get<TranslateRepositoryImpl>(),
            userPrefsRepository = get<UserPrefsRepositoryImpl>(),
            translationModelManager = get<TranslationModelManager>(),
            supportedLanguagesRepository = get()
        )
    }

    viewModel {
        AccountSettingsViewModel(
            auth = get<FirebaseAuthClient>(),
            userRepository = get<UserRepositoryImpl>()
        )
    }

    viewModel {
        ExtractedTextViewModel(
            wordsRepository = get<WordsRepositoryImpl>(),
            translateRepository = get<TranslateRepositoryImpl>(),
            languageRecognitionRepository = get<TextLanguageRecognitionRepositoryImpl>(),
            userPrefsRepository = get<UserPrefsRepositoryImpl>(),
            translationModelManager = get<TranslationModelManager>(),
            supportedLanguagesRepository = get()
        )
    }
}