package com.eferraz.presentation.di

import com.eferraz.usecases.di.UseCaseModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module (
    includes = [
        UseCaseModule::class
    ]
)
@ComponentScan("com.eferraz.presentation")
public class PresentationModule