import com.eferraz.buildlogic.ext.generateRuntimeConfigFromLocalProperties

plugins {
    alias(libs.plugins.foundation.project)
    alias(libs.plugins.foundation.library.koin)
}

generateRuntimeConfigFromLocalProperties()