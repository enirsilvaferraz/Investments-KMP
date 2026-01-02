import com.eferraz.buildlogic.scopes.library
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.foundation.project.library)
    alias(libs.plugins.foundation.library.koin)
}

library {
    namespace = "com.eferraz.usecases"
}

kotlin {

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    dependencies {

        api(projects.entity)
    }

    sourceSets {

//        commonTest.dependencies {
//            implementation(libs.kotlin.test)
//            implementation(libs.kotlinx.coroutines.test)
//        }

        androidUnitTest.dependencies {
            implementation(libs.mockk.android)
            implementation(libs.mockk.agent)
        }
    }
}

kotlin.android {
    namespace = "com.eferraz.usecases"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
}