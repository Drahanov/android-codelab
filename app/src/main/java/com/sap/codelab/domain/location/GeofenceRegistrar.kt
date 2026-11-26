package com.sap.codelab.domain.location

import com.sap.codelab.domain.model.Coordinates

/**
 * Distance to a memo's location at which the user gets reminded.
 */
internal const val REMINDER_RADIUS_METERS: Float = 200f

/**
 * Lets the system watch memo locations, so the app is notified when the user enters one, even while it is not running.
 * Abstracts the concrete location service. All functions are main-safe.
 */
internal interface GeofenceRegistrar {

    /**
     * Starts watching a [REMINDER_RADIUS_METERS] circle around [location]; replaces an existing one for the same memo.
     *
     * @param notifyIfInside - also trigger right away if the user is already inside the circle. Use it for new memos,
     *                         not when registering existing ones again (would remind on every app start).
     * @return true if registered; false if not possible right now (missing permission, location turned off, limit reached).
     */
    suspend fun register(memoId: Long, location: Coordinates, notifyIfInside: Boolean): Boolean

    /**
     * Stops watching the location of the given memo. Does nothing if it is not watched.
     */
    suspend fun unregister(memoId: Long)
}
