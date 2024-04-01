buildscript {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        mavenLocal()
    }

    dependencies {
        classpath("com.github.adobe:aepsdk-commons:gp-3.0.0")
        classpath("org.jetbrains.kotlinx:binary-compatibility-validator:0.13.2")
        classpath("androidx.benchmark:benchmark-gradle-plugin:1.2.3")
    }
}


