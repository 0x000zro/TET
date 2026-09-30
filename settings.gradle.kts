pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "LearningBlueprint"

include(":apps:student")
include(":apps:admin")

include(":core:common")
include(":core:model")
include(":core:designsystem")
include(":core:theme")
