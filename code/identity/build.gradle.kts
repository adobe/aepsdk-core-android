plugins {
    id("aep-library")
}

val identityExtensionName: String by project
val identityExtensionVersion: String by project
val identityMavenRepoName: String by project
val identityMavenRepoDescription: String by project
val coreExtensionVersion: String by project

aepLibrary {
    namespace = "com.adobe.marketing.mobile.identity"
    moduleName = identityExtensionName
    moduleVersion = identityExtensionVersion
    enableSpotless = true
    enableCheckStyle = true
    publishing {
        mavenRepoName = identityMavenRepoName
        mavenRepoDescription = identityMavenRepoDescription
        gitRepoName = "aepsdk-core-android"
        addCoreDependency("$coreExtensionVersion")
    }
}

dependencies {
    implementation(project(":core"))
}