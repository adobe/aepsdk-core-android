import com.adobe.marketing.mobile.gradle.BuildConstants

plugins {
    id("aep-library")
    id("binary-compatibility-validator")
}

val coreExtensionName: String by project
val coreExtensionVersion: String by project
val coreMavenRepoName: String by project
val coreMavenRepoDescription: String by project

// Move to aep-library plugin
val lifecycleProcessVersion = "2.0.0"

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
        addMavenDependency("androidx.lifecycle", "lifecycle-runtime-ktx", BuildConstants.Versions.ANDROIDX_LIFECYCLE_KTX)
        addMavenDependency("androidx.lifecycle", "lifecycle-process", lifecycleProcessVersion)
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

dependencies {
    implementation(BuildConstants.Dependencies.ANDROIDX_LIFECYCLE_KTX)
    implementation("androidx.lifecycle:lifecycle-process:$lifecycleProcessVersion")

    androidTestImplementation(BuildConstants.Dependencies.MOCKITO_CORE)
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.3.0")
    //TODO: Consider moving this to the aep-library plugin later
    androidTestImplementation("com.linkedin.dexmaker:dexmaker-mockito-inline:2.28.3")
    androidTestImplementation(project(":testutils"))
    testImplementation("org.robolectric:robolectric:4.7")
    testImplementation(project(":testutils"))
}
