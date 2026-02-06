import org.gradle.kotlin.dsl.invoke
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.foundation.project.library)
    alias(libs.plugins.foundation.library.compose)
    alias(libs.plugins.foundation.library.koin)
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

        implementation(libs.datatable.material3)
    }
}

kotlin.android {
    namespace = "com.eferraz.presentation"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
}