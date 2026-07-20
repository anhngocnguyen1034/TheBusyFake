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
        // anhnn-components-ads (module quảng cáo) được publish qua JitPack.
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "The Busy Simulator"
include(":app")
include(":language")
