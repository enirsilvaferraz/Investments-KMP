import com.eferraz.buildlogic.scopes.library
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.foundation.project.library)
    alias(libs.plugins.foundation.library.koin)
}

library {
    namespace = "com.eferraz.entities"
}

kotlin {

    sourceSets {

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }

        androidUnitTest.dependencies {
            val mockkVersion = "1.14.7"
            implementation("io.mockk:mockk-android:${mockkVersion}")
            implementation("io.mockk:mockk-agent:${mockkVersion}")
        }
    }
}

kotlin.android {
    namespace = "com.eferraz.entities"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
}