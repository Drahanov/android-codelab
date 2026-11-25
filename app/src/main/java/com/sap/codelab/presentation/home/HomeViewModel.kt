package com.sap.codelab.presentation.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sap.codelab.domain.model.Memo
import com.sap.codelab.domain.repository.MemoRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val KEY_SHOW_ALL = "showAll"

/**
 * UI state of the Home screen.
 *
 * @property showAll true if done memos are shown as well; false if only open memos are shown.
 */
internal data class HomeUiState(
    val memos: List<Memo> = emptyList(),
    val showAll: Boolean = false,
)

/**
 * ViewModel for the Home Activity.
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class HomeViewModel(
    private val repository: MemoRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    // Kept in SavedStateHandle so the filter survives process death.
    private val showAll: StateFlow<Boolean> = savedStateHandle.getStateFlow(KEY_SHOW_ALL, false)

    /**
     * Updates automatically when the filter or the underlying data changes.
     */
    val uiState: StateFlow<HomeUiState> = showAll
        .flatMapLatest { all ->
            val memos = if (all) repository.observeAll() else repository.observeOpen()
            memos.map { HomeUiState(memos = it, showAll = all) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState(showAll = showAll.value))

    /**
     * Switches between showing all memos and only open (not done) memos.
     */
    fun setShowAll(showAll: Boolean) {
        savedStateHandle[KEY_SHOW_ALL] = showAll
    }

    /**
     * Marks the given memo as done. Unchecking is not supported.
     */
    fun markDone(memo: Memo) {
        viewModelScope.launch {
            repository.saveMemo(memo.copy(isDone = true))
        }
    }
}
