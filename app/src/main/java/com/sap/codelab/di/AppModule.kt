package com.sap.codelab.di

import androidx.room.Room
import com.sap.codelab.data.local.AppDatabase
import com.sap.codelab.data.local.DATABASE_NAME
import com.sap.codelab.data.local.MIGRATION_1_2
import com.sap.codelab.data.repository.MemoRepositoryImpl
import com.sap.codelab.domain.repository.MemoRepository
import com.sap.codelab.presentation.create.CreateMemoViewModel
import com.sap.codelab.presentation.detail.ViewMemoViewModel
import com.sap.codelab.presentation.home.HomeViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

internal val dataModule = module {
    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, DATABASE_NAME)
            .addMigrations(MIGRATION_1_2)
            .build()
    }
    single { get<AppDatabase>().getMemoDao() }
    singleOf(::MemoRepositoryImpl) { bind<MemoRepository>() }
}

internal val presentationModule = module {
    viewModelOf(::HomeViewModel)
    viewModelOf(::ViewMemoViewModel)
    viewModelOf(::CreateMemoViewModel)
}

internal val appModules = listOf(dataModule, presentationModule)
