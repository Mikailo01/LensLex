package com.bytecause.lenslex.ui.screens.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.bytecause.lenslex.auth.FireBaseAuthClient
import com.bytecause.lenslex.data.local.room.tables.WordAndSentenceEntity
import com.bytecause.lenslex.data.repository.SupportedLanguagesRepository
import com.bytecause.lenslex.data.repository.UserPrefsRepositoryImpl
import com.bytecause.lenslex.data.repository.WordsDatabaseRepository
import com.bytecause.lenslex.models.SupportedLanguage
import com.bytecause.lenslex.models.UserData
import com.bytecause.lenslex.models.WordsAndSentences
import com.bytecause.lenslex.ui.screens.viewmodel.base.BaseViewModel
import com.bytecause.lenslex.util.mutableStateIn
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val userPrefsRepositoryImpl: UserPrefsRepositoryImpl,
    private val wordsDatabaseRepository: WordsDatabaseRepository,
    private val fireStore: FirebaseFirestore,
    supportedLanguagesRepository: SupportedLanguagesRepository,
    private val fireBaseAuthClient: FireBaseAuthClient
) : BaseViewModel(userPrefsRepositoryImpl, supportedLanguagesRepository) {

    private val _deletedItemsStack = MutableStateFlow<List<WordsAndSentences>>(emptyList())
    val deletedItemsStack get() = _deletedItemsStack

    fun getSignedInUser(): UserData? = fireBaseAuthClient.getSignedInUser()?.run {
        UserData(
            userId = uid,
            userName = displayName,
            profilePictureUrl = photoUrl?.toString()
        )
    }

    private lateinit var fireStoreSnapShotListener: ListenerRegistration

    private fun addSnapShotListener() {
        fireStoreSnapShotListener = fireStore
            .collection("users")
            .document(getSignedInUser()!!.userId)
            .collection("WordsAndSentences")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val wordsList = mutableListOf<WordsAndSentences>()
                    for (doc in snapshot.documents) {
                        wordsList.add(mapDocumentObject(doc))
                    }
                    // Update your UI or state with the new data
                    // For example, if using MutableStateFlow:

                    _getAllWordsFromFireStore.value = wordsList.sortedByDescending { it.timeStamp }
                } else {
                    _getAllWordsFromFireStore.value = emptyList()
                }
            }
    }

    private fun mapDocumentObject(document: DocumentSnapshot): WordsAndSentences {
        return document.data?.let { field ->
            WordsAndSentences(
                id = document.id,
                word = field["word"] as String,
                languageCode = field["languageCode"] as String,
                translations = field["translations"] as Map<String, String>,
                timeStamp = field["timeStamp"] as Long
            )
        } ?: WordsAndSentences()
    }

    private val _getAllWordsFromFireStore: MutableStateFlow<List<WordsAndSentences>> =
        callbackFlow {
            Log.d("idk", "ok")
            addSnapShotListener()

            fireStore
                .collection("users")
                .document(getSignedInUser()!!.userId)
                .collection("WordsAndSentences")
                .get()
                .addOnSuccessListener {
                    trySend(
                        it.documents.map { document ->
                            mapDocumentObject(document)
                        }.toMutableList().sortedByDescending { it.timeStamp }
                    )
                }
            awaitClose { close() }
        }.mutableStateIn(
            viewModelScope,
            emptyList()
        )

    val getAllWordsFromFireStore: StateFlow<List<WordsAndSentences>> =
        _getAllWordsFromFireStore.asStateFlow()

    fun insertWordToFireStore(word: WordsAndSentences) {
        viewModelScope.launch(Dispatchers.IO) {
            fireStore
                .collection("users")
                .document(getSignedInUser()!!.userId)
                .collection("WordsAndSentences")
                .document(word.id)
                .set(word)
        }
    }

    fun deleteWordFromFireStore(documentId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            fireStore
                .collection("users")
                .document(getSignedInUser()!!.userId)
                .collection("WordsAndSentences")
                .document(documentId)
                .delete()
        }
    }

    fun addDeletedItemToStack(item: WordsAndSentences) {
        _deletedItemsStack.value = _deletedItemsStack.value + item
    }

    fun removeDeletedItemFromStack() {
        _deletedItemsStack.value = _deletedItemsStack.value
            .toMutableList()
            .apply {
                removeLast()
            }
    }

    fun saveTranslationOption(language: SupportedLanguage) {
        viewModelScope.launch {
            userPrefsRepositoryImpl.saveTranslationOption(language.langCode)
            super.setLangOption(language = language)
        }
    }

    fun insertOrUpdateWordAndSentenceEntity(word: WordAndSentenceEntity) {
        viewModelScope.launch {
            wordsDatabaseRepository.insertOrUpdateWordAndSentenceEntity(word)
        }
    }

    val getAllWords: Flow<List<WordAndSentenceEntity>> =
        wordsDatabaseRepository.getAllWords

    fun deleteWordById(id: Long) {
        viewModelScope.launch {
            wordsDatabaseRepository.deleteWordById(id)
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (::fireStoreSnapShotListener.isInitialized) fireStoreSnapShotListener.remove()
    }
}