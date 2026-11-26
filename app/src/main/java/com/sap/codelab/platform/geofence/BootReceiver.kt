package com.sap.codelab.platform.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.sap.codelab.domain.usecase.RestoreGeofencesUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

private const val TAG = "BootReceiver"

/**
 * The system removes all geofences on reboot and when the app is updated. This registers them again,
 * so reminders keep working without the user opening the app.
 */
internal class BootReceiver : BroadcastReceiver(), KoinComponent {

    private val restoreGeofences: RestoreGeofencesUseCase by inject()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED && intent.action != Intent.ACTION_MY_PACKAGE_REPLACED) return
        Log.d(TAG, "Restoring geofences after ${intent.action}")

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            try {
                restoreGeofences()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
