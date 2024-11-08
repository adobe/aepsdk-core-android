/**
 * Copyright 2023 Adobe. All rights reserved.
 * This file is licensed to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy
 * of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
 * OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

import com.adobe.marketing.mobile.gradle.BuildConstants

plugins {
    id("aep-library")
    id("binary-compatibility-validator")
}

val coreExtensionVersion: String by project
val jacksonVersion = "2.12.7"

aepLibrary {
    namespace = "com.adobe.marketing.mobile.testutils"
    moduleName = "testutils"
    moduleVersion = "3.0.0"
    enableSpotless = true
    enableSpotlessPrettierForJava = true
    enableDokkaDoc = true

    publishing {
        mavenRepoName = "AdobeMobileTestUtilsSdk"
        mavenRepoDescription = "Android Test Utils for Adobe Mobile Marketing"
        gitRepoName = "aepsdk-testutils-android"
        addCoreDependency(coreExtensionVersion)
        addMavenDependency("androidx.test.ext", "junit", BuildConstants.Versions.ANDROIDX_TEST_EXT_JUNIT)
        addMavenDependency("com.fasterxml.jackson.core", "jackson-databind", jacksonVersion)
    }
}

dependencies {
    implementation(project(":core"))
    implementation(BuildConstants.Dependencies.ANDROIDX_TEST_EXT_JUNIT)
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
}