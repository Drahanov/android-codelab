package com.sap.codelab.domain.usecase

import com.sap.codelab.domain.location.GeofenceRegistrar
import com.sap.codelab.domain.model.Memo
import com.sap.codelab.domain.repository.MemoRepository

/**
 * Saves a memo and, if it is open and has a location, starts watching that location.
 */
internal class SaveMemoUseCase(
    private val repository: MemoRepository,
    private val geofenceRegistrar: GeofenceRegistrar,
) {
    suspend operator fun invoke(memo: Memo) {
        val id = repository.saveMemo(memo)
        val location = memo.location
        if (location != null && !memo.isDone) {
            // If this fails (e.g. permission missing), RestoreGeofencesUseCase registers it later
            geofenceRegistrar.register(id, location, notifyIfInside = true)
        }
    }
}
