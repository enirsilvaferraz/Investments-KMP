package com.eferraz.repositories.di

import com.eferraz.database.di.DatabaseModule
import com.eferraz.network.di.NetworkModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module(
    includes = [
        DatabaseModule::class,
        NetworkModule::class
    ]
)
@ComponentScan("com.eferraz.repositories")
public class RepositoryModule
