import com.eferraz.buildlogic.scopes.application
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.foundation.project.application)
    alias(libs.plugins.foundation.library.compose)
    alias(libs.plugins.foundation.library.koin)
}

application {
    namespace = "com.eferraz.investments"
    versionCode = 1
    versionName = "1.0.0"
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

dependencies {
    debugImplementation(compose.uiTooling)
}
