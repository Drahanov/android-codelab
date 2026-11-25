package com.sap.codelab.presentation.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sap.codelab.domain.model.Memo
import com.sap.codelab.domain.repository.MemoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI state of the CreateMemo screen.
 *
 * @property isSaved true once the memo has been stored; the screen should close.
 */
internal data class CreateMemoUiState(
    val titleError: Boolean = false,
    val descriptionError: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
)

/**
 * ViewModel for matching CreateMemo view. Handles user interactions.
 */
internal class CreateMemoViewModel(private val repository: MemoRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateMemoUiState())
    val uiState: StateFlow<CreateMemoUiState> = _uiState.asStateFlow()

    /**
     * Validates the input and saves the memo if it is valid; otherwise exposes the errors in [uiState].
     */
    fun save(title: String, description: String) {
        val current = _uiState.value
        if (current.isSaving || current.isSaved) return

        val titleError = title.isBlank()
        val descriptionError = description.isBlank()
        val isValid = !titleError && !descriptionError
        _uiState.update { it.copy(titleError = titleError, descriptionError = descriptionError, isSaving = isValid) }
        if (!isValid) return

        viewModelScope.launch {
            repository.saveMemo(Memo(title = title, description = description))
            _uiState.update { it.copy(isSaving = false, isSaved = true) }
        }
    }
}
