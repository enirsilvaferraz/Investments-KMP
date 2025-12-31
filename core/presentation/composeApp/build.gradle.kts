import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
//    alias(libs.plugins.compose.HotReload)
    alias(libs.plugins.foundation.library.koin)
    alias(libs.plugins.foundation.library.navigation)
    alias(libs.plugins.foundation.library.compose)
}

kotlin {

    compilerOptions {
        freeCompilerArgs.add("-Xreturn-value-checker=check")
        freeCompilerArgs.add("-Xexplicit-backing-fields")
    }

    jvm("desktop")

    android {
        compileSdk = libs.versions.android.compileSdk.get().toInt()
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    dependencies {

        implementation("org.jetbrains.compose.runtime:runtime:1.11.0-alpha01")
        implementation("org.jetbrains.compose.foundation:foundation:1.11.0-alpha01")
        implementation("org.jetbrains.compose.material3:material3:1.9.0")
        implementation("org.jetbrains.compose.ui:ui:1.11.0-alpha01")
        implementation("org.jetbrains.compose.components:components-resources:1.11.0-alpha01")
        implementation("org.jetbrains.compose.ui:ui-tooling-preview:1.11.0-alpha01")
//        implementation(libs.androidx.lifecycle.viewmodelCompose)
//        implementation(libs.androidx.lifecycle.runtimeCompose)

        implementation(libs.androidx.lifecycle.viewmodel)
        implementation(libs.androidx.lifecycle.runtimeCompose)

        implementation(projects.entity)
        implementation(projects.usecases)

        implementation("org.jetbrains.compose.material3.adaptive:adaptive:1.2.0")
        implementation("org.jetbrains.compose.material3.adaptive:adaptive-layout:1.2.0")
        implementation("org.jetbrains.compose.material3.adaptive:adaptive-navigation:1.2.0")

        implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")
    }

    sourceSets {
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

kotlin.android {
    namespace = "com.eferraz.presentation"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
}