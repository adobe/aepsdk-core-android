import com.adobe.marketing.mobile.gradle.BuildConstants

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.adobe.marketing.mobile.integration"
    compileSdk = BuildConstants.Versions.COMPILE_SDK_VERSION

    defaultConfig {
        minSdk = BuildConstants.Versions.MIN_SDK_VERSION
        consumerProguardFiles("consumer-rules.pro")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // The following argument makes the Android Test Orchestrator run its
        // "pm clear" command after each test invocation. This command ensures
        // that the app's state is completely cleared between tests.
        testInstrumentationRunnerArguments["clearPackageData"] = "true"
    }

    buildTypes {
        getByName(BuildConstants.BuildTypes.RELEASE) {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }


    compileOptions {
        sourceCompatibility = BuildConstants.Versions.JAVA_SOURCE_COMPATIBILITY
        targetCompatibility = BuildConstants.Versions.JAVA_TARGET_COMPATIBILITY
    }

    kotlinOptions {
        jvmTarget = BuildConstants.Versions.KOTLIN_JVM_TARGET
        languageVersion = BuildConstants.Versions.KOTLIN_LANGUAGE_VERSION
        apiVersion = BuildConstants.Versions.KOTLIN_API_VERSION
    }

    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":signal"))
    implementation(project(":identity"))
    implementation(project(":lifecycle"))

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar"))))
    implementation(BuildConstants.Dependencies.ANDROIDX_CORE_KTX)
    implementation(BuildConstants.Dependencies.ANDROIDX_APPCOMPAT)

    testImplementation(BuildConstants.Dependencies.JUNIT)
    androidTestImplementation(BuildConstants.Dependencies.ANDROIDX_TEST_EXT_JUNIT)
    androidTestImplementation(BuildConstants.Dependencies.ESPRESSO_CORE)
    androidTestImplementation(BuildConstants.Dependencies.ANDROIDX_TEST_RUNNER)

    androidTestUtil(BuildConstants.Dependencies.ANDROIDX_TEST_ORCHESTRATOR)

}