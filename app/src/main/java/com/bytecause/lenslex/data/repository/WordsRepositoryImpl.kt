package com.bytecause.lenslex.data.repository

import com.bytecause.lenslex.data.remote.auth.FirebaseAuthClient
import com.bytecause.lenslex.data.repository.abstraction.WordsRepository
import com.bytecause.lenslex.domain.models.WordsAndSentences
import com.bytecause.lenslex.util.BaseCollection
import com.bytecause.lenslex.util.FIELD_LANGUAGE_CODE
import com.bytecause.lenslex.util.FIELD_TIMESTAMP
import com.bytecause.lenslex.util.FIELD_TRANSLATIONS
import com.bytecause.lenslex.util.FIELD_WORD
import com.bytecause.lenslex.util.WordsCollection
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class WordsRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuthClient,
) : WordsRepository {

    private fun user(): FirebaseUser? = auth.getAuth().currentUser

    override fun getWords(
        originLangCode: String,
        targetLangCode: String
    ): Flow<List<WordsAndSentences>> = callbackFlow {
        user()?.uid?.let { userId ->
            val listener = firestore
                .collection(BaseCollection)
                .document(userId)
                .collection(WordsCollection)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        close(e)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && !snapshot.isEmpty) {
                        val wordsList = mutableListOf<WordsAndSentences>()
                        for (doc in snapshot.documents) {
                            val originLang = doc.getString(FIELD_LANGUAGE_CODE)
                            val targetLang = doc.get(FIELD_TRANSLATIONS) as Map<String, String>

                            // filter words in corresponding languages
                            if (originLangCode == originLang && targetLang.containsKey(
                                    targetLangCode
                                )
                            ) {
                                wordsList.add(mapDocumentObject(doc))
                            }
                        }

                        trySend(wordsList.sortedByDescending { it.timeStamp })
                    } else trySend(emptyList())
                }
            awaitClose { listener.remove() }
        }
    }

    private fun mapDocumentObject(document: DocumentSnapshot): WordsAndSentences {
        return document.data?.let { field ->
            WordsAndSentences(
                id = document.id,
                word = field[FIELD_WORD] as String,
                languageCode = field[FIELD_LANGUAGE_CODE] as String,
                translations = field[FIELD_TRANSLATIONS] as Map<String, String>,
                timeStamp = field[FIELD_TIMESTAMP] as Long
            )
        } ?: WordsAndSentences()
    }

    override fun addWord(word: WordsAndSentences): Flow<Boolean> = callbackFlow {
        user()?.uid?.let { userId ->
            val collection = firestore
                .collection(BaseCollection)
                .document(userId)
                .collection(WordsCollection)

            // when device is offline, callback won't be invoked, because new data cannot be sent to
            // server, so we need observer which will listen for local changes
            val observer = collection.addSnapshotListener { value, error ->
                when {
                    error != null -> trySend(false)
                    value != null && value.metadata.hasPendingWrites() -> trySend(true)
                }
            }

            collection
                .add(word).addOnCompleteListener { task ->
                    if (task.isSuccessful) trySend(true)
                    else trySend(false)
                }

            awaitClose { observer.remove() }
        }
    }

    override fun deleteWord(documentId: String) {
        user()?.uid?.let { userId ->
            firestore
                .collection(BaseCollection)
                .document(userId)
                .collection(WordsCollection)
                .document(documentId)
                .delete()
        }
    }
}