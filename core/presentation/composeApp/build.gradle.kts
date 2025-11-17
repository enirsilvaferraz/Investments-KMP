import com.eferraz.buildlogic.scopes.library
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.foundation.project.library)
    alias(libs.plugins.foundation.library.compose)
    alias(libs.plugins.foundation.library.koin)
    alias(libs.plugins.foundation.library.navigation)
}

library {
    namespace = "com.eferraz.pokedex"
}

kotlin {

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    dependencies {

        implementation(libs.androidx.lifecycle.viewmodel)
        implementation(libs.androidx.lifecycle.runtimeCompose)

//        implementation(libs.coil.compose)
//        implementation(libs.coil.network.ktor3)
//
//        implementation(libs.paging.common)
//        implementation(libs.paging.compose.common)

        implementation(projects.entity)
        implementation(projects.usecases)
//        implementation(projects.repositories)
//        implementation(projects.network)
//        implementation(projects.database)

//        implementation("org.jetbrains.compose.material3.adaptive:adaptive:1.1.2")
        implementation("org.jetbrains.compose.material3.adaptive:adaptive-layout:1.2.0")
        implementation("org.jetbrains.compose.material3.adaptive:adaptive-navigation:1.2.0")

//        implementation(this@kotlin.compose.material3AdaptiveNavigationSuite)
//        implementation("org.jetbrains.compose.material3:material3:1.9.0")
//        implementation("org.jetbrains.compose.material3:material3-adaptive-navigation-suite:1.9.0")
//        implementation("org.jetbrains.compose.material3:material3-window-size-class:1.9.0")

        implementation("org.jetbrains.compose.material3:material3:1.9.0")

        implementation("org.jetbrains.androidx.navigation:navigation-compose:2.9.1")
//        implementation("org.jetbrains.compose.material:material-navigation:1.9.1")

        implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}