import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.foundation.project)
    alias(libs.plugins.foundation.library.koin)
}

kotlin {

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    dependencies {
        api(projects.entity)
    }
}

kotlin.android {
    namespace = "com.eferraz.usecases"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
}