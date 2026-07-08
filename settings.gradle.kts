pluginManagement {
    includeBuild("build-logic")
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
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "eventsnap"

include(":app-mobile")

include(":core:common")
include(":core:model")
include(":core:data")
include(":core:designsystem")
include(":core:ui-mobile")
include(":core:testing")

include(":feature:capture:data")
include(":feature:capture:ui-mobile")
include(":feature:review:data")
include(":feature:review:ui-mobile")
include(":feature:settings:data")
include(":feature:settings:ui-mobile")
