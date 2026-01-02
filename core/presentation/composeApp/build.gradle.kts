import org.gradle.kotlin.dsl.invoke
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
//    alias(libs.plugins.compose.HotReload)
    alias(libs.plugins.foundation.project.library)
    alias(libs.plugins.foundation.library.koin)
    alias(libs.plugins.foundation.library.compose)
}

kotlin {

    compilerOptions {
        freeCompilerArgs.add("-Xreturn-value-checker=check")
        freeCompilerArgs.add("-Xexplicit-backing-fields")
    }

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    dependencies {
        implementation(projects.entity)
        implementation(projects.usecases)

        implementation(libs.androidx.lifecycle.viewmodel)
        implementation(libs.androidx.lifecycle.runtimeCompose)
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.bundles.compose.common)
            implementation(libs.bundles.compose.adaptive.bundle)
        }
    }
}

kotlin.android {
    namespace = "com.eferraz.presentation"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
}

dependencies {
    "androidRuntimeClasspath"(libs.androidx.ui.tooling)
}