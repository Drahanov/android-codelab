package com.sap.codelab.data.repository

import com.sap.codelab.data.local.MemoDao
import com.sap.codelab.data.local.toDomain
import com.sap.codelab.data.local.toEntity
import com.sap.codelab.domain.model.Memo
import com.sap.codelab.domain.repository.MemoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Room backed [MemoRepository]. Main-safe: Room runs suspend queries and flows on its own executors.
 */
internal class MemoRepositoryImpl(private val dao: MemoDao) : MemoRepository {

    override suspend fun saveMemo(memo: Memo): Long = dao.insert(memo.toEntity())

    override fun observeAll(): Flow<List<Memo>> = dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeOpen(): Flow<List<Memo>> = dao.observeOpen().map { list -> list.map { it.toDomain() } }

    override suspend fun getMemoById(id: Long): Memo? = dao.getMemoById(id)?.toDomain()
}
