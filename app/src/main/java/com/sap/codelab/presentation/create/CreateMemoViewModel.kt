package com.sap.codelab.presentation.create

import androidx.lifecycle.ViewModel
import com.sap.codelab.domain.model.Memo
import com.sap.codelab.domain.repository.MemoRepository
import com.sap.codelab.utils.extensions.empty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * ViewModel for matching CreateMemo view. Handles user interactions.
 *
 * @param applicationScope - used for saving, so the save completes even though the activity finishes right away.
 */
internal class CreateMemoViewModel(
    private val repository: MemoRepository,
    private val applicationScope: CoroutineScope,
) : ViewModel() {

    private var memo = Memo(title = String.empty(), description = String.empty())

    /**
     * Saves the memo in it's current state.
     */
    fun saveMemo() {
        applicationScope.launch {
            repository.saveMemo(memo)
        }
    }

    /**
     * Call this method to update the memo. This is usually needed when the user changed his input.
     */
    fun updateMemo(title: String, description: String) {
        memo = Memo(title = title, description = description)
    }

    /**
     * @return true if the title and content are not blank; false otherwise.
     */
    fun isMemoValid(): Boolean = memo.title.isNotBlank() && memo.description.isNotBlank()

    /**
     * @return true if the memo text is blank, false otherwise.
     */
    fun hasTextError() = memo.description.isBlank()

    /**
     * @return true if the memo title is blank, false otherwise.
     */
    fun hasTitleError() = memo.title.isBlank()
}