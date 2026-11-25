package com.sap.codelab.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

internal const val DATABASE_NAME: String = "codelab"

/**
 * That database that is used to store information.
 */
@Database(entities = [MemoEntity::class], version = 2, exportSchema = true)
internal abstract class AppDatabase : RoomDatabase() {

    abstract fun getMemoDao(): MemoDao
}
