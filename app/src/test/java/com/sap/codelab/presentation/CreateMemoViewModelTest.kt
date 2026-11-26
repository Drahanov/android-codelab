package com.sap.codelab.presentation

import androidx.lifecycle.SavedStateHandle
import com.sap.codelab.domain.usecase.SaveMemoUseCase
import com.sap.codelab.fakes.BERLIN
import com.sap.codelab.fakes.FakeGeofenceRegistrar
import com.sap.codelab.fakes.FakeMemoRepository
import com.sap.codelab.fakes.MainDispatcherRule
import com.sap.codelab.presentation.create.CreateMemoViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CreateMemoViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeMemoRepository()
    private val savedStateHandle = SavedStateHandle()
    private fun viewModel() = CreateMemoViewModel(SaveMemoUseCase(repository, FakeGeofenceRegistrar()), savedStateHandle)

    @Test
    fun `empty input shows all errors and saves nothing`() {
        val viewModel = viewModel()

        viewModel.save(title = " ", description = "")

        val state = viewModel.uiState.value
        assertTrue(state.titleError)
        assertTrue(state.descriptionError)
        assertTrue(state.locationError)
        assertFalse(state.isSaved)
        assertTrue(repository.memos.value.isEmpty())
    }

    @Test
    fun `choosing a location clears the location error`() {
        val viewModel = viewModel()
        viewModel.save(title = "Milk", description = "2 liters")

        viewModel.setLocation(BERLIN)

        assertFalse(viewModel.uiState.value.locationError)
        assertEquals(BERLIN, viewModel.uiState.value.location)
    }

    @Test
    fun `valid input is saved with its location`() {
        val viewModel = viewModel()
        viewModel.setLocation(BERLIN)

        viewModel.save(title = "Milk", description = "2 liters")

        assertTrue(viewModel.uiState.value.isSaved)
        val saved = repository.memos.value.single()
        assertEquals("Milk", saved.title)
        assertEquals(BERLIN, saved.location)
    }

    @Test
    fun `picked location survives process death`() {
        viewModel().setLocation(BERLIN)

        // A new ViewModel with the same SavedStateHandle, as after process death
        assertEquals(BERLIN, viewModel().uiState.value.location)
    }
}
