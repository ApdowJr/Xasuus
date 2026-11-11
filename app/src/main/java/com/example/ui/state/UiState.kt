package com.example.ui.state

sealed interface UiState<out T> {
    object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

enum class NotesTab(val label: String) {
    ALL("Dhammaan"),
    PINNED("La xushay"),
    FAVORITES("Jeceshahay"),
    ARCHIVED("Keydsan")
}
