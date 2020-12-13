pluginManagement {
    repositories {
        gradlePluginPortal()
        jcenter()
    }
}

plugins {
    id("com.gradle.enterprise") version "3.3.1"
}

rootProject.name = "bowler-proto-kotlin"

include(":proto")

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
    }
}
