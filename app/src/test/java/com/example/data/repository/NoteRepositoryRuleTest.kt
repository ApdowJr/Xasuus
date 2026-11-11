package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.Note
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun createNote_validPayload_createsDocumentAndReturnsId() = runBlocking {
    signInTestUser(ALICE_EMAIL)
    val repository = NoteRepository(firestore, auth)

    val result = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.createNote(
        title = "Alice Note 1",
        content = "Test content for Alice",
        categoryId = "work",
        tags = listOf("work", "test"),
        color = "purple"
      )
    }
    assertTrue("Create note should succeed", result.isSuccess)
    val noteId = result.getOrThrow()
    assertTrue("Note ID should not be empty", noteId.isNotEmpty())
  }

  @Test
  fun observeNotes_authenticatedOwner_emitsMatchingNotes() = runBlocking {
    signInTestUser(ALICE_EMAIL)
    val repository = NoteRepository(firestore, auth)
    val noteId = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.createNote(
        title = "Observable Note",
        content = "Live stream test"
      ).getOrThrow()
    }

    val emittedNotes = withTimeout(FLOW_TIMEOUT_MS) {
      repository.observeNotes().first { list -> list.any { it.id == noteId } }
    }
    assertTrue("Emitted notes must contain created note ID", emittedNotes.any { it.id == noteId })
  }

  @Test
  fun updateNote_authenticatedOwner_updatesSuccessfully() = runBlocking {
    signInTestUser(ALICE_EMAIL)
    val repository = NoteRepository(firestore, auth)
    val noteId = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.createNote(
        title = "Initial Title",
        content = "Initial content"
      ).getOrThrow()
    }

    val updateResult = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.updateNote(
        id = noteId,
        title = "Updated Title",
        content = "Updated content",
        categoryId = "general",
        tags = listOf("updated"),
        color = "blue",
        isPinned = true,
        isFavorite = true,
        isArchived = false
      )
    }
    assertTrue("Update should succeed", updateResult.isSuccess)
  }

  @Test
  fun deleteNote_authenticatedOwner_deletesSuccessfully() = runBlocking {
    signInTestUser(ALICE_EMAIL)
    val repository = NoteRepository(firestore, auth)
    val noteId = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.createNote(
        title = "To be deleted",
        content = "Delete me"
      ).getOrThrow()
    }

    val deleteResult = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.deleteNote(noteId)
    }
    assertTrue("Delete should succeed", deleteResult.isSuccess)
  }

  @Test
  fun togglePin_authenticatedOwner_togglesPinStateSuccessfully() = runBlocking {
    signInTestUser(ALICE_EMAIL)
    val repository = NoteRepository(firestore, auth)
    val noteId = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.createNote(
        title = "Pinnable Note",
        content = "Test content",
        isPinned = false
      ).getOrThrow()
    }
    val note = Note(id = noteId, title = "Pinnable Note", isPinned = false)
    val toggleResult = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.togglePin(note)
    }
    assertTrue("Toggle pin should succeed", toggleResult.isSuccess)
  }

  @Test
  fun observeNotes_unauthenticatedCaller_failsWithPermissionDenied() = runBlocking {
    auth.signOut()
    val repository = NoteRepository(firestore, auth)

    var failed = false
    try {
      withTimeout(FLOW_TIMEOUT_MS) {
        repository.observeNotes().first()
      }
    } catch (e: Exception) {
      val firestoreException = e.unwrapFirestoreException()
      failed = e is IllegalStateException ||
          (firestoreException != null && firestoreException.code == FirebaseFirestoreException.Code.PERMISSION_DENIED)
    }
    assertTrue("Must fail due to unauthenticated state", failed)
  }

  private companion object {
    const val ALICE_EMAIL = "alice@test.com"
    const val DEFAULT_TIMEOUT_MS = 6000L
    const val FLOW_TIMEOUT_MS = 6000L
  }
}
