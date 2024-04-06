package com.bytecause.lenslex.ui.screens.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.bytecause.lenslex.auth.FireBaseAuthClient
import com.bytecause.lenslex.data.local.room.tables.WordAndSentenceEntity
import com.bytecause.lenslex.data.repository.SupportedLanguagesRepository
import com.bytecause.lenslex.data.repository.UserPrefsRepositoryImpl
import com.bytecause.lenslex.data.repository.WordsDatabaseRepository
import com.bytecause.lenslex.models.WordsAndSentences
import com.bytecause.lenslex.ui.screens.viewmodel.base.BaseViewModel
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AddViewModel @Inject constructor(
    private val wordsDatabaseRepository: WordsDatabaseRepository,
    private val firebase: FirebaseFirestore,
    private val authClient: FireBaseAuthClient,
    userPrefsRepositoryImpl: UserPrefsRepositoryImpl,
    supportedLanguagesRepository: SupportedLanguagesRepository
) : BaseViewModel(userPrefsRepositoryImpl, supportedLanguagesRepository) {

    /*fun insertWord(word: WordsAndSentences, onSuccess: () -> Unit) {
        viewModelScope.launch {
            firebase
                .collection("WordsAndSentences")
                .add(word)
                .addOnSuccessListener {
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Error adding document", e)
                }
        }
    }*/

    fun insertWord(word: WordsAndSentences, onSuccess: () -> Unit) {
        authClient.getSignedInUser()?.uid?.let { userId ->
            viewModelScope.launch {
                firebase
                    .collection("users")
                    .document(userId)
                    .collection("WordsAndSentences")
                    .add(word)
                    .addOnSuccessListener {
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        Log.w("Firestore", "Error adding document", e)
                    }
            }
        }
    }

    fun insertOrUpdateWordAndSentenceEntity(entity: WordAndSentenceEntity) {
        viewModelScope.launch {
            // Remove unnecessary whitespaces.
            val regex = Regex("\\s+")
            wordsDatabaseRepository.insertOrUpdateWordAndSentenceEntity(
                entity.copy(
                    word = entity.word.replace(regex, " ").lowercase()
                )
            )
        }
    }
}