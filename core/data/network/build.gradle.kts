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
            kotlin.srcDir(layout.buildDirectory.dir("generated/kotlin"))
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
val generateTokenConfig = tasks.register("generateTokenConfig") {

    val outputFile = layout.buildDirectory.file("generated/kotlin/com/eferraz/network/TokenConfig.kt")
    
    inputs.property("brApiToken", brApiToken)
    outputs.file(outputFile)
    
    doLast {
        val token = inputs.properties["brApiToken"] as? String ?: ""
        val file = outputFile.get().asFile
        file.parentFile.mkdirs()
        file.writeText("""
            package com.eferraz.network
            
            internal object TokenConfig {
                const val BRAPI_TOKEN = "$token"
            }
        """.trimIndent())
    }
}

// Configura para executar antes de todas as compilações do Kotlin
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    dependsOn(generateTokenConfig)
}

// Também configura para tasks de metadata (compatibilidade)
tasks.matching { 
    (it.name.contains("compile") || it.name.startsWith("ksp")) &&
    (it.name.contains("Kotlin") || it.name.contains("Metadata"))
}.configureEach {
    dependsOn(generateTokenConfig)
}
