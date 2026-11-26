package com.sap.codelab.di

import androidx.room.Room
import com.sap.codelab.data.local.AppDatabase
import com.sap.codelab.data.local.DATABASE_NAME
import com.sap.codelab.data.local.MIGRATION_1_2
import com.sap.codelab.data.repository.MemoRepositoryImpl
import com.sap.codelab.domain.repository.MemoRepository
import com.sap.codelab.presentation.create.CreateMemoViewModel
import com.sap.codelab.presentation.detail.ViewMemoViewModel
import com.sap.codelab.domain.location.GeofenceRegistrar
import com.sap.codelab.domain.permission.PermissionChecker
import com.sap.codelab.domain.usecase.MarkMemoDoneUseCase
import com.sap.codelab.domain.usecase.RestoreGeofencesUseCase
import com.sap.codelab.domain.usecase.SaveMemoUseCase
import com.sap.codelab.platform.geofence.PlayServicesGeofenceRegistrar
import com.sap.codelab.platform.map.OsmLocationPickerMap
import com.sap.codelab.platform.permission.AndroidPermissionChecker
import com.sap.codelab.presentation.home.HomeViewModel
import com.sap.codelab.presentation.map.LocationPickerMap
import com.sap.codelab.presentation.picklocation.PickLocationViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
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

internal val domainModule = module {
    factoryOf(::SaveMemoUseCase)
    factoryOf(::MarkMemoDoneUseCase)
    factoryOf(::RestoreGeofencesUseCase)
}

internal val presentationModule = module {
    viewModelOf(::HomeViewModel)
    viewModelOf(::ViewMemoViewModel)
    viewModelOf(::CreateMemoViewModel)
    viewModelOf(::PickLocationViewModel)
}

/**
 * Implementations backed by third-party libraries, bound to our own interfaces.
 */
internal val platformModule = module {
    factoryOf(::OsmLocationPickerMap) { bind<LocationPickerMap>() }
    singleOf(::AndroidPermissionChecker) { bind<PermissionChecker>() }
    singleOf(::PlayServicesGeofenceRegistrar) { bind<GeofenceRegistrar>() }
}

internal val appModules = listOf(dataModule, domainModule, presentationModule, platformModule)
