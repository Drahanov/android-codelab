package com.sap.codelab.presentation.create

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sap.codelab.domain.model.Coordinates
import com.sap.codelab.domain.model.Memo
import com.sap.codelab.domain.usecase.SaveMemoUseCase
import com.sap.codelab.presentation.common.getCoordinates
import com.sap.codelab.presentation.common.setCoordinates
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI state of the CreateMemo screen.
 *
 * @property location the reminder location picked by the user, or null if none has been picked yet.
 * @property isSaved true once the memo has been stored; the screen should close.
 */
internal data class CreateMemoUiState(
    val location: Coordinates? = null,
    val titleError: Boolean = false,
    val descriptionError: Boolean = false,
    val locationError: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
)

/**
 * ViewModel for matching CreateMemo view. Handles user interactions.
 */
internal class CreateMemoViewModel(
    private val saveMemo: SaveMemoUseCase,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    // The location is kept in SavedStateHandle, so it survives process death (e.g. while the map is open).
    private val _uiState = MutableStateFlow(CreateMemoUiState(location = savedStateHandle.getCoordinates()))
    val uiState: StateFlow<CreateMemoUiState> = _uiState.asStateFlow()

    /**
     * Sets the reminder location picked by the user.
     */
    fun setLocation(coordinates: Coordinates) {
        savedStateHandle.setCoordinates(coordinates)
        _uiState.update { it.copy(location = coordinates, locationError = false) }
    }

    /**
     * Validates the input and saves the memo if it is valid; otherwise exposes the errors in [uiState].
     */
    fun save(title: String, description: String) {
        val current = _uiState.value
        if (current.isSaving || current.isSaved) return

        val titleError = title.isBlank()
        val descriptionError = description.isBlank()
        val locationError = current.location == null
        val isValid = !titleError && !descriptionError && !locationError
        _uiState.update {
            it.copy(titleError = titleError, descriptionError = descriptionError, locationError = locationError, isSaving = isValid)
        }
        if (!isValid) return

        viewModelScope.launch {
            saveMemo(Memo(title = title, description = description, location = current.location))
            _uiState.update { it.copy(isSaving = false, isSaved = true) }
        }
    }
}
