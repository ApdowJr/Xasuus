package com.example.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

/** Small Firestore-friendly checklist row embedded in a note document. */
@IgnoreExtraProperties
data class ChecklistEntry(
    val id: String = "",
    val text: String = "",
    val isChecked: Boolean = false
)
