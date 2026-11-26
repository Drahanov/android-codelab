package com.sap.codelab.domain

import com.sap.codelab.domain.model.Memo
import com.sap.codelab.domain.notification.REMINDER_TEXT_MAX_LENGTH
import com.sap.codelab.domain.notification.reminderText
import org.junit.Assert.assertEquals
import org.junit.Test

class ReminderTextTest {

    private fun memo(description: String) = Memo(title = "Title", description = description)

    @Test
    fun `short text is kept`() {
        assertEquals("2 liters", memo("2 liters").reminderText())
    }

    @Test
    fun `text of exactly 140 chars is kept`() {
        val text = "a".repeat(REMINDER_TEXT_MAX_LENGTH)
        assertEquals(text, memo(text).reminderText())
    }

    @Test
    fun `long text is cut to 140 chars`() {
        val text = "a".repeat(REMINDER_TEXT_MAX_LENGTH) + "bcd"
        assertEquals("a".repeat(REMINDER_TEXT_MAX_LENGTH), memo(text).reminderText())
    }

    @Test
    fun `emoji on the boundary is not cut in half`() {
        // The emoji occupies chars 140 and 141 (two UTF-16 chars)
        val text = "a".repeat(REMINDER_TEXT_MAX_LENGTH - 1) + "😀" + "rest"
        assertEquals("a".repeat(REMINDER_TEXT_MAX_LENGTH - 1), memo(text).reminderText())
    }
}
