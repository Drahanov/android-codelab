package com.sap.codelab.presentation.common

import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

/**
 * Draws the activity behind the system bars (enforced from targetSdk 35) and pads the views, so nothing is hidden:
 * the app bar gets the status bar height, the rest of [root] the navigation bar / keyboard / cutouts.
 */
internal fun ComponentActivity.setupEdgeToEdge(root: View, appBar: View) {
    enableEdgeToEdge()
    ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
        val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
        val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
        appBar.updatePadding(top = bars.top)
        view.updatePadding(left = bars.left, right = bars.right, bottom = maxOf(bars.bottom, ime.bottom))
        WindowInsetsCompat.CONSUMED
    }
}
