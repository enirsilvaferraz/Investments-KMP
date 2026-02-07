rootProject.name = "Investments"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {

    includeBuild("build-logic")

    @Suppress("UnstableApiUsage")
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }

    versionCatalogs {
        create("libs") {
            from(files("build-logic/gradle/libs.versions.toml"))
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(":umbrellaApp")
project(":umbrellaApp").projectDir = File(settingsDir, "core/apps/umbrellaApp")

include(":androidApp")
project(":androidApp").projectDir = File(settingsDir, "core/apps/androidApp")

include(":desktopApp")
project(":desktopApp").projectDir = File(settingsDir, "core/apps/desktopApp")

include(":composeApp")
project(":composeApp").projectDir = File(settingsDir, "core/presentation/composeApp")

include(":design-system")
project(":design-system").projectDir = File(settingsDir, "core/presentation/design-system")

include(":entity")
project(":entity").projectDir = File(settingsDir, "core/domain/entity")

include(":usecases")
project(":usecases").projectDir = File(settingsDir, "core/domain/usecases")

include(":repositories")
project(":repositories").projectDir = File(settingsDir, "core/data/repositories")

include(":network")
project(":network").projectDir = File(settingsDir, "core/data/network")

include(":database")
project(":database").projectDir = File(settingsDir, "core/data/database")