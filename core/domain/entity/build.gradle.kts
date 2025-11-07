import com.eferraz.buildlogic.scopes.library

plugins {
    alias(libs.plugins.foundation.project.library)
    alias(libs.plugins.foundation.library.koin)
}

library {
    namespace = "com.eferraz.entities"
}