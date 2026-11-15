package com.example.data.model

/**
 * Field limits mirrored by `firestore.rules`.
 *
 * Keep these in sync: a value larger than the matching rule limit makes Firestore reject the
 * whole write with PERMISSION_DENIED, even though the rest of the note is valid.
 */
object NoteLimits {
    const val TITLE = 200
    const val CONTENT = 50_000
    const val TAGS = 10
    const val CHECKLIST = 100
    const val CHECKLIST_TEXT = 500
    const val CATEGORY_NAME = 50
    const val CATEGORY_ID = 64
}
