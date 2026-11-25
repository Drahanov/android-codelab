package com.sap.codelab.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sap.codelab.domain.repository.MemoRepository
import com.sap.codelab.domain.model.Memo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the Home Activity.
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class HomeViewModel(private val repository: MemoRepository) : ViewModel() {

    private val showAll = MutableStateFlow(false)

    /**
     * The memos to display. Updates automatically when the filter or the underlying data changes.
     */
    val memos: StateFlow<List<Memo>> = showAll
        .flatMapLatest { all -> if (all) repository.observeAll() else repository.observeOpen() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /**
     * Shows all memos.
     */
    fun loadAllMemos() {
        showAll.value = true
    }

    /**
     * Shows only open (not done) memos.
     */
    fun loadOpenMemos() {
        showAll.value = false
    }

    /**
     * Updates the given memo, marking it as done if isChecked is true.
     *
     * @param memo      - the memo to update.
     * @param isChecked - whether the memo has been checked (marked as done).
     */
    fun updateMemo(memo: Memo, isChecked: Boolean) {
        // We'll only forward the update if the memo has been checked, since we don't offer to uncheck memos right now
        if (!isChecked) return
        viewModelScope.launch {
            repository.saveMemo(memo.copy(isDone = true))
        }
    }
}
