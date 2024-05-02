package com.bytecause.lenslex.ui.screens.viewmodel

import androidx.lifecycle.viewModelScope
import com.bytecause.lenslex.data.local.room.tables.WordAndSentenceEntity
import com.bytecause.lenslex.data.repository.AuthRepository
import com.bytecause.lenslex.data.repository.SupportedLanguagesRepository
import com.bytecause.lenslex.data.repository.UserPrefsRepositoryImpl
import com.bytecause.lenslex.data.repository.WordsDatabaseRepository
import com.bytecause.lenslex.models.WordsAndSentences
import com.bytecause.lenslex.ui.screens.viewmodel.base.BaseViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch


class AddViewModel(
    private val wordsDatabaseRepository: WordsDatabaseRepository,
    private val firebase: FirebaseFirestore,
    private val auth: AuthRepository,
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
        auth.getFirebaseAuth.currentUser?.uid?.let { userId ->
            viewModelScope.launch {
                firebase
                    .collection("users")
                    .document(userId)
                    .collection("WordsAndSentences")
                    .add(word)
            }.invokeOnCompletion { onSuccess() }
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