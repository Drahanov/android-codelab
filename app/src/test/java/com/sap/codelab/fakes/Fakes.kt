package com.sap.codelab.fakes

import com.sap.codelab.domain.location.GeofenceRegistrar
import com.sap.codelab.domain.model.Coordinates
import com.sap.codelab.domain.model.Memo
import com.sap.codelab.domain.notification.MemoNotifier
import com.sap.codelab.domain.permission.PermissionChecker
import com.sap.codelab.domain.permission.ReminderPermission
import com.sap.codelab.domain.repository.MemoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** In-memory repository; assigns ids like Room's autoGenerate. */
internal class FakeMemoRepository(vararg initial: Memo) : MemoRepository {
    val memos = MutableStateFlow(initial.toList())
    private var nextId = (initial.maxOfOrNull { it.id } ?: 0) + 1

    override suspend fun saveMemo(memo: Memo): Long {
        val saved = if (memo.id == 0L) memo.copy(id = nextId++) else memo
        memos.value = memos.value.filterNot { it.id == saved.id } + saved
        return saved.id
    }

    override fun observeAll(): Flow<List<Memo>> = memos
    override fun observeOpen(): Flow<List<Memo>> = memos.map { list -> list.filterNot { it.isDone } }
    override suspend fun getMemoById(id: Long): Memo? = memos.value.firstOrNull { it.id == id }
}

internal data class Registration(val memoId: Long, val location: Coordinates, val notifyIfInside: Boolean)

internal class FakeGeofenceRegistrar : GeofenceRegistrar {
    val registered = mutableListOf<Registration>()
    val unregistered = mutableListOf<Long>()

    override suspend fun register(memoId: Long, location: Coordinates, notifyIfInside: Boolean): Boolean {
        registered += Registration(memoId, location, notifyIfInside)
        return true
    }

    override suspend fun unregister(memoId: Long) {
        unregistered += memoId
    }
}

internal class FakeMemoNotifier : MemoNotifier {
    val shown = mutableListOf<Memo>()
    override fun showReminder(memo: Memo) {
        shown += memo
    }
}

internal class FakePermissionChecker(var missing: List<ReminderPermission> = emptyList()) : PermissionChecker {
    override fun missingReminderPermissions(): List<ReminderPermission> = missing
    override fun hasPreciseLocation(): Boolean = ReminderPermission.PRECISE_LOCATION !in missing
    override fun canMonitorGeofences(): Boolean =
        hasPreciseLocation() && ReminderPermission.BACKGROUND_LOCATION !in missing
}

internal val BERLIN = Coordinates(52.52, 13.405)
internal val MUNICH = Coordinates(48.137, 11.575)
