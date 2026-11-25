package com.sap.codelab

import android.app.Application
import com.sap.codelab.data.repository.MemoRepositoryImpl

/**
 * Extension of the Android Application class.
 */
internal class App : Application() {
    override fun onCreate() {
        super.onCreate()
        MemoRepositoryImpl.initialize(this)
    }
}