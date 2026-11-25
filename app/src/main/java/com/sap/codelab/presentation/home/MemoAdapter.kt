package com.sap.codelab.presentation.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.sap.codelab.databinding.RecyclerviewMemoBinding
import com.sap.codelab.domain.model.Memo

/**
 * Adapter displaying a list of memos. Use [submitList] to update the items.
 *
 * @param onClick    - called when the user selects a memo.
 * @param onMarkDone - called when the user ticks the checkbox of an open memo.
 */
internal class MemoAdapter(
    private val onClick: (Memo) -> Unit,
    private val onMarkDone: (Memo) -> Unit,
) : ListAdapter<Memo, MemoViewHolder>(MemoDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoViewHolder {
        val binding = RecyclerviewMemoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MemoViewHolder(binding, onClick, onMarkDone)
    }

    override fun onBindViewHolder(holder: MemoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private object MemoDiffCallback : DiffUtil.ItemCallback<Memo>() {
        override fun areItemsTheSame(oldItem: Memo, newItem: Memo): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Memo, newItem: Memo): Boolean = oldItem == newItem
    }
}
