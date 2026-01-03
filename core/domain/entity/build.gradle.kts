plugins {
    alias(libs.plugins.foundation.project.library)
    alias(libs.plugins.foundation.library.koin)
}

kotlin {

    sourceSets {
        androidUnitTest.dependencies {
            implementation(libs.mockk.android)
            implementation(libs.mockk.agent)
        }
    }
}

kotlin.android {
    namespace = "com.eferraz.entities"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
}