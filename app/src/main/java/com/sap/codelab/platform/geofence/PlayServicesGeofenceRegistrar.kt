package com.sap.codelab.platform.geofence

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.sap.codelab.domain.location.GeofenceRegistrar
import com.sap.codelab.domain.location.REMINDER_RADIUS_METERS
import com.sap.codelab.domain.model.Coordinates
import com.sap.codelab.domain.permission.PermissionChecker
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

private const val TAG = "GeofenceRegistrar"

/**
 * [GeofenceRegistrar] backed by the Google Play Services geofencing API. Transitions are delivered to
 * [GeofenceBroadcastReceiver]; the geofence request id is the memo id.
 */
internal class PlayServicesGeofenceRegistrar(
    private val context: Context,
    private val permissionChecker: PermissionChecker,
) : GeofenceRegistrar {

    private val client = LocationServices.getGeofencingClient(context)

    // Must be mutable: Play Services adds the transition details to the intent
    private val pendingIntent: PendingIntent by lazy {
        PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, GeofenceBroadcastReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
        )
    }

    @SuppressLint("MissingPermission") // checked via permissionChecker
    override suspend fun register(memoId: Long, location: Coordinates): Boolean {
        if (!permissionChecker.canMonitorGeofences()) {
            Log.w(TAG, "Not registering memo $memoId: location permissions missing")
            return false
        }
        val geofence = Geofence.Builder()
            .setRequestId(memoId.toString())
            .setCircularRegion(location.latitude, location.longitude, REMINDER_RADIUS_METERS)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()
        val request = GeofencingRequest.Builder()
            // Also remind if the user is already inside when the memo is saved
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()
        return try {
            client.addGeofences(request, pendingIntent).await()
            Log.d(TAG, "Registered memo $memoId at $location")
            true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // e.g. location turned off (GEOFENCE_NOT_AVAILABLE) or more than 100 geofences
            Log.w(TAG, "Registering memo $memoId failed", e)
            false
        }
    }

    override suspend fun unregister(memoId: Long) {
        try {
            client.removeGeofences(listOf(memoId.toString())).await()
            Log.d(TAG, "Unregistered memo $memoId")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Unregistering memo $memoId failed", e)
        }
    }
}
