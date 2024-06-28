package com.bytecause.lenslex.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.bytecause.lenslex.data.local.mlkit.TextRecognizer
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
import com.bytecause.lenslex.data.repository.VerifyOobRepositoryImpl
import com.bytecause.lenslex.data.repository.WordsRepositoryImpl
import com.bytecause.lenslex.ui.screens.viewmodel.AccountSettingsViewModel
import com.bytecause.lenslex.ui.screens.viewmodel.AccountViewModel
import com.bytecause.lenslex.ui.screens.viewmodel.AddViewModel
import com.bytecause.lenslex.ui.screens.viewmodel.HomeViewModel
import com.bytecause.lenslex.ui.screens.viewmodel.LoginViewModel
import com.bytecause.lenslex.ui.screens.viewmodel.ModifiedImagePreviewViewModel
import com.bytecause.lenslex.ui.screens.viewmodel.RecognizedTextViewModel
import com.bytecause.lenslex.ui.screens.viewmodel.SendEmailResetViewModel
import com.bytecause.lenslex.ui.screens.viewmodel.UpdatePasswordViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_preferences"
)

val appModule = module {

    // Firebase
    single { FirebaseFirestore.getInstance() }

    single<FirebaseAuthClient> {
        FirebaseAuthClient()
    }

    single<FirebaseCloudStorage> {
        FirebaseCloudStorage()
    }

    single<TextRecognizer> {
        TextRecognizer(androidContext())
    }

    // Repositories
    single<SupportedLanguagesRepository> {
        SupportedLanguagesRepository()
    }

    single<WordsRepositoryImpl> {
        WordsRepositoryImpl(
            firestore = Firebase.firestore,
            auth = FirebaseAuthClient()
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
        TranslateRepositoryImpl()
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
            supportedLanguagesRepository = get(),
            auth = get<FirebaseAuthClient>()
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
        AccountViewModel(get<FirebaseAuthClient>(), get())
    }

    viewModel {
        ModifiedImagePreviewViewModel(get<TextRecognitionRepositoryImpl>())
    }

    viewModel {
        AddViewModel(
            wordsRepository = get<WordsRepositoryImpl>(),
            translateRepository = get<TranslateRepositoryImpl>(),
            userPrefsRepository = get<UserPrefsRepositoryImpl>(),
            supportedLanguagesRepository = get()
        )
    }

    viewModel {
        AccountSettingsViewModel(get<FirebaseAuthClient>(), get())
    }

    viewModel {
        RecognizedTextViewModel(
            wordsRepository = get<WordsRepositoryImpl>(),
            translateRepository = get<TranslateRepositoryImpl>(),
            languageRecognitionRepository = get<TextLanguageRecognitionRepositoryImpl>(),
            userPrefsRepository = get<UserPrefsRepositoryImpl>(),
            supportedLanguagesRepository = get()
        )
    }
}