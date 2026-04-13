import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.foundation.project)
    alias(libs.plugins.foundation.library.comp)
    alias(libs.plugins.foundation.library.koin)
}

kotlin {

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    dependencies {
        implementation(projects.features.assetManagement)
        implementation(projects.features.composeApp)
        implementation(projects.domain.entity)
        implementation(projects.domain.usecases)
        implementation(projects.data.repositories)
        implementation(projects.data.network)
        implementation(projects.data.database)
    }
}