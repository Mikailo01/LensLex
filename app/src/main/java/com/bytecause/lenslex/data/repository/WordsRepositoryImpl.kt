package com.bytecause.lenslex.data.repository

import com.bytecause.lenslex.data.remote.auth.Authenticator
import com.bytecause.lenslex.data.repository.abstraction.WordsRepository
import com.bytecause.lenslex.domain.models.WordsAndSentences
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class WordsRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val auth: Authenticator,
) : WordsRepository {

    override fun getWords(): Flow<List<WordsAndSentences>> = callbackFlow {
        auth.getAuth().currentUser?.uid?.let { userId ->
            val listener = firestore
                .collection("users")
                .document(userId)
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
                word = field["word"] as String,
                languageCode = field["languageCode"] as String,
                translations = field["translations"] as Map<String, String>,
                timeStamp = field["timeStamp"] as Long
            )
        } ?: WordsAndSentences()
    }

    override fun addWord(word: WordsAndSentences): Flow<Boolean> = callbackFlow {
        auth.getAuth().currentUser?.uid?.let { userId ->
            firestore
                .collection("users")
                .document(userId)
                .collection("WordsAndSentences")
                .document(word.id)
                .set(word)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) trySend(true)
                    else trySend(false)
                }
        }
        awaitClose()
    }

    override fun deleteWord(documentId: String) {
        auth.getAuth().currentUser?.uid?.let { userId ->
            firestore
                .collection("users")
                .document(userId)
                .collection("WordsAndSentences")
                .document(documentId)
                .delete()
        }
    }
}