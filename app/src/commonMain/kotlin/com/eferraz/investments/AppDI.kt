package com.eferraz.investments

import com.eferraz.database.di.DatabaseModule
import com.eferraz.entities.di.EntityModule
import com.eferraz.network.di.NetworkModule
import com.eferraz.presentation.di.PresentationModule
import com.eferraz.repositories.di.RepositoryModule
import com.eferraz.usecases.di.UseCaseModule
import org.koin.dsl.KoinConfiguration
import org.koin.ksp.generated.module

internal object AppDI {

    operator fun invoke() = KoinConfiguration {

        modules(
            DatabaseModule().module,
            NetworkModule().module,
            EntityModule().module,
            UseCaseModule().module,
            RepositoryModule().module,
            PresentationModule().module
        )

        printLogger()
    }
}