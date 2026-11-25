package com.sap.codelab.presentation.home

import androidx.recyclerview.widget.RecyclerView
import com.sap.codelab.databinding.RecyclerviewMemoBinding
import com.sap.codelab.domain.model.Memo

/**
 * View holder for Memos. Listeners are set once; they act on the memo currently bound.
 */
internal class MemoViewHolder(
    private val binding: RecyclerviewMemoBinding,
    onClick: (Memo) -> Unit,
    onMarkDone: (Memo) -> Unit,
) : RecyclerView.ViewHolder(binding.root) {

    private var memo: Memo? = null

    init {
        binding.root.setOnClickListener { memo?.let(onClick) }
        binding.checkBox.setOnCheckedChangeListener { _, isChecked ->
            // Only react to user input on open memos; bind() updates the checkbox without a listener call
            val current = memo ?: return@setOnCheckedChangeListener
            if (isChecked && !current.isDone) onMarkDone(current)
        }
    }

    /**
     * Updates the memo view with the given memo.
     */
    fun bind(memo: Memo) {
        this.memo = null // ignore the checkbox callback while (re)binding
        binding.memoTitle.text = memo.title
        binding.memoText.text = memo.description
        binding.checkBox.isChecked = memo.isDone
        // We only let the user edit the checkbox if the item has not been marked as "done"
        binding.checkBox.isEnabled = !memo.isDone
        this.memo = memo
    }
}
