import com.eferraz.buildlogic.scopes.library
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.foundation.project.library)
    alias(libs.plugins.foundation.library.compose)
    alias(libs.plugins.foundation.library.koin)
    alias(libs.plugins.foundation.library.navigation)
}

library {
    namespace = "com.eferraz.presentation"
}

kotlin {

    compilerOptions {
        freeCompilerArgs.add("-Xreturn-value-checker=check")
        freeCompilerArgs.add("-Xexplicit-backing-fields")
    }

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    dependencies {

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

dependencies {
//    debugImplementation(compose.uiTooling)
}