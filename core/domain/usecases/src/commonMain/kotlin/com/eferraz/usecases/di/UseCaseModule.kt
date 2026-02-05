package com.eferraz.usecases.di

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan("com.eferraz.usecases")
public class UseCaseModule {
    
    @Single
    public fun provideCoroutineDispatcher(): CoroutineDispatcher = Dispatchers.Default
}