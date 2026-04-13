package com.eferraz.asset_management.di

import com.eferraz.usecases.di.UseCaseModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module([UseCaseModule::class])
@ComponentScan("com.eferraz.asset_management")
public class AssetManagementModule
