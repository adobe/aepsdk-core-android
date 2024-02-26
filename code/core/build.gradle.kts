import com.adobe.marketing.mobile.gradle.BuildConstants

plugins {
    id("aep-library")
    id("binary-compatibility-validator")
}

val coreExtensionName: String by project
val coreExtensionVersion: String by project
val coreMavenRepoName: String by project
val coreMavenRepoDescription: String by project

aepLibrary {
    namespace = "com.adobe.marketing.mobile.core"
    moduleName = coreExtensionName
    moduleVersion = coreExtensionVersion
    compose = true
    enableDokkaDoc = true
    enableSpotless = true
    enableCheckStyle = true
    publishing {
        mavenRepoName = coreMavenRepoName
        mavenRepoDescription = coreMavenRepoDescription
        gitRepoName = "aepsdk-core-android"

        addMavenDependency("org.jetbrains.kotlin", "kotlin-stdlib-jdk8", BuildConstants.Versions.KOTLIN)
        addMavenDependency("androidx.appcompat", "appcompat", BuildConstants.Versions.ANDROIDX_APPCOMPAT)
        addMavenDependency("androidx.compose.runtime", "runtime", BuildConstants.Versions.COMPOSE)
        addMavenDependency("androidx.compose.material", "material", BuildConstants.Versions.COMPOSE_MATERIAL)
        addMavenDependency("androidx.compose.animation", "animation", BuildConstants.Versions.COMPOSE)
        addMavenDependency("androidx.activity", "activity-compose", BuildConstants.Versions.ANDROIDX_ACTIVITY_COMPOSE)
    }
}

apiValidation {
    ignoredPackages.addAll(setOf(
            "com.adobe.marketing.mobile.internal",
            "com.adobe.marketing.mobile.services.internal",
            "com.adobe.marketing.mobile.services.ui.internal"
    ))

    ignoredClasses.addAll(setOf(
            "com.adobe.marketing.mobile.core.BuildConfig"
    ))
}

//TODO: Consider moving this to the aep-library plugin later
dependencies {
    androidTestImplementation("org.mockito:mockito-core:5.3.1")
    androidTestImplementation("com.linkedin.dexmaker:dexmaker-mockito-inline:2.28.3")
}
