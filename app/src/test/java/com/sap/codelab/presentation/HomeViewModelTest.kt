package com.sap.codelab.presentation

import androidx.lifecycle.SavedStateHandle
import com.sap.codelab.domain.model.Memo
import com.sap.codelab.domain.permission.ReminderPermission
import com.sap.codelab.domain.usecase.MarkMemoDoneUseCase
import com.sap.codelab.domain.usecase.RestoreGeofencesUseCase
import com.sap.codelab.fakes.BERLIN
import com.sap.codelab.fakes.FakeGeofenceRegistrar
import com.sap.codelab.fakes.FakeMemoRepository
import com.sap.codelab.fakes.FakePermissionChecker
import com.sap.codelab.fakes.MainDispatcherRule
import com.sap.codelab.presentation.home.HomeViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val open = Memo(id = 1, title = "Open", description = "x", location = BERLIN)
    private val done = Memo(id = 2, title = "Done", description = "x", isDone = true)
    private val repository = FakeMemoRepository(open, done)
    private val registrar = FakeGeofenceRegistrar()
    private val permissions = FakePermissionChecker()

    // uiState only updates while collected (WhileSubscribed), like when the screen is visible
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun TestScope.collectState(viewModel: HomeViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
    }

    private fun viewModel() = HomeViewModel(
        repository = repository,
        markMemoDone = MarkMemoDoneUseCase(repository, registrar),
        restoreGeofences = RestoreGeofencesUseCase(repository, registrar),
        permissionChecker = permissions,
        savedStateHandle = SavedStateHandle(),
    )

    @Test
    fun `shows open memos by default and all memos on request`() = runTest {
        val viewModel = viewModel()
        collectState(viewModel)

        assertEquals(listOf(open), viewModel.uiState.value.memos)

        viewModel.setShowAll(true)
        assertEquals(listOf(open, done), viewModel.uiState.value.memos)
    }

    @Test
    fun `marking done removes the memo from the open list`() = runTest {
        val viewModel = viewModel()
        collectState(viewModel)

        viewModel.markDone(open)

        assertTrue(viewModel.uiState.value.memos.isEmpty())
        assertEquals(listOf(1L), registrar.unregistered)
    }

    @Test
    fun `shows the next missing permission`() = runTest {
        permissions.missing = listOf(ReminderPermission.NOTIFICATIONS, ReminderPermission.BACKGROUND_LOCATION)
        val viewModel = viewModel()
        collectState(viewModel)

        assertEquals(ReminderPermission.NOTIFICATIONS, viewModel.uiState.value.missingPermission)
    }

    @Test
    fun `memos are registered once permissions become complete`() = runTest {
        permissions.missing = listOf(ReminderPermission.BACKGROUND_LOCATION)
        val viewModel = viewModel()
        collectState(viewModel)
        assertTrue(registrar.registered.isEmpty())

        permissions.missing = emptyList()
        viewModel.refreshPermissions()

        assertNull(viewModel.uiState.value.missingPermission)
        assertEquals(listOf(1L), registrar.registered.map { it.memoId })

        // Refreshing again (e.g. every time the screen is shown) does not register again
        viewModel.refreshPermissions()
        assertEquals(1, registrar.registered.size)
    }
}
