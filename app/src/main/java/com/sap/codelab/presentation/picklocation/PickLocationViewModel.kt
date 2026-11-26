package com.sap.codelab.presentation.picklocation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.sap.codelab.domain.model.Coordinates
import com.sap.codelab.presentation.common.getCoordinates
import com.sap.codelab.presentation.common.setCoordinates
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for [PickLocation]. Starts with the point passed in the intent extras (if any).
 */
internal class PickLocationViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    private val _selected = MutableStateFlow(savedStateHandle.getCoordinates())

    /**
     * The point the user selected, or null if nothing has been selected yet.
     */
    val selected: StateFlow<Coordinates?> = _selected.asStateFlow()

    fun select(coordinates: Coordinates) {
        savedStateHandle.setCoordinates(coordinates)
        _selected.value = coordinates
    }
}
