package com.sap.codelab.domain.usecase

import com.sap.codelab.domain.location.GeofenceRegistrar
import com.sap.codelab.domain.repository.MemoRepository
import kotlinx.coroutines.flow.first

/**
 * Registers the locations of all open memos again. Needed when they could not be registered before
 * (permission granted later) or the system dropped them (e.g. after a reboot). Registering again is harmless.
 */
internal class RestoreGeofencesUseCase(
    private val repository: MemoRepository,
    private val geofenceRegistrar: GeofenceRegistrar,
) {
    suspend operator fun invoke() {
        repository.observeOpen().first().forEach { memo ->
            memo.location?.let { geofenceRegistrar.register(memo.id, it, notifyIfInside = false) }
        }
    }
}
