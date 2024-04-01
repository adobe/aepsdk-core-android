plugins {
    id("aep-library")
}

val signalExtensionName: String by project
val signalExtensionVersion: String by project
val signalMavenRepoName: String by project
val signalMavenRepoDescription: String by project
val coreExtensionVersion: String by project

aepLibrary {
    namespace = "com.adobe.marketing.mobile.signal"
    moduleName = signalExtensionName
    moduleVersion = signalExtensionVersion
    enableDokkaDoc = false
    enableSpotless = true
    enableCheckStyle = true
    publishing {
        mavenRepoName = signalMavenRepoName
        mavenRepoDescription = signalMavenRepoDescription
        gitRepoName = "aepsdk-core-android"
        addCoreDependency("$coreExtensionVersion")
    }
}

dependencies {
    implementation(project(":core"))
}