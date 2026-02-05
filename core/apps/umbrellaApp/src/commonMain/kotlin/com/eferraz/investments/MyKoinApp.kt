package com.eferraz.investments

import com.eferraz.database.di.DatabaseModule
import com.eferraz.entities.di.EntityModule
import com.eferraz.network.di.NetworkModule
import com.eferraz.presentation.di.PresentationModule
import com.eferraz.repositories.di.RepositoryModule
import com.eferraz.usecases.di.UseCaseModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.KoinApplication

@KoinApplication(
    modules = [
        DatabaseModule::class,
        NetworkModule::class,
        EntityModule::class,
        UseCaseModule::class,
        RepositoryModule::class,
        PresentationModule::class
    ]
)
@ComponentScan("com.eferraz.investments")
internal class MyKoinApp