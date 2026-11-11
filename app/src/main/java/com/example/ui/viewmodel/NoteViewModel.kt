package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.Category
import com.example.data.model.Note
import com.example.data.repository.NoteRepository
import com.example.ui.state.NotesTab
import com.example.ui.state.UiState
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NoteViewModel(
    private val repository: NoteRepository
) : ViewModel() {

    val notesState: StateFlow<UiState<List<Note>>> = repository.observeNotes()
        .map<List<Note>, UiState<List<Note>>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            emit(UiState.Error(rootError.message ?: "Khalad baa dhacay markii xasuusaha la soo qaadayay"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    val categoriesState: StateFlow<UiState<List<Category>>> = repository.observeCategories()
        .map<List<Category>, UiState<List<Category>>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            emit(UiState.Error(rootError.message ?: "Khalad baa dhacay markii qeybaha la soo qaadayay"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    val selectedCategoryId: StateFlow<String?> = _selectedCategoryId.asStateFlow()

    private val _activeTab = MutableStateFlow(NotesTab.ALL)
    val activeTab: StateFlow<NotesTab> = _activeTab.asStateFlow()

    private val _operationMessage = MutableStateFlow<String?>(null)
    val operationMessage: StateFlow<String?> = _operationMessage.asStateFlow()

    val filteredNotes: StateFlow<List<Note>> = combine(
        notesState,
        searchQuery,
        selectedCategoryId,
        activeTab
    ) { state, query, categoryId, tab ->
        if (state !is UiState.Success) return@combine emptyList()
        val allNotes = state.data
        val trimmedQuery = query.trim().lowercase()

        allNotes.filter { note ->
            val matchesTab = when (tab) {
                NotesTab.ALL -> !note.isArchived
                NotesTab.PINNED -> note.isPinned && !note.isArchived
                NotesTab.FAVORITES -> note.isFavorite && !note.isArchived
                NotesTab.ARCHIVED -> note.isArchived
            }
            val matchesCategory = categoryId == null || note.categoryId.equals(categoryId, ignoreCase = true)
            val matchesQuery = trimmedQuery.isEmpty() ||
                note.title.lowercase().contains(trimmedQuery) ||
                note.content.lowercase().contains(trimmedQuery) ||
                note.tags.any { it.lowercase().contains(trimmedQuery) }

            matchesTab && matchesCategory && matchesQuery
        }.sortedWith(
            compareByDescending<Note> { it.isPinned }
                .thenByDescending { it.updatedAt ?: it.createdAt }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = emptyList()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(categoryId: String?) {
        _selectedCategoryId.value = categoryId
    }

    fun setActiveTab(tab: NotesTab) {
        _activeTab.value = tab
    }

    fun clearOperationMessage() {
        _operationMessage.value = null
    }

    fun createNote(
        title: String,
        content: String,
        categoryId: String,
        tags: List<String>,
        color: String,
        isPinned: Boolean,
        isFavorite: Boolean,
        reminderAtMillis: Long? = null,
        onSuccess: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = repository.createNote(
                title = title,
                content = content,
                categoryId = categoryId,
                tags = tags,
                color = color,
                isPinned = isPinned,
                isFavorite = isFavorite,
                reminderAtMillis = reminderAtMillis
            )
            result.onSuccess { id ->
                _operationMessage.value = "Xasuusta si guul leh ayaa loo kaydiyay!"
                onSuccess(id)
            }.onFailure { e ->
                _operationMessage.value = "Khalad: ${e.localizedMessage}"
            }
        }
    }

    fun updateNote(
        id: String,
        title: String,
        content: String,
        categoryId: String,
        tags: List<String>,
        color: String,
        isPinned: Boolean,
        isFavorite: Boolean,
        isArchived: Boolean,
        reminderAtMillis: Long? = null,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = repository.updateNote(
                id = id,
                title = title,
                content = content,
                categoryId = categoryId,
                tags = tags,
                color = color,
                isPinned = isPinned,
                isFavorite = isFavorite,
                isArchived = isArchived,
                reminderAtMillis = reminderAtMillis
            )
            result.onSuccess {
                _operationMessage.value = "Xasuusta waa la cusbooneysiiyay!"
                onSuccess()
            }.onFailure { e ->
                _operationMessage.value = "Khalad: ${e.localizedMessage}"
            }
        }
    }

    fun togglePin(note: Note) {
        viewModelScope.launch {
            repository.togglePin(note)
        }
    }

    fun toggleFavorite(note: Note) {
        viewModelScope.launch {
            repository.toggleFavorite(note)
        }
    }

    fun toggleArchive(note: Note) {
        viewModelScope.launch {
            repository.toggleArchive(note)
        }
    }

    fun deleteNote(noteId: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val result = repository.deleteNote(noteId)
            result.onSuccess {
                _operationMessage.value = "Xasuusta waa la tirtiray!"
                onSuccess()
            }.onFailure { e ->
                _operationMessage.value = "Khalad: ${e.localizedMessage}"
            }
        }
    }

    fun createCategory(name: String, color: String, icon: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val result = repository.createCategory(name, color, icon)
            result.onSuccess {
                _operationMessage.value = "Qeyb cusub ayaa lagu daray!"
                onSuccess()
            }.onFailure { e ->
                _operationMessage.value = "Khalad: ${e.localizedMessage}"
            }
        }
    }

    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            repository.deleteCategory(categoryId)
        }
    }

    class Factory(private val repository: NoteRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NoteViewModel(repository) as T
        }
    }
}
