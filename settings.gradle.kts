pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        flatDir {
            dirs("libs")
        }
        maven {
            url = uri("https://jitpack.io")
        }
    }
}

rootProject.name = "Native_Express_Sdk"
include(":app")
include(":ExpressCheckoutSdk")
