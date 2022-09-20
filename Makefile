BRANCH_VERSION=$(shell git rev-parse --abbrev-ref HEAD | sed "s/dev-v//g")
# BRANCH_VERSION=1.10.0
update-version:
	echo $(BRANCH_VERSION)
	sed -i '' "s/[0-9]*\.[0-9]*\.[0-9]/$(BRANCH_VERSION)/g" ./android-core-library/src/phone/java/com/adobe/marketing/mobile/ExtensionVersionManager.java
	sed -i '' "s/\(coreExtensionVersion=\)[0-9]*\.[0-9]*\.[0-9]/\1$(BRANCH_VERSION)/g" ./gradle.properties
	sed -i '' "s/\(mavenCoreVersion=\)[0-9]*\.[0-9]*\.[0-9]/\1$(BRANCH_VERSION)/g" ./gradle.properties
	sed -i '' "s/\(coreLibraryMavenRootVersion=\)[0-9]*\.[0-9]*\.[0-9]/\1$(BRANCH_VERSION)/g" ./gradle.properties

setup:
	  (mkdir -p ci)	

clean:
	  (rm -rf ci)
	  (./code/gradlew -p code clean)

checkstyle:
		(./code/gradlew -p code/android-core-library checkstyle)

check-format:
		(./code/gradlew -p code/android-core-library ktlintCheck)

format:
		(./code/gradlew -p code/android-core-library ktlintFormat)

assemble-phone:
		(./code/gradlew -p code/android-core-library assemblePhone)

assemble-phone-release:
		(./code/gradlew -p code/android-core-library assemblePhoneRelease)

unit-test:
		(./code/gradlew -p code/android-core-library testPhoneDebugUnitTest)
		(./code/gradlew -p code/android-core-compatiblity testPhoneDebugUnitTest)
		(./code/gradlew -p code/android-signal-library testPhoneDebugUnitTest)
		(./code/gradlew -p code/android-lifecycle-library testPhoneDebugUnitTest)

functional-test:
		(./code/gradlew -p code/android-core-library uninstallPhoneDebugAndroidTest)
		(./code/gradlew -p code/android-core-library connectedPhoneDebugAndroidTest)
		(./code/gradlew -p code/android-core-compatiblity  uninstallPhoneDebugAndroidTest)
		(./code/gradlew -p code/android-core-compatiblity  connectedPhoneDebugAndroidTest)
		(./code/gradlew -p code/android-lifecycle-library uninstallPhoneDebugAndroidTest)
		(./code/gradlew -p code/android-lifecycle-library connectedPhoneDebugAndroidTest)

integration-test:
		(./code/gradlew -p code/integration-tests uninstallDebugAndroidTest)
		(./code/gradlew -p code/integration-tests connectedDebugAndroidTest)


javadoc:
		(./code/gradlew -p code/android-core-library dokkaJavadoc)


build-third-party-extension:
		(./code/gradlew test-third-party-extension:build)

publishCoreToMavenLocal:
		(./code/gradlew -p code/android-core-library publishReleasePublicationToMavenLocal)

ci-publish:
	(./code/gradlew -p code/android-core-library  publishReleasePublicationToSonatypeRepository)


