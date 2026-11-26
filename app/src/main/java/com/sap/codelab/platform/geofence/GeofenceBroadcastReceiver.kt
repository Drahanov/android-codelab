package com.sap.codelab.platform.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.GeofencingEvent

private const val TAG = "GeofenceReceiver"

/**
 * Receives geofence transitions from Play Services, also when the app is not running.
 * Showing the reminder notification is added in the next step; for now the transition is only logged.
 */
internal class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val event = GeofencingEvent.fromIntent(intent) ?: return
        if (event.hasError()) {
            Log.w(TAG, "Geofence error ${event.errorCode}")
            return
        }
        val memoIds = event.triggeringGeofences.orEmpty().map { it.requestId }
        Log.d(TAG, "Transition ${event.geofenceTransition} for memos $memoIds")
    }
}
