package com.sap.codelab.presentation.create

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.core.content.ContextCompat
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.sap.codelab.R
import com.sap.codelab.databinding.ActivityCreateMemoBinding
import com.sap.codelab.presentation.common.collectWhenStarted
import com.sap.codelab.presentation.common.toDisplayString
import com.sap.codelab.presentation.picklocation.PickLocationContract

/**
 * Activity that allows a user to create a new Memo.
 */
internal class CreateMemo : AppCompatActivity() {

    private lateinit var binding: ActivityCreateMemoBinding
    private val model: CreateMemoViewModel by viewModel()
    private val pickLocation = registerForActivityResult(PickLocationContract()) { coordinates ->
        coordinates?.let(model::setLocation)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateMemoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        binding.contentCreateMemo.memoLocationButton.setOnClickListener {
            pickLocation.launch(model.uiState.value.location)
        }
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
            memoLocation.text = when {
                state.location != null -> state.location.toDisplayString()
                state.locationError -> getString(R.string.memo_location_empty_error)
                else -> getString(R.string.no_location)
            }
            if (state.locationError) {
                memoLocation.setTextColor(ContextCompat.getColor(this@CreateMemo, R.color.error))
            } else {
                memoLocation.setTextColor(memoDescription.currentTextColor)
            }
            memoLocationButton.setText(if (state.location != null) R.string.change_location else R.string.choose_location)
        }
    }
}
