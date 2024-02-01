import com.adobe.marketing.mobile.gradle.BuildConstants

plugins {
    id("com.android.library")
}

android {
    namespace = "com.adobe.marketing.mobile.sample.thirdpartyextension"
    compileSdk = BuildConstants.Versions.COMPILE_SDK_VERSION

    defaultConfig {
        minSdk = BuildConstants.Versions.MIN_SDK_VERSION
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar"))))
    implementation(project(":core"))
}
