import com.eferraz.buildlogic.ext.generateRuntimeConfigFromLocalProperties

plugins {
    alias(libs.plugins.foundation.project)
}

generateRuntimeConfigFromLocalProperties()