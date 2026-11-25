package com.sap.codelab.data.local

import com.sap.codelab.domain.model.Coordinates
import com.sap.codelab.domain.model.Memo

internal fun MemoEntity.toDomain(): Memo = Memo(
    id = id,
    title = title,
    description = description,
    reminderDate = reminderDate,
    location = if (reminderLatitude != null && reminderLongitude != null) {
        Coordinates(reminderLatitude, reminderLongitude)
    } else {
        null
    },
    isDone = isDone,
)

internal fun Memo.toEntity(): MemoEntity = MemoEntity(
    id = id,
    title = title,
    description = description,
    reminderDate = reminderDate,
    reminderLatitude = location?.latitude,
    reminderLongitude = location?.longitude,
    isDone = isDone,
)
