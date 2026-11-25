package com.sap.codelab.presentation.create

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.sap.codelab.R
import com.sap.codelab.databinding.ActivityCreateMemoBinding
import com.sap.codelab.presentation.common.collectWhenStarted

/**
 * Activity that allows a user to create a new Memo.
 */
internal class CreateMemo : AppCompatActivity() {

    private lateinit var binding: ActivityCreateMemoBinding
    private val model: CreateMemoViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateMemoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        collectWhenStarted(model.uiState, ::render)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_create_memo, menu)
        return true
    }

    /**
     * Handles actionbar interactions.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save -> {
                saveMemo()
                true
            }

            else             -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Passes the current input to the ViewModel, which validates and saves it.
     */
    private fun saveMemo() {
        binding.contentCreateMemo.run {
            model.save(memoTitle.text.toString(), memoDescription.text.toString())
        }
    }

    private fun render(state: CreateMemoUiState) {
        if (state.isSaved) {
            finish()
            return
        }
        binding.contentCreateMemo.run {
            memoTitleContainer.error = if (state.titleError) getString(R.string.memo_title_empty_error) else null
            memoDescription.error = if (state.descriptionError) getString(R.string.memo_text_empty_error) else null
        }
    }
}
