package com.eferraz.pokedex

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.eferraz.pokedex.di.AppDI
import com.eferraz.pokedex.ui.HomeRoute
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinMultiplatformApplication
import org.koin.core.annotation.KoinExperimentalAPI

@Composable
@Preview
public fun InternalApp() {

    MaterialTheme {

        @OptIn(KoinExperimentalAPI::class)
        KoinMultiplatformApplication(config = AppDI()) {

            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = HomeRouting) {

                composable<HomeRouting> {
                    HomeRoute()
//                    {
//                        navController.navigate(PokemonDetailRouting(it.id))
//                    }
                }
//
//                composable<PokemonDetailRouting> {
//                    PokemonRoute(id = it.toRoute<PokemonDetailRouting>().id) {
//                        navController.popBackStack()
//                    }
//                }
            }
        }
    }
}