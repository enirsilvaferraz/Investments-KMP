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

//        implementation(projects.entity)
//        implementation(projects.usecases)
//        implementation(projects.repositories)
//        implementation(projects.network)
//        implementation(projects.database)
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}