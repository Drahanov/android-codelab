package com.sap.codelab.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * v1 -> v2: reminder coordinates change from INTEGER NOT NULL to nullable REAL.
 *
 * SQLite cannot change a column type in place, so the table is recreated. The old values were
 * never set by the app (always 0) and integers cannot hold real coordinates anyway, so they become NULL.
 */
internal val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE memo_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                title TEXT NOT NULL,
                description TEXT NOT NULL,
                reminderDate INTEGER NOT NULL,
                reminderLatitude REAL,
                reminderLongitude REAL,
                isDone INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO memo_new (id, title, description, reminderDate, reminderLatitude, reminderLongitude, isDone)
            SELECT id, title, description, reminderDate, NULL, NULL, isDone FROM memo
            """.trimIndent()
        )
        db.execSQL("DROP TABLE memo")
        db.execSQL("ALTER TABLE memo_new RENAME TO memo")
    }
}
