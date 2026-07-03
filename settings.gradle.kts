rootProject.name = "Cook"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        maven("https://maven.aliyun.com/repository/central")
        maven("https://maven.aliyun.com/repository/google")
        maven("https://maven.aliyun.com/repository/gradle-plugin")
        maven("https://maven.aliyun.com/repository/public")

        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        maven("https://artifacts-caching-proxy.aws.intellij.net/plugins.gradle.org/m2")
        maven("https://cache-redirector.jetbrains.com/plugins.gradle.org/m2")
        maven("https://cache-redirector.jetbrains.com/maven-central")
        maven(url = "https://packages.jetbrains.team/maven/p/jcs/maven")
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {

        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        maven("https://artifacts-caching-proxy.aws.intellij.net/plugins.gradle.org/m2")
        maven("https://cache-redirector.jetbrains.com/plugins.gradle.org/m2")
        maven("https://cache-redirector.jetbrains.com/maven-central")
        maven(url = "https://packages.jetbrains.team/maven/p/jcs/maven")

    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(":androidApp")
include(":desktopApp")
include(":shared")
