package com.sap.codelab.domain.notification

import com.sap.codelab.domain.model.Memo

/**
 * Maximum number of characters of the memo text shown in a reminder.
 */
internal const val REMINDER_TEXT_MAX_LENGTH: Int = 140

/**
 * Shows reminders to the user. Abstracts the platform notification system.
 */
internal interface MemoNotifier {

    /**
     * Shows a reminder with the memo title and [reminderText]. Does nothing if notifications are not allowed.
     */
    fun showReminder(memo: Memo)
}

/**
 * The first [REMINDER_TEXT_MAX_LENGTH] characters of the memo text. Never cuts a character made of two
 * UTF-16 chars (e.g. an emoji) in half.
 */
internal fun Memo.reminderText(): String {
    if (description.length <= REMINDER_TEXT_MAX_LENGTH) return description
    val end = if (description[REMINDER_TEXT_MAX_LENGTH - 1].isHighSurrogate()) REMINDER_TEXT_MAX_LENGTH - 1 else REMINDER_TEXT_MAX_LENGTH
    return description.substring(0, end)
}
