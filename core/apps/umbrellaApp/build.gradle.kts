import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.foundation.project)
    alias(libs.plugins.foundation.library.comp)
    alias(libs.plugins.foundation.library.koin)
}

kotlin {

    compilerOptions {
        freeCompilerArgs.add("-Xskip-prerelease-check")
    }

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    dependencies {
        implementation(projects.composeApp)
        implementation(projects.entity)
        implementation(projects.usecases)
        implementation(projects.repositories)
        implementation(projects.network)
        implementation(projects.database)
    }
}

kotlin.android {
    namespace = "com.eferraz.umbrella"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
}