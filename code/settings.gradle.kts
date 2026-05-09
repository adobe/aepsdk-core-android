pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        mavenLocal()
    }
}

includeBuild("../../aepsdk-commons/android/aepsdk-gradle-plugin") {
    dependencySubstitution {
        substitute(module("com.github.adobe.aepsdk-commons:aepsdk-gradle-plugin"))
            .using(project(":aepsdk-gradle-plugin"))
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        mavenLocal()
        maven { url = uri("https://central.sonatype.com/repository/maven-snapshots/") }
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
        ":testutils"
        )
