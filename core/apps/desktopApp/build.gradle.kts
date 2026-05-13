import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JvmVendorSpec
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
    kotlin("jvm")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xskip-prerelease-check")
    }
}

dependencies {
    implementation(projects.apps.umbrellaApp)
    implementation(compose.desktop.currentOs)
}

val packagingJdk = javaToolchains.launcherFor {
    languageVersion.set(JavaLanguageVersion.of(21))
    vendor.set(JvmVendorSpec.ADOPTIUM)
}

compose.desktop {
    application {
        mainClass = "com.eferraz.investments.MainKt"

        nativeDistributions {
            // Native packaging needs a full JDK with jpackage instead of Android Studio's bundled JBR.
            javaHome = packagingJdk.get().metadata.installationPath.asFile.absolutePath
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.eferraz.investments"
            packageVersion = "1.0.0"
            macOS {
                bundleID = "com.eferraz.investments"
            }
        }
    }
}

