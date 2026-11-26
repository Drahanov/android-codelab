package com.sap.codelab.presentation.home

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.sap.codelab.R
import com.sap.codelab.presentation.common.setupEdgeToEdge
import com.sap.codelab.databinding.ActivityHomeBinding
import com.sap.codelab.domain.permission.ReminderPermission
import com.sap.codelab.presentation.common.collectWhenStarted
import com.sap.codelab.presentation.create.CreateMemo
import com.sap.codelab.presentation.detail.BUNDLE_MEMO_ID
import com.sap.codelab.presentation.detail.ViewMemo
import com.sap.codelab.presentation.permission.ReminderPermissionRequester

/**
 * The main activity of the app. Shows a list of recorded memos and lets the user add new memos.
 */
internal class Home : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val model: HomeViewModel by viewModel()
    private val permissionRequester = ReminderPermissionRequester(
        activity = this,
        nextMissing = { model.nextMissingPermission() },
        onResult = { model.refreshPermissions() },
        onPermanentlyDenied = { model.onPermissionPermanentlyDenied(it) },
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupEdgeToEdge(binding.root, binding.appBar)
        setSupportActionBar(binding.toolbar)

        // Setup the adapter and the recycler view
        setupRecyclerView(initializeAdapter())

        binding.fab.setOnClickListener {
            // Handles clicks on the FAB button > creates a new Memo
            startActivity(Intent(this@Home, CreateMemo::class.java))
        }
        binding.contentHome.permissionButton.setOnClickListener {
            if (model.uiState.value.openSettings) {
                permissionRequester.openAppSettings()
            } else {
                permissionRequester.requestNext()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // The user may have changed permissions in the system settings in the meantime
        model.refreshPermissions()
    }

    /**
     * Initializes the adapter and sets the needed callbacks.
     */
    private fun initializeAdapter() : MemoAdapter {
        val adapter = MemoAdapter(
            onClick = { memo -> showMemo(memo.id) },
            onMarkDone = model::markDone,
        )
        collectWhenStarted(model.uiState) { state ->
            adapter.submitList(state.memos)
            renderPermissionBanner(state)
            invalidateOptionsMenu()
        }
        return adapter
    }

    private fun renderPermissionBanner(state: HomeUiState) {
        binding.contentHome.run {
            permissionBanner.isVisible = state.missingPermission != null
            permissionMessage.setText(
                when (state.missingPermission) {
                    ReminderPermission.PRECISE_LOCATION, null -> R.string.permission_precise_location
                    ReminderPermission.NOTIFICATIONS -> R.string.permission_notifications
                    ReminderPermission.BACKGROUND_LOCATION -> R.string.permission_background_location
                }
            )
            permissionButton.setText(if (state.openSettings) R.string.permission_open_settings else R.string.permission_allow)
        }
    }

    /**
     * Opens the Memo detail view for the given memoId.
     *
     * @param memoId    - the id of the memo to be shown.
     */
    private fun showMemo(memoId: Long) {
        val intent = Intent(this@Home, ViewMemo::class.java)
        intent.putExtra(BUNDLE_MEMO_ID, memoId)
        startActivity(intent)
    }

    /**
     * Initializes the recycler view to display the list of memos.
     */
    private fun setupRecyclerView(adapter: MemoAdapter) {
        binding.contentHome.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@Home, LinearLayoutManager.VERTICAL, false)
            this.adapter = adapter
            addItemDecoration(DividerItemDecoration(this@Home, (layoutManager as LinearLayoutManager).orientation))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        // Only offer the filter that is not currently active
        val showAll = model.uiState.value.showAll
        menu.findItem(R.id.action_show_all).isVisible = !showAll
        menu.findItem(R.id.action_show_open).isVisible = showAll
        return super.onPrepareOptionsMenu(menu)
    }

    /**
     * Handles actionbar interactions.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_show_all -> {
                model.setShowAll(true)
                true
            }
            R.id.action_show_open -> {
                model.setShowAll(false)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}
