buildscript {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        mavenLocal()
    }


    dependencies {
        // Todo: Move away from specifying commit once we release a stable version."
        classpath("com.github.adobe:aepsdk-commons:9deb721db7")
        classpath("org.jetbrains.kotlinx:binary-compatibility-validator:0.13.2")
        classpath("androidx.benchmark:benchmark-gradle-plugin:1.2.3")
    }
}


