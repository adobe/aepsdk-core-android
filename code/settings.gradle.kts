pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        mavenLocal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        mavenLocal()
        maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "aepsdk-core-android"
include (
        ":core",
        ":lifecycle",
        ":identity",
        ":signal",
        ":test-third-party-extension",
        ":integration-tests",
        ":macrobenchmark",
        ":microbenchmark",
        ":testapp",
        )