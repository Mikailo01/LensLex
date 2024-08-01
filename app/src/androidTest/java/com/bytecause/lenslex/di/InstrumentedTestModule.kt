package com.bytecause.lenslex.di

import com.bytecause.lenslex.data.FakeCredentialManagerImpl
import com.bytecause.lenslex.data.FakeUserRepositoryImpl
import com.bytecause.lenslex.data.local.TranslationOptionsDataSource
import com.bytecause.lenslex.data.local.mlkit.TranslationModelManager
import com.bytecause.lenslex.data.remote.auth.abstraction.CredentialManager
import com.bytecause.lenslex.data.remote.auth.FirebaseAuthClient
import com.bytecause.lenslex.data.repository.FakeAuthenticatorImpl
import com.bytecause.lenslex.data.repository.FakeTextRecognitionRepository
import com.bytecause.lenslex.data.repository.FakeTranslateRepositoryImpl
import com.bytecause.lenslex.data.repository.FakeUserPrefsRepositoryImpl
import com.bytecause.lenslex.data.repository.FakeWordsRepositoryImpl
import com.bytecause.lenslex.data.repository.SupportedLanguagesRepository
import com.bytecause.lenslex.ui.screens.viewmodel.AddViewModel
import com.bytecause.lenslex.ui.screens.viewmodel.HomeViewModel
import com.bytecause.lenslex.ui.screens.viewmodel.LoginViewModel
import org.koin.dsl.module

val testModule = module {

    single<FirebaseAuthClient> {
        FirebaseAuthClient()
    }

    single<SupportedLanguagesRepository> {
        SupportedLanguagesRepository()
    }

    single<TranslationModelManager> { TranslationModelManager() }

    single {
        LoginViewModel(get<FirebaseAuthClient>())
    }

    single<TranslationOptionsDataSource> {
        TranslationOptionsDataSource()
    }

    single {
        HomeViewModel(
            wordsRepository = get<FakeWordsRepositoryImpl>(),
            userPrefsRepository = get<FakeUserPrefsRepositoryImpl>(),
            textRecognitionRepository = get<FakeTextRecognitionRepository>(),
            supportedLanguagesRepository = get<SupportedLanguagesRepository>(),
            userRepository = get<FakeUserRepositoryImpl>(),
            translationOptionsDataSource = get<TranslationOptionsDataSource>(),
            translationModelManager = get<TranslationModelManager>()
        )
    }

    single {
        AddViewModel(
            wordsRepository = get<FakeWordsRepositoryImpl>(),
            translateRepository = get<FakeTranslateRepositoryImpl>(),
            userPrefsRepository = get<FakeUserPrefsRepositoryImpl>(),
            supportedLanguagesRepository = get<SupportedLanguagesRepository>(),
            translationOptionsDataSource = get<TranslationOptionsDataSource>(),
            translationModelManager = get<TranslationModelManager>()
        )
    }

    single {
        LoginViewModel(
            auth = get<FakeAuthenticatorImpl>()
        )
    }

    single<CredentialManager> { FakeCredentialManagerImpl() }
    single<FakeUserRepositoryImpl> { FakeUserRepositoryImpl() }
    single<FakeWordsRepositoryImpl> { FakeWordsRepositoryImpl() }
    single<FakeUserPrefsRepositoryImpl> { FakeUserPrefsRepositoryImpl() }
    single<FakeAuthenticatorImpl> { FakeAuthenticatorImpl() }
    single<FakeTranslateRepositoryImpl> { FakeTranslateRepositoryImpl() }
    single<FakeTextRecognitionRepository> { FakeTextRecognitionRepository() }
}