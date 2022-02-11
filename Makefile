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
	  (./code/gradlew clean)

checkstyle:
		(./code/gradlew android-core-library:checkstyle)

assemble-phone:
		(./code/gradlew android-core-library:assemblePhone)

assemble-phone-release:
		(./code/gradlew android-core-library:assemblePhoneRelease)

unit-test:
		(./code/gradlew android-core-library:testPhoneDebugUnitTest)

functional-test:
		(./code/gradlew android-core-library:uninstallPhoneDebugAndroidTest)
		(./code/gradlew android-core-library:connectedPhoneDebugAndroidTest)

javadoc:
		(./code/gradlew android-core-library:Javadoc)


build-third-party-extension:
		(./code/gradlew test-third-party-extension:build)




