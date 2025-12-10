import com.eferraz.buildlogic.scopes.library
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import java.util.Properties

plugins {
    alias(libs.plugins.foundation.project.library)
    alias(libs.plugins.foundation.library.koin)
    alias(libs.plugins.foundation.library.ktor)
}

library {
    namespace = "com.eferraz.network"
}

kotlin {

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    dependencies {
        implementation(projects.entity)
    }

    sourceSets {
        val commonMain by getting {
            kotlin.srcDir("build/generated/kotlin")
        }
    }
}


// Lê o local.properties
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

// Obtém o token do local.properties (assumindo que a propriedade se chama "BRAPI_TOKEN")
val brApiToken = localProperties.getProperty("BRAPI_TOKEN", "")


// Gera um arquivo Kotlin com o token para uso em commonMain
tasks.register("generateTokenConfig") {
    val outputDir = file("build/generated/kotlin")
    val outputFile = file("$outputDir/com/eferraz/network/TokenConfig.kt")

    doFirst {
        outputDir.mkdirs()
        outputFile.parentFile.mkdirs()
        outputFile.writeText("""
            package com.eferraz.network
            
            internal object TokenConfig {
                const val BRAPI_TOKEN = "$brApiToken"
            }
        """.trimIndent())
    }
}

tasks.named("compileKotlinMetadata") {
    dependsOn("generateTokenConfig")
}