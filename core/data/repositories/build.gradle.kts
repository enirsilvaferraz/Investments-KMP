import com.eferraz.buildlogic.scopes.library
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.foundation.project.library)
    alias(libs.plugins.foundation.library.koin)
}

library {
    namespace = "com.eferraz.repositories"
}

kotlin {

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    dependencies {
        implementation(projects.entity)
        implementation(projects.usecases)
        implementation(projects.database)
        implementation(projects.network)
    }
}

kotlin.android {
    namespace = "com.eferraz.repositories"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
}