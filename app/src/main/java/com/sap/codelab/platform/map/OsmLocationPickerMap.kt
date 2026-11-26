package com.sap.codelab.platform.map

import android.content.Context
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.sap.codelab.R
import com.sap.codelab.domain.model.Coordinates
import com.sap.codelab.presentation.map.LocationPickerMap
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import java.io.File

/**
 * [LocationPickerMap] backed by osmdroid (OpenStreetMap tiles, no API key required).
 */
internal class OsmLocationPickerMap : LocationPickerMap {

    private var mapView: MapView? = null
    private var marker: Marker? = null
    private var onTap: ((Coordinates) -> Unit)? = null

    override fun attach(container: ViewGroup, lifecycleOwner: LifecycleOwner) {
        val context = container.context
        configure(context)
        val map = MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            overlays.add(MapEventsOverlay(object : MapEventsReceiver {
                override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                    onTap?.invoke(Coordinates(p.latitude, p.longitude))
                    return true
                }

                override fun longPressHelper(p: GeoPoint): Boolean = false
            }))
        }
        container.addView(map, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        mapView = map

        lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) = map.onResume()
            override fun onPause(owner: LifecycleOwner) = map.onPause()
            override fun onDestroy(owner: LifecycleOwner) {
                map.onDetach()
                mapView = null
                marker = null
            }
        })
    }

    override fun moveTo(coordinates: Coordinates, zoom: Double) {
        mapView?.controller?.apply {
            setZoom(zoom)
            setCenter(coordinates.toGeoPoint())
        }
    }

    override fun showMarker(coordinates: Coordinates?) {
        val map = mapView ?: return
        if (coordinates == null) {
            marker?.let { map.overlays.remove(it) }
            marker = null
        } else {
            val current = marker ?: Marker(map).apply {
                icon = ContextCompat.getDrawable(map.context, R.drawable.ic_location_pin)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                setInfoWindow(null)
                map.overlays.add(this)
            }.also { marker = it }
            current.position = coordinates.toGeoPoint()
        }
        map.invalidate()
    }

    override fun setOnMapTapListener(listener: (Coordinates) -> Unit) {
        onTap = listener
    }

    private fun Coordinates.toGeoPoint() = GeoPoint(latitude, longitude)

    private companion object {
        /**
         * osmdroid requires a user agent (OSM tile usage policy) and a cache location.
         * App-private storage is used, so no storage permission is needed.
         */
        fun configure(context: Context) {
            Configuration.getInstance().apply {
                userAgentValue = context.packageName
                osmdroidBasePath = File(context.filesDir, "osmdroid")
                osmdroidTileCache = File(context.cacheDir, "osmdroid/tiles")
            }
        }
    }
}
