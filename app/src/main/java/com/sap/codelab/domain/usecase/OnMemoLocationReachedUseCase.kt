package com.sap.codelab.domain.usecase

import com.sap.codelab.domain.location.GeofenceRegistrar
import com.sap.codelab.domain.notification.MemoNotifier
import com.sap.codelab.domain.repository.MemoRepository

/**
 * Called when the user entered the area of a memo: shows the reminder. The location stays watched,
 * so the user is reminded on every visit until the memo is marked as done.
 */
internal class OnMemoLocationReachedUseCase(
    private val repository: MemoRepository,
    private val notifier: MemoNotifier,
    private val geofenceRegistrar: GeofenceRegistrar,
) {
    suspend operator fun invoke(memoId: Long) {
        val memo = repository.getMemoById(memoId)
        if (memo == null || memo.isDone) {
            // Outdated geofence, e.g. the memo was completed while its removal failed
            geofenceRegistrar.unregister(memoId)
            return
        }
        notifier.showReminder(memo)
    }
}
