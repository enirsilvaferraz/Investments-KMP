import com.eferraz.buildlogic.scopes.application
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.foundation.project.application)
    alias(libs.plugins.foundation.library.compose)
    alias(libs.plugins.foundation.library.koin)
    alias(libs.plugins.foundation.library.navigation)
}

application {
    namespace = "com.eferraz.investments"
    versionCode = 1
    versionName = "1.0.0"
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

        implementation(libs.coil.compose)
        implementation(libs.coil.network.ktor3)

        implementation(libs.paging.common)
        implementation(libs.paging.compose.common)

        implementation(projects.composeApp)
        implementation(projects.entity)
        implementation(projects.usecases)
        implementation(projects.repositories)
        implementation(projects.network)
        implementation(projects.database)
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}
