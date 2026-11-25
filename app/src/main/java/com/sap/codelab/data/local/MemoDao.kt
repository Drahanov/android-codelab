package com.sap.codelab.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data access object for memos.
 */
@Dao
internal interface MemoDao {

    @Query("SELECT * FROM memo")
    fun observeAll(): Flow<List<MemoEntity>>

    @Query("SELECT * FROM memo WHERE isDone = 0")
    fun observeOpen(): Flow<List<MemoEntity>>

    /**
     * Inserts the given memo, replacing an existing one with the same id.
     *
     * @return the row id of the inserted memo.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(memo: MemoEntity): Long

    @Query("SELECT * FROM memo WHERE id = :memoId")
    suspend fun getMemoById(memoId: Long): MemoEntity?
}
