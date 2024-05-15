package com.bytecause.lenslex.di

import com.bytecause.lenslex.data.remote.auth.FirebaseAuthClient
import com.bytecause.lenslex.data.repository.FakeAuthenticator
import com.bytecause.lenslex.data.repository.FakeUserPrefsRepository
import com.bytecause.lenslex.data.repository.FakeWordsRepository
import com.bytecause.lenslex.data.repository.SupportedLanguagesRepository
import com.bytecause.lenslex.ui.screens.viewmodel.HomeViewModel
import com.bytecause.lenslex.ui.screens.viewmodel.LoginViewModel
import org.koin.dsl.module

val testModule = module {

    single {
        FirebaseAuthClient()
    }

    single {
        LoginViewModel(get<FirebaseAuthClient>())
    }

    single {
        SupportedLanguagesRepository()
    }

    single {
        HomeViewModel(
            wordsRepository = get<FakeWordsRepository>(),
            userPrefsRepository = get<FakeUserPrefsRepository>(),
            supportedLanguagesRepository = get<SupportedLanguagesRepository>(),
            auth = get<FakeAuthenticator>()
        )
    }

    single<FakeWordsRepository> { FakeWordsRepository() }
    single<FakeUserPrefsRepository> { FakeUserPrefsRepository() }
    single<FakeAuthenticator> { FakeAuthenticator() }
}