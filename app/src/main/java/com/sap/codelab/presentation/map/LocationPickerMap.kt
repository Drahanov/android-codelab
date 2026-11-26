package com.sap.codelab.presentation.map

import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import com.sap.codelab.domain.model.Coordinates

/**
 * An interactive map that lets the user pick a point. Abstracts the concrete map library,
 * so it can be swapped (e.g. osmdroid -> Google Maps) without touching the screens.
 */
internal interface LocationPickerMap {

    /**
     * Creates the map view inside [container]. The map follows [lifecycleOwner] (resume/pause/destroy) by itself.
     */
    fun attach(container: ViewGroup, lifecycleOwner: LifecycleOwner)

    /**
     * Moves the camera to the given point.
     */
    fun moveTo(coordinates: Coordinates, zoom: Double)

    /**
     * Shows a marker at the given point, or removes it when null.
     */
    fun showMarker(coordinates: Coordinates?)

    /**
     * Called with the tapped point whenever the user taps the map.
     */
    fun setOnMapTapListener(listener: (Coordinates) -> Unit)
}
