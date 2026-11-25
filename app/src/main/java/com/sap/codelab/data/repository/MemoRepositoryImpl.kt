package com.sap.codelab.data.repository

import android.content.Context
import androidx.room.Room
import com.sap.codelab.data.local.AppDatabase
import com.sap.codelab.data.local.DATABASE_NAME
import com.sap.codelab.data.local.MIGRATION_1_2
import com.sap.codelab.data.local.MemoDao
import com.sap.codelab.data.local.toDomain
import com.sap.codelab.data.local.toEntity
import com.sap.codelab.domain.model.Memo
import com.sap.codelab.domain.repository.MemoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Room backed [MemoRepository]. Main-safe: Room runs suspend queries and flows on its own executors.
 *
 * TODO(step 3): replace this singleton with an injected instance.
 */
internal object MemoRepositoryImpl : MemoRepository {

    private lateinit var dao: MemoDao

    fun initialize(applicationContext: Context) {
        dao = Room.databaseBuilder(applicationContext, AppDatabase::class.java, DATABASE_NAME)
            .addMigrations(MIGRATION_1_2)
            .build()
            .getMemoDao()
    }

    override suspend fun saveMemo(memo: Memo): Long = dao.insert(memo.toEntity())

    override fun observeAll(): Flow<List<Memo>> = dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeOpen(): Flow<List<Memo>> = dao.observeOpen().map { list -> list.map { it.toDomain() } }

    override suspend fun getMemoById(id: Long): Memo? = dao.getMemoById(id)?.toDomain()
}
