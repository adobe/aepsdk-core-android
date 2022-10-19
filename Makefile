BRANCH_VERSION=$(shell git rev-parse --abbrev-ref HEAD | sed "s/dev-v//g")
update-version:
	echo $(BRANCH_VERSION)
	sed -i '' "s/[0-9]*\.[0-9]*\.[0-9]/$(BRANCH_VERSION)/g" ./android-core-library/src/phone/java/com/adobe/marketing/mobile/ExtensionVersionManager.java
	sed -i '' "s/\(coreExtensionVersion=\)[0-9]*\.[0-9]*\.[0-9]/\1$(BRANCH_VERSION)/g" ./gradle.properties
	sed -i '' "s/\(mavenCoreVersion=\)[0-9]*\.[0-9]*\.[0-9]/\1$(BRANCH_VERSION)/g" ./gradle.properties
	sed -i '' "s/\(coreLibraryMavenRootVersion=\)[0-9]*\.[0-9]*\.[0-9]/\1$(BRANCH_VERSION)/g" ./gradle.properties

clean:
	  (./code/gradlew -p code clean)

checkstyle: core-checkstyle
		
check-format: core-check-format

format: core-format

api-dump: 
		(./code/gradlew -p code/android-core-library apiDump)

api-check: 
		(./code/gradlew -p code/android-core-library apiCheck)

assemble-phone: core-assemble-phone signal-assemble-phone lifecycle-assemble-phone
	
assemble-phone-release: core-assemble-phone-release signal-assemble-phone-release lifecycle-assemble-phone-release

javadoc: core-javadoc

unit-test: core-unit-test signal-unit-test lifecycle-unit-test

unit-test-coverage: core-unit-test-coverage signal-unit-test-coverage lifecycle-unit-test-coverage

functional-test: core-functional-test signal-functional-test lifecycle-functional-test

functional-test-coverage: core-functional-test-coverage signal-functional-test-coverage lifecycle-functional-test-coverage

integration-test: 
		(./code/gradlew -p code/integration-tests uninstallDebugAndroidTest)
		(./code/gradlew -p code/integration-tests connectedDebugAndroidTest)

build-third-party-extension:
		(./code/gradlew test-third-party-extension:build)

### Core 

core-checkstyle:
		(./code/gradlew -p code/android-core-library checkstyle)

core-check-format:
		(./code/gradlew -p code/android-core-library ktlintCheck)

core-format:
		(./code/gradlew -p code/android-core-library ktlintFormat)

core-assemble-phone:
		(./code/gradlew -p code/android-core-library assemblePhone)
		
core-assemble-phone-release:		
		(./code/gradlew -p code/android-core-library assemblePhoneRelease)
		
core-unit-test:
		(./code/gradlew -p code/android-core-library testPhoneDebugUnitTest)
		
core-unit-test-coverage:
		(./code/gradlew -p code/android-core-library createPhoneDebugUnitTestCoverageReport)

core-functional-test:
		(./code/gradlew -p code/android-core-library uninstallPhoneDebugAndroidTest)
		(./code/gradlew -p code/android-core-library connectedPhoneDebugAndroidTest)		

core-functional-test-coverage:		
		(./code/gradlew -p code/android-core-library createPhoneDebugAndroidTestCoverageReport)

core-javadoc:
		(./code/gradlew -p code/android-core-library dokkaJavadoc)

core-publish:
		(./code/gradlew -p code/android-core-library  publishReleasePublicationToSonatypeRepository)

core-publish-maven-local:
		(./code/gradlew -p code/android-core-library publishReleasePublicationToMavenLocal)		

### Signal 

signal-checkstyle:
		(./code/gradlew -p code/android-signal-library checkstyle)

signal-check-format:
		(./code/gradlew -p code/android-signal-library ktlintCheck)

signal-format:
		(./code/gradlew -p code/android-signal-library ktlintFormat)

signal-assemble-phone:
		(./code/gradlew -p code/android-signal-library assemblePhone)
		
signal-assemble-phone-release:		
		(./code/gradlew -p code/android-signal-library assemblePhoneRelease)
		
signal-unit-test:
		(./code/gradlew -p code/android-signal-library testPhoneDebugUnitTest)
		
signal-unit-test-coverage:
		(./code/gradlew -p code/android-signal-library createPhoneDebugUnitTestCoverageReport)

signal-functional-test:
		(./code/gradlew -p code/android-signal-library uninstallPhoneDebugAndroidTest)
		(./code/gradlew -p code/android-signal-library connectedPhoneDebugAndroidTest)		

signal-functional-test-coverage:		
		(./code/gradlew -p code/android-signal-library createPhoneDebugAndroidTestCoverageReport)

signal-publish:
		(./code/gradlew -p code/android-signal-library  publishReleasePublicationToSonatypeRepository)

signal-publish-maven-local:
		(./code/gradlew -p code/android-signal-library publishReleasePublicationToMavenLocal)		

### Lifecycle 

lifecycle-checkstyle:
		(./code/gradlew -p code/android-lifecycle-library checkstyle)

lifecycle-assemble-phone:
		(./code/gradlew -p code/android-lifecycle-library assemblePhone)
		
lifecycle-assemble-phone-release:		
		(./code/gradlew -p code/android-lifecycle-library assemblePhoneRelease)
		
lifecycle-unit-test:
		(./code/gradlew -p code/android-lifecycle-library testPhoneDebugUnitTest)
		
lifecycle-unit-test-coverage:
		(./code/gradlew -p code/android-lifecycle-library createPhoneDebugUnitTestCoverageReport)

lifecycle-functional-test:
		(./code/gradlew -p code/android-lifecycle-library uninstallPhoneDebugAndroidTest)
		(./code/gradlew -p code/android-lifecycle-library connectedPhoneDebugAndroidTest)		

lifecycle-functional-test-coverage:		
		(./code/gradlew -p code/android-lifecycle-library createPhoneDebugAndroidTestCoverageReport)

lifecycle-publish:
		(./code/gradlew -p code/android-lifecycle-library  publishReleasePublicationToSonatypeRepository)

lifecycle-publish-maven-local:
		(./code/gradlew -p code/android-lifecycle-library publishReleasePublicationToMavenLocal)	

### Identity 
