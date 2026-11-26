package com.sap.codelab.presentation.picklocation

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.sap.codelab.R
import com.sap.codelab.databinding.ActivityPickLocationBinding
import com.sap.codelab.domain.model.Coordinates
import com.sap.codelab.presentation.common.collectWhenStarted
import com.sap.codelab.presentation.common.putCoordinates
import com.sap.codelab.presentation.map.LocationPickerMap
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

private val DEFAULT_CENTER = Coordinates(latitude = 50.0, longitude = 10.0) // Central Europe
private const val DEFAULT_ZOOM = 4.0
private const val SELECTED_ZOOM = 16.0

/**
 * Full screen map that lets the user pick the location of a memo. Use it via [PickLocationContract].
 */
internal class PickLocation : AppCompatActivity() {

    private lateinit var binding: ActivityPickLocationBinding
    private val model: PickLocationViewModel by viewModel()
    private val map: LocationPickerMap by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPickLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        map.attach(binding.mapContainer, this)
        map.setOnMapTapListener(model::select)
        val initial = model.selected.value
        map.moveTo(initial ?: DEFAULT_CENTER, if (initial != null) SELECTED_ZOOM else DEFAULT_ZOOM)

        collectWhenStarted(model.selected) { selected ->
            map.showMarker(selected)
            binding.hint.isVisible = selected == null
            invalidateOptionsMenu()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_pick_location, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.action_confirm).isEnabled = model.selected.value != null
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_confirm -> {
                setResult(RESULT_OK, Intent().putCoordinates(model.selected.value))
                finish()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
