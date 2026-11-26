package com.sap.codelab.domain.usecase

import com.sap.codelab.domain.location.GeofenceRegistrar
import com.sap.codelab.domain.model.Memo
import com.sap.codelab.domain.repository.MemoRepository

/**
 * Marks a memo as done; a done memo does not remind anymore.
 */
internal class MarkMemoDoneUseCase(
    private val repository: MemoRepository,
    private val geofenceRegistrar: GeofenceRegistrar,
) {
    suspend operator fun invoke(memo: Memo) {
        repository.saveMemo(memo.copy(isDone = true))
        geofenceRegistrar.unregister(memo.id)
    }
}
