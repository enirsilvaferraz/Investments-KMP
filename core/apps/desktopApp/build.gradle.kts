import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.compose.compiler)
//    alias(libs.plugins.composeHotReload)
    kotlin("jvm")
    alias(libs.plugins.compose.multiplatform)
}

dependencies {
    implementation(project(":umbrellaApp"))
    implementation(compose.desktop.currentOs)
//    implementation(libs.kotlinx.coroutinesSwing)
}

compose.desktop {
    application {
        mainClass = "com.eferraz.investments.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.eferraz.investments"
            packageVersion = "1.0.0"
        }
    }
}

