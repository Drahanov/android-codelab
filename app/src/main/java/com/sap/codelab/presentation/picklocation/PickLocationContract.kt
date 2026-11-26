package com.sap.codelab.presentation.picklocation

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.sap.codelab.domain.model.Coordinates
import com.sap.codelab.presentation.common.getCoordinates
import com.sap.codelab.presentation.common.putCoordinates

/**
 * Opens [PickLocation]. Input: the currently selected point (or null). Output: the picked point, or null if cancelled.
 */
internal class PickLocationContract : ActivityResultContract<Coordinates?, Coordinates?>() {

    override fun createIntent(context: Context, input: Coordinates?): Intent =
        Intent(context, PickLocation::class.java).putCoordinates(input)

    override fun parseResult(resultCode: Int, intent: Intent?): Coordinates? =
        if (resultCode == Activity.RESULT_OK) intent?.getCoordinates() else null
}
