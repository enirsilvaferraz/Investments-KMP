package com.eferraz.presentation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Serializable
internal object HistoryRouting : NavKey

@Serializable
internal object AssetsRouting : NavKey

@Serializable
internal object FixedIncomeAssetRouting

@Serializable
internal object VariableIncomeAssetRouting

@Serializable
internal object FundsAssetRouting


// Creates the required serializing configuration for open polymorphism
internal val config = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(AssetsRouting::class, AssetsRouting.serializer())
            subclass(HistoryRouting::class, HistoryRouting.serializer())
        }
    }
}