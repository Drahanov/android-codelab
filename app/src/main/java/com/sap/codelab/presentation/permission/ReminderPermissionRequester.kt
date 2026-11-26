package com.sap.codelab.presentation.permission

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withResumed
import com.sap.codelab.R
import com.sap.codelab.domain.permission.ReminderPermission
import com.sap.codelab.platform.permission.manifestPermissions
import com.sap.codelab.platform.permission.reminderPermissionOf
import kotlinx.coroutines.launch

/**
 * Requests the [ReminderPermission]s one after another: each granted permission continues with the next missing one.
 * Must be created before the activity is started (e.g. as a property), because it registers an activity result.
 *
 * @param nextMissing         - returns the next permission that is still missing, or null.
 * @param onResult            - called after every request, so the caller can refresh its state.
 * @param onPermanentlyDenied - called when the system will not show the request dialog for a permission anymore.
 */
internal class ReminderPermissionRequester(
    private val activity: ComponentActivity,
    private val nextMissing: () -> ReminderPermission?,
    private val onResult: () -> Unit,
    private val onPermanentlyDenied: (ReminderPermission) -> Unit,
) {

    private val launcher = activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
        // Empty if the request was interrupted, e.g. by a configuration change
        val requested = reminderPermissionOf(results.keys) ?: return@registerForActivityResult
        onResult()
        if (nextMissing() != requested) {
            // Granted: continue with the next one. Wait until the system dialog is fully closed, otherwise the
            // system rejects the new request as "already in progress" and reports it as denied.
            activity.lifecycleScope.launch { activity.withResumed { requestNext() } }
        } else if (requested.manifestPermissions().none(activity::shouldShowRequestPermissionRationale)) {
            onPermanentlyDenied(requested)
        }
    }

    /**
     * Requests the next missing permission. Background location first explains what to choose on the next screen.
     */
    fun requestNext() {
        val permission = nextMissing() ?: return
        if (permission == ReminderPermission.BACKGROUND_LOCATION) {
            showBackgroundRationale { launch(permission) }
        } else {
            launch(permission)
        }
    }

    /**
     * Opens the app's system settings, where permanently denied permissions can still be granted.
     */
    fun openAppSettings() {
        activity.startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", activity.packageName, null))
        )
    }

    private fun launch(permission: ReminderPermission) {
        launcher.launch(permission.manifestPermissions().toTypedArray())
    }

    private fun showBackgroundRationale(onContinue: () -> Unit) {
        // The label of the "all the time" option differs between Android versions and languages
        val optionLabel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.packageManager.backgroundPermissionOptionLabel
        } else {
            activity.getString(R.string.permission_background_option_fallback)
        }
        AlertDialog.Builder(activity)
            .setTitle(R.string.permission_background_title)
            .setMessage(activity.getString(R.string.permission_background_rationale, optionLabel))
            .setPositiveButton(R.string.action_continue) { _, _ -> onContinue() }
            .setNegativeButton(R.string.action_not_now, null)
            .show()
    }
}
