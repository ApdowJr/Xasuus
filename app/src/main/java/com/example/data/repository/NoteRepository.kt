package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.Category
import com.example.data.model.Note
import com.example.data.util.OperationType
import com.example.data.util.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class NoteRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        Firebase.auth
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in before accessing Firestore.")
    }

    fun observeNotes(): Flow<List<Note>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/notes"
        emitAll(
            db.collection("users").document(uid).collection("notes")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(Note::class.java)
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun createNote(
        title: String,
        content: String = "",
        categoryId: String = "general",
        tags: List<String> = emptyList(),
        color: String = "purple",
        isPinned: Boolean = false,
        isFavorite: Boolean = false,
        isArchived: Boolean = false,
        reminderAtMillis: Long? = null
    ): Result<String> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("notes").document()
        val payload = mapOf(
            "userId" to uid,
            "title" to title.trim(),
            "content" to content.trim(),
            "categoryId" to categoryId.ifBlank { "general" },
            "tags" to tags.take(10),
            "isPinned" to isPinned,
            "isFavorite" to isFavorite,
            "isArchived" to isArchived,
            "color" to color.ifBlank { "purple" },
            "reminderAtMillis" to reminderAtMillis,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateNote(
        id: String,
        title: String,
        content: String,
        categoryId: String,
        tags: List<String>,
        color: String,
        isPinned: Boolean,
        isFavorite: Boolean,
        isArchived: Boolean,
        reminderAtMillis: Long? = null
    ): Result<Unit> {
        val uid = requireUserId()
        require(id.isNotBlank()) { "Note ID cannot be blank for update" }
        val docRef = db.collection("users").document(uid).collection("notes").document(id)
        val payload = mapOf(
            "title" to title.trim(),
            "content" to content.trim(),
            "categoryId" to categoryId.ifBlank { "general" },
            "tags" to tags.take(10),
            "isPinned" to isPinned,
            "isFavorite" to isFavorite,
            "isArchived" to isArchived,
            "color" to color.ifBlank { "purple" },
            "reminderAtMillis" to reminderAtMillis,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.update(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun togglePin(note: Note): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("notes").document(note.id)
        return try {
            docRef.update(
                mapOf(
                    "isPinned" to !note.isPinned,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun toggleFavorite(note: Note): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("notes").document(note.id)
        return try {
            docRef.update(
                mapOf(
                    "isFavorite" to !note.isFavorite,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun toggleArchive(note: Note): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("notes").document(note.id)
        return try {
            docRef.update(
                mapOf(
                    "isArchived" to !note.isArchived,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteNote(noteId: String): Result<Unit> {
        val uid = requireUserId()
        require(noteId.isNotBlank()) { "Note ID cannot be blank for delete" }
        val docRef = db.collection("users").document(uid).collection("notes").document(noteId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    fun observeCategories(): Flow<List<Category>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/categories"
        emitAll(
            db.collection("users").document(uid).collection("categories")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(Category::class.java)
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun createCategory(name: String, color: String = "purple", icon: String = "folder"): Result<String> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("categories").document()
        val payload = mapOf(
            "userId" to uid,
            "name" to name.trim(),
            "color" to color.ifBlank { "purple" },
            "icon" to icon.ifBlank { "folder" },
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteCategory(categoryId: String): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("categories").document(categoryId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }
}
