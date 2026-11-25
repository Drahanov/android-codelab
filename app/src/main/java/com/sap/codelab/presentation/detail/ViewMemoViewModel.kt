package com.sap.codelab.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sap.codelab.domain.model.Memo
import com.sap.codelab.domain.repository.MemoRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

/**
 * UI state of the ViewMemo screen.
 */
internal sealed interface ViewMemoUiState {
    data object Loading : ViewMemoUiState
    data class Loaded(val memo: Memo) : ViewMemoUiState

    /** The memo does not exist (anymore), e.g. when opened from an outdated notification. */
    data object NotFound : ViewMemoUiState
}

/**
 * ViewModel for matching ViewMemo view. The memo id comes from the intent extras via [SavedStateHandle].
 */
internal class ViewMemoViewModel(
    repository: MemoRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val memoId: Long? = savedStateHandle[BUNDLE_MEMO_ID]

    val uiState: StateFlow<ViewMemoUiState> = flow {
        val memo = memoId?.let { repository.getMemoById(it) }
        emit(if (memo != null) ViewMemoUiState.Loaded(memo) else ViewMemoUiState.NotFound)
    }.stateIn(viewModelScope, SharingStarted.Lazily, ViewMemoUiState.Loading)
}
