package com.eferraz.presentation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.eferraz.pokedex.theme.AppTheme
import com.eferraz.presentation.assets.AssetsRoute
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinMultiplatformApplication
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.KoinConfiguration

@Composable
@Preview
public fun InternalApp(config: KoinConfiguration) {

    AppTheme {

        @OptIn(KoinExperimentalAPI::class)
        KoinMultiplatformApplication(config = config) {

            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = HomeRouting) {

                composable<HomeRouting> {
                    AssetsRoute()
                }
            }
        }
    }
}