import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.foundation.project)
    alias(libs.plugins.foundation.library.comp)
}

kotlin {

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    dependencies {
        implementation(projects.umbrellaApp)
    }
}

kotlin.android {
    namespace = "com.eferraz.umbrella"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
}