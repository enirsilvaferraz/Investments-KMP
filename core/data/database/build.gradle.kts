import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.foundation.project)
    alias(libs.plugins.foundation.library.room)
    alias(libs.plugins.foundation.library.koin)
}

kotlin {

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    dependencies {
        implementation(projects.domain.entity)
    }

    sourceSets {
        jvmTest.dependencies {
            implementation("androidx.room3:room3-testing:3.0.0-alpha05")
        }
    }
}