plugins {
    id("aep-library")
}

val lifecycleExtensionName: String by project
val lifecycleExtensionVersion: String by project
val lifecycleMavenRepoName: String by project
val lifecycleMavenRepoDescription: String by project
val coreExtensionVersion: String by project

aepLibrary {
    namespace = "com.adobe.marketing.mobile.lifecycle"
    moduleName = lifecycleExtensionName
    moduleVersion = lifecycleExtensionVersion
    enableSpotless = true
    enableCheckStyle = true
    publishing {
        mavenRepoName = lifecycleMavenRepoName
        mavenRepoDescription = lifecycleMavenRepoDescription
        gitRepoName = "aepsdk-core-android"
        addCoreDependency("$coreExtensionVersion")
    }
}

dependencies {
    implementation(project(":core"))
}