package com.sap.codelab.presentation.common

import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import com.sap.codelab.domain.model.Coordinates
import java.util.Locale

// Coordinates are passed as two doubles, so the domain model stays free of Android Parcelable.
private const val KEY_LATITUDE = "latitude"
private const val KEY_LONGITUDE = "longitude"

internal fun Intent.putCoordinates(coordinates: Coordinates?): Intent = apply {
    if (coordinates != null) {
        putExtra(KEY_LATITUDE, coordinates.latitude)
        putExtra(KEY_LONGITUDE, coordinates.longitude)
    }
}

internal fun Intent.getCoordinates(): Coordinates? =
    if (hasExtra(KEY_LATITUDE) && hasExtra(KEY_LONGITUDE)) {
        Coordinates(getDoubleExtra(KEY_LATITUDE, 0.0), getDoubleExtra(KEY_LONGITUDE, 0.0))
    } else {
        null
    }

/**
 * Reads coordinates stored with [setCoordinates] or passed as intent extras with [putCoordinates].
 */
internal fun SavedStateHandle.getCoordinates(): Coordinates? {
    val latitude: Double = get(KEY_LATITUDE) ?: return null
    val longitude: Double = get(KEY_LONGITUDE) ?: return null
    return Coordinates(latitude, longitude)
}

internal fun SavedStateHandle.setCoordinates(coordinates: Coordinates) {
    set(KEY_LATITUDE, coordinates.latitude)
    set(KEY_LONGITUDE, coordinates.longitude)
}

/**
 * Formats as "52.52000, 13.40500" (about 1 m precision), independent of the device locale.
 */
internal fun Coordinates.toDisplayString(): String = String.format(Locale.US, "%.5f, %.5f", latitude, longitude)
