package com.bytecause.lenslex.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.bytecause.lenslex.auth.FireBaseAuthClient
import com.bytecause.lenslex.data.local.room.AppDatabase
import com.bytecause.lenslex.data.local.room.WordDao
import com.bytecause.lenslex.data.repository.SupportedLanguagesRepository
import com.bytecause.lenslex.data.repository.UserPrefsRepositoryImpl
import com.bytecause.lenslex.data.repository.WordsDatabaseRepository
import com.bytecause.lenslex.ui.screens.viewmodel.AccountSettingsViewModel
import com.bytecause.lenslex.ui.screens.viewmodel.AccountViewModel
import com.bytecause.lenslex.ui.screens.viewmodel.AddViewModel
import com.bytecause.lenslex.ui.screens.viewmodel.HomeViewModel
import com.bytecause.lenslex.ui.screens.viewmodel.LoginViewModel
import com.bytecause.lenslex.ui.screens.viewmodel.TextRecognitionSharedViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_preferences"
)

val appModule = module {

    // Firebase
    single { FirebaseFirestore.getInstance() }

    single {
        Firebase.firestore
    }

    single<FireBaseAuthClient> {
        FireBaseAuthClient()
    }

    // Database
    single {
        Room.databaseBuilder(
            context = get(),
            klass = AppDatabase::class.java,
            name = "app_database"
        )
            .build()
    }

    single<WordDao> { get<AppDatabase>().wordDao() }

    // Repositories
    single<SupportedLanguagesRepository> {
        SupportedLanguagesRepository()
    }

    single { WordsDatabaseRepository(get()) }

    single {
        UserPrefsRepositoryImpl(androidContext().userDataStore)
    }

    // ViewModels
    viewModel {
        HomeViewModel(
            userPrefsRepositoryImpl = get(),
            wordsDatabaseRepository = get(),
            fireStore = get(),
            supportedLanguagesRepository = get(),
            fireBaseAuthClient = get()
        )
    }

    viewModel {
        LoginViewModel(get())
    }

    viewModel {
        TextRecognitionSharedViewModel()
    }

    viewModel {
        AccountViewModel(get())
    }

    viewModel {
        AddViewModel(
            wordsDatabaseRepository = get(),
            firebase = get(),
            fireBaseAuthClient = get(),
            userPrefsRepositoryImpl = get(),
            supportedLanguagesRepository = get()
        )
    }

    viewModel {
        AccountSettingsViewModel(get(), get())
    }
}