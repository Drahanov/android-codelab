package com.sap.codelab.domain.repository

import com.sap.codelab.domain.model.Memo
import kotlinx.coroutines.flow.Flow

/**
 * Repository offering memo related operations. All functions are main-safe.
 */
internal interface MemoRepository {

    /**
     * Saves the given memo, replacing an existing one with the same id.
     *
     * @return the id of the saved memo.
     */
    suspend fun saveMemo(memo: Memo): Long

    /**
     * @return all memos, updated on every change.
     */
    fun observeAll(): Flow<List<Memo>>

    /**
     * @return all memos except those marked as "done", updated on every change.
     */
    fun observeOpen(): Flow<List<Memo>>

    /**
     * @return the memo whose id matches the given id, or null if there is none.
     */
    suspend fun getMemoById(id: Long): Memo?
}
