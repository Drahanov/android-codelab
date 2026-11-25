package com.sap.codelab.domain.model

/**
 * Represents a memo.
 *
 * @property location the place the user wants to be reminded at, or null if no location reminder is set.
 */
internal data class Memo(
    val id: Long = 0,
    val title: String,
    val description: String,
    val reminderDate: Long = 0,
    val location: Coordinates? = null,
    val isDone: Boolean = false,
)
