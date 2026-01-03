import com.eferraz.buildlogic.ext.configureTokenConfigGeneration
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.foundation.project.library)
    alias(libs.plugins.foundation.library.koin)
    alias(libs.plugins.foundation.library.ktor)
}

kotlin {

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    dependencies {
        implementation(projects.entity)
    }
}

kotlin.android {
    namespace = "com.eferraz.network"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
}

configureTokenConfigGeneration(
    fileName = "TokenConfig",
    packageName = "com.eferraz.network",
    properties = listOf("BRAPI_TOKEN")
)
