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
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        flatDir {
            dirs("libs")
        }
        maven {
            url = uri("https://jitpack.io")
            credentials.username = providers.gradleProperty("authToken").get()
        }
    }
}

rootProject.name = "Native_Express_Sdk"
include(":app")
include(":ExpressCheckoutSdk")
