package com.sap.codelab.domain.permission

/**
 * Permissions that location based reminders depend on, in the order they should be requested.
 */
internal enum class ReminderPermission {
    /** Precise location; geofencing does not work with approximate location. */
    PRECISE_LOCATION,

    /** Posting the reminder notification (Android 13+). */
    NOTIFICATIONS,

    /** Location access while the app is closed (Android 10+). Can only be requested after [PRECISE_LOCATION]. */
    BACKGROUND_LOCATION,
}

/**
 * Tells which [ReminderPermission]s are granted.
 */
internal interface PermissionChecker {

    /**
     * @return the permissions reminders still need, in request order. Empty if everything is granted.
     */
    fun missingReminderPermissions(): List<ReminderPermission>

    fun hasPreciseLocation(): Boolean
}
