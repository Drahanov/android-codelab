package com.sap.codelab.presentation.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sap.codelab.domain.model.Memo
import com.sap.codelab.domain.permission.PermissionChecker
import com.sap.codelab.domain.permission.ReminderPermission
import com.sap.codelab.domain.repository.MemoRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val KEY_SHOW_ALL = "showAll"

/**
 * UI state of the Home screen.
 *
 * @property showAll           true if done memos are shown as well; false if only open memos are shown.
 * @property missingPermission the next permission reminders need, or null if all are granted.
 * @property openSettings      true if [missingPermission] was permanently denied and can only be granted in the app settings.
 */
internal data class HomeUiState(
    val memos: List<Memo> = emptyList(),
    val showAll: Boolean = false,
    val missingPermission: ReminderPermission? = null,
    val openSettings: Boolean = false,
)

/**
 * ViewModel for the Home Activity.
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class HomeViewModel(
    private val repository: MemoRepository,
    private val permissionChecker: PermissionChecker,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    // Kept in SavedStateHandle so the filter survives process death.
    private val showAll: StateFlow<Boolean> = savedStateHandle.getStateFlow(KEY_SHOW_ALL, false)
    private val missingPermission = MutableStateFlow(nextMissingPermission())
    private val permanentlyDenied = MutableStateFlow<ReminderPermission?>(null)

    private val memos = showAll.flatMapLatest { all ->
        if (all) repository.observeAll() else repository.observeOpen()
    }

    /**
     * Updates automatically when the filter, the underlying data or the permissions change.
     */
    val uiState: StateFlow<HomeUiState> = combine(showAll, memos, missingPermission, permanentlyDenied) { all, memos, missing, denied ->
        HomeUiState(
            memos = memos,
            showAll = all,
            missingPermission = missing,
            openSettings = missing != null && missing == denied,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        HomeUiState(showAll = showAll.value, missingPermission = missingPermission.value),
    )

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

    /**
     * Re-checks the permissions; call it whenever they may have changed (screen visible again, request finished).
     */
    fun refreshPermissions() {
        missingPermission.value = nextMissingPermission()
    }

    /**
     * @return the next permission reminders need, checked right now; null if all are granted.
     */
    fun nextMissingPermission(): ReminderPermission? = permissionChecker.missingReminderPermissions().firstOrNull()

    /**
     * The system will not show the request dialog for [permission] anymore; the user has to use the app settings.
     */
    fun onPermissionPermanentlyDenied(permission: ReminderPermission) {
        permanentlyDenied.value = permission
    }
}
