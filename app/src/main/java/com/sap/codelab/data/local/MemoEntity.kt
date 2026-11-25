package com.sap.codelab.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room representation of a memo.
 */
@Entity(tableName = "memo")
internal data class MemoEntity(
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "description")
    val description: String,
    @ColumnInfo(name = "reminderDate")
    val reminderDate: Long,
    @ColumnInfo(name = "reminderLatitude")
    val reminderLatitude: Double?,
    @ColumnInfo(name = "reminderLongitude")
    val reminderLongitude: Double?,
    @ColumnInfo(name = "isDone")
    val isDone: Boolean,
)
