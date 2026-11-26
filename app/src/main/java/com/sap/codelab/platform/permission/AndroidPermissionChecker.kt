package com.sap.codelab.platform.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.sap.codelab.domain.permission.PermissionChecker
import com.sap.codelab.domain.permission.ReminderPermission

/**
 * [PermissionChecker] based on the Android runtime permissions. Permissions that do not exist on the
 * running Android version count as granted.
 */
internal class AndroidPermissionChecker(private val context: Context) : PermissionChecker {

    override fun missingReminderPermissions(): List<ReminderPermission> =
        ReminderPermission.entries.filterNot(::isGranted)

    override fun hasPreciseLocation(): Boolean = isGranted(ReminderPermission.PRECISE_LOCATION)

    override fun canMonitorGeofences(): Boolean =
        isGranted(ReminderPermission.PRECISE_LOCATION) && isGranted(ReminderPermission.BACKGROUND_LOCATION)

    private fun isGranted(permission: ReminderPermission): Boolean =
        permission.manifestPermissions().all { granted(it) } && when (permission) {
            // Coarse is requested together with fine (required since Android 12), but only fine counts
            ReminderPermission.PRECISE_LOCATION -> granted(Manifest.permission.ACCESS_FINE_LOCATION)
            else -> true
        }

    private fun granted(permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}

/**
 * The Android permissions to request for this [ReminderPermission]; empty if not needed on this Android version.
 */
internal fun ReminderPermission.manifestPermissions(): List<String> = when (this) {
    ReminderPermission.PRECISE_LOCATION ->
        listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

    ReminderPermission.NOTIFICATIONS ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) listOf(Manifest.permission.POST_NOTIFICATIONS) else emptyList()

    ReminderPermission.BACKGROUND_LOCATION ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) listOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION) else emptyList()
}

/**
 * Maps the keys of a permission request result back to the [ReminderPermission] that was requested.
 */
internal fun reminderPermissionOf(manifestPermissions: Collection<String>): ReminderPermission? =
    ReminderPermission.entries.firstOrNull { permission ->
        val expected = permission.manifestPermissions()
        expected.isNotEmpty() && manifestPermissions.containsAll(expected)
    }
