package com.sap.codelab.platform.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.sap.codelab.domain.usecase.OnMemoLocationReachedUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

private const val TAG = "GeofenceReceiver"

/**
 * Receives geofence transitions from Play Services, also when the app is not running
 * (the system starts the process for it).
 */
internal class GeofenceBroadcastReceiver : BroadcastReceiver(), KoinComponent {

    private val onMemoLocationReached: OnMemoLocationReachedUseCase by inject()

    override fun onReceive(context: Context, intent: Intent) {
        val event = GeofencingEvent.fromIntent(intent) ?: return
        if (event.hasError()) {
            Log.w(TAG, "Geofence error ${event.errorCode}")
            return
        }
        if (event.geofenceTransition != Geofence.GEOFENCE_TRANSITION_ENTER) return
        val memoIds = event.triggeringGeofences.orEmpty().mapNotNull { it.requestId.toLongOrNull() }
        Log.d(TAG, "Entered area of memos $memoIds")

        // Keeps the receiver alive while the database is read (max. ~10 seconds)
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            try {
                memoIds.forEach { onMemoLocationReached(it) }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
