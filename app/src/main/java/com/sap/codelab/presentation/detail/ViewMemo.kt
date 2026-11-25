package com.sap.codelab.presentation.detail

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.sap.codelab.R
import com.sap.codelab.databinding.ActivityViewMemoBinding
import com.sap.codelab.domain.model.Memo
import com.sap.codelab.presentation.common.collectWhenStarted

internal const val BUNDLE_MEMO_ID: String = "memoId"

/**
 * Activity that allows a user to see the details of a memo.
 */
internal class ViewMemo : AppCompatActivity() {

    private lateinit var binding: ActivityViewMemoBinding
    private val model: ViewMemoViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewMemoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        collectWhenStarted(model.uiState) { state ->
            when (state) {
                ViewMemoUiState.Loading -> Unit
                is ViewMemoUiState.Loaded -> updateUI(state.memo)
                ViewMemoUiState.NotFound -> {
                    Toast.makeText(this@ViewMemo, R.string.memo_not_found, Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    /**
     * Updates the UI with the given memo details.
     *
     * @param memo - the memo whose details are to be displayed.
     */
    private fun updateUI(memo: Memo) {
        binding.contentCreateMemo.run {
            memoTitle.setText(memo.title)
            memoDescription.setText(memo.description)
            memoTitle.isEnabled = false
            memoDescription.isEnabled = false
        }
    }
}
