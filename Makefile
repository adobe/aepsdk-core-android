
clean:
	  (./code/gradlew -p code clean)

checkstyle: core-checkstyle signal-checkstyle lifecycle-checkstyle identity-checkstyle

checkformat: core-checkformat signal-checkformat lifecycle-checkformat identity-checkformat

format: core-format signal-format lifecycle-format identity-format

api-dump: 
		(./code/gradlew -p code/android-core-library apiDump)

api-check: 
		(./code/gradlew -p code/android-core-library apiCheck)

assemble-phone: core-assemble-phone signal-assemble-phone lifecycle-assemble-phone identity-assemble-phone

assemble-phone-release: core-assemble-phone-release signal-assemble-phone-release lifecycle-assemble-phone-release identity-assemble-phone-release

javadoc: core-javadoc

unit-test: core-unit-test signal-unit-test lifecycle-unit-test

unit-test-coverage: core-unit-test-coverage signal-unit-test-coverage lifecycle-unit-test-coverage

functional-test: core-functional-test signal-functional-test lifecycle-functional-test identity-functional-test

functional-test-coverage: core-functional-test-coverage signal-functional-test-coverage lifecycle-functional-test-coverage identity-functional-test-coverage

integration-test: 
		(./code/gradlew -p code/integration-tests uninstallDebugAndroidTest)
		(./code/gradlew -p code/integration-tests connectedDebugAndroidTest)

build-third-party-extension:
		(./code/gradlew test-third-party-extension:build)

### Core 

core-checkstyle:
		(./code/gradlew -p code/android-core-library checkstyle)

core-checkformat:
		(./code/gradlew -p code/android-core-library spotlessCheck)

core-format:
		(./code/gradlew -p code/android-core-library spotlessApply)

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
		(./code/gradlew -p code/android-core-library publishReleasePublicationToMavenLocal -x signReleasePublication)		

core-publish-maven-local-jitpack:
		(./code/gradlew -p code/android-core-library publishReleasePublicationToMavenLocal -Pjitpack -x signReleasePublication)		

### Signal 

signal-checkstyle:
		(./code/gradlew -p code/android-signal-library checkstyle)

signal-checkformat:
		(./code/gradlew -p code/android-signal-library spotlessCheck)

signal-format:
		(./code/gradlew -p code/android-signal-library spotlessApply)

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
		(./code/gradlew -p code/android-signal-library assemblePhone)
		(./code/gradlew -p code/android-signal-library publishReleasePublicationToMavenLocal)		

signal-publish-maven-local-jitpack:
		(./code/gradlew -p code/android-signal-library assemblePhone)
		(./code/gradlew -p code/android-signal-library publishReleasePublicationToMavenLocal -Pjitpack)		

### Lifecycle 

lifecycle-checkstyle:
		(./code/gradlew -p code/android-lifecycle-library checkstyle)

lifecycle-checkformat:
		(./code/gradlew -p code/android-lifecycle-library spotlessCheck)

lifecycle-format:
		(./code/gradlew -p code/android-lifecycle-library spotlessApply)

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
		(./code/gradlew -p code/android-lifecycle-library assemblePhone)
		(./code/gradlew -p code/android-lifecycle-library publishReleasePublicationToMavenLocal)		

lifecycle-publish-maven-local-jitpack:
		(./code/gradlew -p code/android-lifecycle-library assemblePhone)
		(./code/gradlew -p code/android-lifecycle-library publishReleasePublicationToMavenLocal -Pjitpack)

### Identity 

identity-checkstyle:
		(./code/gradlew -p code/android-identity-library checkstyle)

identity-checkformat:
		(./code/gradlew -p code/android-identity-library spotlessCheck)

identity-format:
		(./code/gradlew -p code/android-identity-library spotlessApply)

identity-assemble-phone:
		(./code/gradlew -p code/android-identity-library assemblePhone)

identity-assemble-phone-release:		
		(./code/gradlew -p code/android-identity-library assemblePhoneRelease)

identity-unit-test:
		(./code/gradlew -p code/android-identity-library testPhoneDebugUnitTest)

identity-unit-test-coverage:
		(./code/gradlew -p code/android-identity-library createPhoneDebugUnitTestCoverageReport)

identity-functional-test:
		(./code/gradlew -p code/android-identity-library uninstallPhoneDebugAndroidTest)
		(./code/gradlew -p code/android-identity-library connectedPhoneDebugAndroidTest)		

identity-functional-test-coverage:		
		(./code/gradlew -p code/android-identity-library createPhoneDebugAndroidTestCoverageReport)

identity-publish:
		(./code/gradlew -p code/android-identity-library  publishReleasePublicationToSonatypeRepository)

identity-publish-maven-local:
		(./code/gradlew -p code/android-identity-library assemblePhone)
		(./code/gradlew -p code/android-identity-library publishReleasePublicationToMavenLocal)		

identity-publish-maven-local-jitpack:
		(./code/gradlew -p code/android-identity-library assemblePhone)
		(./code/gradlew -p code/android-identity-library publishReleasePublicationToMavenLocal -Pjitpack)

#compatibility
compatibility-publish-maven-local:
		(./code/gradlew -p code/android-core-compatiblity assemblePhone)
		(./code/gradlew -p code/android-core-compatiblity publishReleasePublicationToMavenLocal)		

compatibility-publish-maven-local-jitpack:
		(./code/gradlew -p code/android-core-compatiblity assemblePhone)
		(./code/gradlew -p code/android-core-compatiblity publishReleasePublicationToMavenLocal -Pjitpack)
		
# make bump-versions from='2\.0\.0' to=2.0.1
bump-versions:
	(LC_ALL=C find . -type f -name 'gradle.properties' -exec sed -i '' 's/$(from)/$(to)/' {} +)
	(LC_ALL=C find . -type f -name '*.kt' -exec sed -i '' 's/$(from)/$(to)/' {} +)	
	(LC_ALL=C find . -type f -name '*.java' -exec sed -i '' 's/$(from)/$(to)/' {} +)
