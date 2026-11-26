package com.sap.codelab.domain

import com.sap.codelab.domain.model.Memo
import com.sap.codelab.domain.usecase.MarkMemoDoneUseCase
import com.sap.codelab.domain.usecase.OnMemoLocationReachedUseCase
import com.sap.codelab.domain.usecase.RestoreGeofencesUseCase
import com.sap.codelab.domain.usecase.SaveMemoUseCase
import com.sap.codelab.fakes.BERLIN
import com.sap.codelab.fakes.FakeGeofenceRegistrar
import com.sap.codelab.fakes.FakeMemoNotifier
import com.sap.codelab.fakes.FakeMemoRepository
import com.sap.codelab.fakes.MUNICH
import com.sap.codelab.fakes.Registration
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UseCasesTest {

    private val registrar = FakeGeofenceRegistrar()
    private val notifier = FakeMemoNotifier()

    @Test
    fun `saving a memo with location registers it and reminds if already inside`() = runTest {
        val repository = FakeMemoRepository()

        SaveMemoUseCase(repository, registrar)(Memo(title = "Milk", description = "2 liters", location = BERLIN))

        val saved = repository.memos.value.single()
        assertEquals(listOf(Registration(saved.id, BERLIN, notifyIfInside = true)), registrar.registered)
    }

    @Test
    fun `saving a memo without location does not register anything`() = runTest {
        SaveMemoUseCase(FakeMemoRepository(), registrar)(Memo(title = "Milk", description = "2 liters"))

        assertTrue(registrar.registered.isEmpty())
    }

    @Test
    fun `marking a memo done saves it and stops watching its location`() = runTest {
        val memo = Memo(id = 1, title = "Milk", description = "2 liters", location = BERLIN)
        val repository = FakeMemoRepository(memo)

        MarkMemoDoneUseCase(repository, registrar)(memo)

        assertTrue(repository.memos.value.single().isDone)
        assertEquals(listOf(1L), registrar.unregistered)
    }

    @Test
    fun `restore registers only open memos with location, without immediate reminder`() = runTest {
        val repository = FakeMemoRepository(
            Memo(id = 1, title = "Open with location", description = "x", location = BERLIN),
            Memo(id = 2, title = "Done", description = "x", location = MUNICH, isDone = true),
            Memo(id = 3, title = "No location", description = "x"),
        )

        RestoreGeofencesUseCase(repository, registrar)()

        assertEquals(listOf(Registration(1, BERLIN, notifyIfInside = false)), registrar.registered)
    }

    @Test
    fun `reaching the location of an open memo shows the reminder`() = runTest {
        val memo = Memo(id = 1, title = "Milk", description = "2 liters", location = BERLIN)

        OnMemoLocationReachedUseCase(FakeMemoRepository(memo), notifier, registrar)(1)

        assertEquals(listOf(memo), notifier.shown)
        assertTrue(registrar.unregistered.isEmpty())
    }

    @Test
    fun `reaching the location of a done memo does not remind and removes the geofence`() = runTest {
        val memo = Memo(id = 1, title = "Milk", description = "2 liters", location = BERLIN, isDone = true)

        OnMemoLocationReachedUseCase(FakeMemoRepository(memo), notifier, registrar)(1)

        assertTrue(notifier.shown.isEmpty())
        assertEquals(listOf(1L), registrar.unregistered)
    }

    @Test
    fun `reaching the location of a deleted memo removes the geofence`() = runTest {
        OnMemoLocationReachedUseCase(FakeMemoRepository(), notifier, registrar)(42)

        assertTrue(notifier.shown.isEmpty())
        assertEquals(listOf(42L), registrar.unregistered)
    }
}
