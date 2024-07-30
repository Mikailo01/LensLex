package com.bytecause.lenslex.data.repository

import com.bytecause.lenslex.data.remote.auth.FirebaseAuthClient
import com.bytecause.lenslex.data.repository.abstraction.WordsRepository
import com.bytecause.lenslex.domain.models.Words
import com.bytecause.lenslex.util.BaseCollection
import com.bytecause.lenslex.util.FIELD_LANGUAGE_CODE
import com.bytecause.lenslex.util.FIELD_TIMESTAMP
import com.bytecause.lenslex.util.FIELD_TRANSLATIONS
import com.bytecause.lenslex.util.FIELD_WORD
import com.bytecause.lenslex.util.WordsCollection
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
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
    ): Flow<List<Words>> = callbackFlow {
        var listener: ListenerRegistration? = null

        user()?.uid?.let { userId ->
            listener = firestore
                .collection(BaseCollection)
                .document(userId)
                .collection(WordsCollection)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        close(e)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && !snapshot.isEmpty) {
                        val wordsList = mutableListOf<Words>()
                        for (doc in snapshot.documents) {
                            val originLang = doc.getString(FIELD_LANGUAGE_CODE)
                            val targetLang = doc.get(FIELD_TRANSLATIONS) as Map<String, String>

                            // filter words in corresponding languages
                            if (originLangCode == originLang && targetLang.containsKey(
                                    targetLangCode
                                )
                            ) {
                                wordsList.add(documentToWords(doc))
                            }
                        }

                        trySend(wordsList.sortedByDescending { it.timeStamp })
                    } else trySend(emptyList())
                }
        }
        awaitClose { listener?.remove() }
    }

    private fun documentToWords(document: DocumentSnapshot): Words {
        return document.data?.let { field ->
            Words(
                id = document.id,
                word = field[FIELD_WORD] as String,
                languageCode = field[FIELD_LANGUAGE_CODE] as String,
                translations = field[FIELD_TRANSLATIONS] as Map<String, String>,
                timeStamp = field[FIELD_TIMESTAMP] as Long
            )
        } ?: Words()
    }

    override fun addWord(word: Words): Flow<Boolean> = callbackFlow {
        var observer: ListenerRegistration? = null

        user()?.uid?.let { userId ->
            val collection = firestore
                .collection(BaseCollection)
                .document(userId)
                .collection(WordsCollection)

            // when device is offline, callback won't be invoked, because new data cannot be sent to
            // server, so we need observer which will listen for local changes
            observer = collection.addSnapshotListener { value, error ->
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

        }
        awaitClose { observer?.remove() }
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