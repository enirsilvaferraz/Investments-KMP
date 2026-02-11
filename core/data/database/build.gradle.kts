import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.foundation.project)
    alias(libs.plugins.foundation.library.room)
    alias(libs.plugins.foundation.library.koin)
}

kotlin {

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    dependencies {
        implementation(projects.entity)
    }
}

kotlin.android {
    namespace = "com.eferraz.database"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
}