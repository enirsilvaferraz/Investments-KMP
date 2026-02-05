package com.eferraz.presentation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Serializable
internal object TestRouting : NavKey

@Serializable
internal object HistoryRouting : NavKey

@Serializable
internal object HistoryRoutingV2 : NavKey

@Serializable
internal object AssetsRouting : NavKey

@Serializable
internal object FixedIncomeAssetRouting : NavKey

@Serializable
internal object VariableIncomeAssetRouting : NavKey

@Serializable
internal object FundsAssetRouting : NavKey

@Serializable
internal object FixedIncomeHistoryRouting : NavKey

@Serializable
internal object VariableIncomeHistoryRouting : NavKey

@Serializable
internal object FundsHistoryRouting : NavKey

@Serializable
internal object GoalsMonitoringRouting : NavKey

// Creates the required serializing configuration for open polymorphism
internal val config = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(TestRouting::class, TestRouting.serializer())
            subclass(AssetsRouting::class, AssetsRouting.serializer())
            subclass(HistoryRouting::class, HistoryRouting.serializer())
            subclass(HistoryRoutingV2::class, HistoryRoutingV2.serializer())
            subclass(FixedIncomeAssetRouting::class, FixedIncomeAssetRouting.serializer())
            subclass(VariableIncomeAssetRouting::class, VariableIncomeAssetRouting.serializer())
            subclass(FundsAssetRouting::class, FundsAssetRouting.serializer())
            subclass(FixedIncomeHistoryRouting::class, FixedIncomeHistoryRouting.serializer())
            subclass(VariableIncomeHistoryRouting::class, VariableIncomeHistoryRouting.serializer())
            subclass(FundsHistoryRouting::class, FundsHistoryRouting.serializer())
            subclass(GoalsMonitoringRouting::class, GoalsMonitoringRouting.serializer())
        }
    }
}