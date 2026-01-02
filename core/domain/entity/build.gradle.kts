import com.eferraz.buildlogic.scopes.library
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.foundation.project.library)
    alias(libs.plugins.foundation.library.koin)
}

library {
    namespace = "com.eferraz.entities"
}

kotlin {

    sourceSets {
        androidUnitTest.dependencies {
            implementation(libs.mockk.android)
            implementation(libs.mockk.agent)
        }
    }
}

kotlin.android {
    namespace = "com.eferraz.entities"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
}