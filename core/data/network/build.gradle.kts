import com.eferraz.buildlogic.ext.generateConstants
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.foundation.project)
    alias(libs.plugins.foundation.library.koin)
    alias(libs.plugins.foundation.library.ktor)
}

kotlin {

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    dependencies {
        implementation(projects.domain.entity)
    }
}

generateConstants(
    fileName = "TokenConfig",
    packageName = "com.eferraz.network",
    properties = listOf("BRAPI_TOKEN")
)
