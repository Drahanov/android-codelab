package com.sap.codelab.di

import org.junit.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.test.verify.verify

/**
 * Verifies at test time that every dependency in the Koin graph can be resolved,
 * so a missing definition fails the build instead of crashing the app at runtime.
 */
@OptIn(KoinExperimentalAPI::class)
class AppModuleTest {

    @Test
    fun `koin graph is complete`() {
        module { includes(appModules) }.verify()
    }
}
