package com.eferraz.pokedex.di

import com.eferraz.pokedex.di.ComposeModule
import org.koin.dsl.KoinConfiguration
import org.koin.ksp.generated.module

internal object AppDI {

    operator fun invoke() = KoinConfiguration {

        modules(
//            DatabaseModule().module,
//            NetworkModule().module,
//            EntityModule().module,
//            UseCaseModule().module,
//            RepositoryModule().module,
            ComposeModule().module
        )

        printLogger()
    }
}