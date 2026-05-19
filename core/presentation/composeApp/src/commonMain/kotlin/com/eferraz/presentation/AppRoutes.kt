package com.eferraz.presentation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import com.eferraz.asset_management.di.AssetManagementRouting
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Serializable
internal object HistoryRouting : NavKey

// Creates the required serializing configuration for open polymorphism
internal val config = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(HistoryRouting::class, HistoryRouting.serializer())
            subclass(AssetManagementRouting::class, AssetManagementRouting.serializer())
        }
    }
}
