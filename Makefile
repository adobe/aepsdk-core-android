
clean:
	  (./code/gradlew -p code clean)

checkstyle: core-checkstyle signal-checkstyle lifecycle-checkstyle identity-checkstyle

checkformat: core-checkformat signal-checkformat lifecycle-checkformat identity-checkformat testutils-checkformat

# Used by build and test CI workflow
lint: checkstyle checkformat

format: core-format signal-format lifecycle-format identity-format testutils-format

api-dump: 
		(./code/gradlew -p code/core apiDump)
		(./code/gradlew -p code/testutils apiDump)

api-check: 
		(./code/gradlew -p code/core apiCheck)
		(./code/gradlew -p code/testutils apiCheck)

assemble-phone: core-assemble-phone signal-assemble-phone lifecycle-assemble-phone identity-assemble-phone

assemble-phone-release: core-assemble-phone-release signal-assemble-phone-release lifecycle-assemble-phone-release identity-assemble-phone-release

unit-test: core-unit-test signal-unit-test lifecycle-unit-test identity-unit-test testutils-unit-test

unit-test-coverage: core-unit-test-coverage signal-unit-test-coverage lifecycle-unit-test-coverage identity-unit-test-coverage testutils-unit-test-coverage

functional-test: core-functional-test signal-functional-test lifecycle-functional-test identity-functional-test

functional-test-coverage: core-functional-test-coverage signal-functional-test-coverage lifecycle-functional-test-coverage identity-functional-test-coverage

integration-test: 
		(./code/gradlew -p code/integration-tests uninstallDebugAndroidTest)
		(./code/gradlew -p code/integration-tests connectedDebugAndroidTest)

# Alias for CI
integration-test-coverage: integration-test

build-third-party-extension:
		(./code/gradlew test-third-party-extension:build)

### Core 

core-checkstyle:
		(./code/gradlew -p code/core checkstyle)

core-checkformat:
		(./code/gradlew -p code/core spotlessCheck)

core-format:
		(./code/gradlew -p code/core spotlessApply)

core-assemble-phone:
		(./code/gradlew -p code/core assemblePhone)

core-assemble-phone-release:		
		(./code/gradlew -p code/core assemblePhoneRelease)

core-unit-test:
		(./code/gradlew -p code/core testPhoneDebugUnitTest)

core-unit-test-coverage:
		(./code/gradlew -p code/core createPhoneDebugUnitTestCoverageReport)

core-functional-test:
		(./code/gradlew -p code/core uninstallPhoneDebugAndroidTest)
		(./code/gradlew -p code/core connectedPhoneDebugAndroidTest)		

core-functional-test-coverage:		
		(./code/gradlew -p code/core createPhoneDebugAndroidTestCoverageReport)

core-javadoc:
		(./code/gradlew -p code/core dokkaJavadoc)

# Alias for CI
javadoc: core-javadoc

core-publish-snapshot: clean core-assemble-phone-release
		(./code/gradlew -p code/core publishReleasePublicationToSonatypeRepository --stacktrace)

core-publish-main: clean core-assemble-phone-release
		(./code/gradlew -p code/core publishReleasePublicationToSonatypeRepository -Prelease)

core-publish-maven-local:
		(./code/gradlew -p code/core publishReleasePublicationToMavenLocal -x signReleasePublication)		

core-publish-maven-local-jitpack:
		(./code/gradlew -p code/core publishReleasePublicationToMavenLocal -Pjitpack -x signReleasePublication)		

### Signal 

signal-checkstyle:
		(./code/gradlew -p code/signal checkstyle)

signal-checkformat:
		(./code/gradlew -p code/signal spotlessCheck)

signal-format:
		(./code/gradlew -p code/signal spotlessApply)

signal-assemble-phone:
		(./code/gradlew -p code/signal assemblePhone)

signal-assemble-phone-release:		
		(./code/gradlew -p code/signal assemblePhoneRelease)

signal-unit-test:
		(./code/gradlew -p code/signal testPhoneDebugUnitTest)

signal-unit-test-coverage:
		(./code/gradlew -p code/signal createPhoneDebugUnitTestCoverageReport)

signal-functional-test:
		(./code/gradlew -p code/signal uninstallPhoneDebugAndroidTest)
		(./code/gradlew -p code/signal connectedPhoneDebugAndroidTest)		

signal-functional-test-coverage:		
		(./code/gradlew -p code/signal createPhoneDebugAndroidTestCoverageReport)

signal-publish-snapshot: clean signal-assemble-phone
		(./code/gradlew -p code/signal publishReleasePublicationToSonatypeRepository --stacktrace)

signal-publish-main: clean signal-assemble-phone
		(./code/gradlew -p code/signal publishReleasePublicationToSonatypeRepository -Prelease)

signal-publish-maven-local:
		(./code/gradlew -p code/signal assemblePhone)
		(./code/gradlew -p code/signal publishReleasePublicationToMavenLocal)		

signal-publish-maven-local-jitpack:
		(./code/gradlew -p code/signal assemblePhone)
		(./code/gradlew -p code/signal publishReleasePublicationToMavenLocal -Pjitpack -x signReleasePublication)		

### Lifecycle 

lifecycle-checkstyle:
		(./code/gradlew -p code/lifecycle checkstyle)

lifecycle-checkformat:
		(./code/gradlew -p code/lifecycle spotlessCheck)

lifecycle-format:
		(./code/gradlew -p code/lifecycle spotlessApply)

lifecycle-assemble-phone:
		(./code/gradlew -p code/lifecycle assemblePhone)

lifecycle-assemble-phone-release:		
		(./code/gradlew -p code/lifecycle assemblePhoneRelease)

lifecycle-unit-test:
		(./code/gradlew -p code/lifecycle testPhoneDebugUnitTest)

lifecycle-unit-test-coverage:
		(./code/gradlew -p code/lifecycle createPhoneDebugUnitTestCoverageReport)

lifecycle-functional-test:
		(./code/gradlew -p code/lifecycle uninstallPhoneDebugAndroidTest)
		(./code/gradlew -p code/lifecycle connectedPhoneDebugAndroidTest)		

lifecycle-functional-test-coverage:		
		(./code/gradlew -p code/lifecycle createPhoneDebugAndroidTestCoverageReport)

lifecycle-publish-snapshot: clean lifecycle-assemble-phone
		# (./code/gradlew -p code/lifecycle compilePhoneDebugJavaWithJavac)
		(./code/gradlew -p code/lifecycle publishReleasePublicationToSonatypeRepository --stacktrace)

lifecycle-publish-main: clean lifecycle-assemble-phone
		(./code/gradlew -p code/lifecycle publishReleasePublicationToSonatypeRepository -Prelease)

lifecycle-publish-maven-local:
		(./code/gradlew -p code/lifecycle assemblePhone)
		(./code/gradlew -p code/lifecycle publishReleasePublicationToMavenLocal)		

lifecycle-publish-maven-local-jitpack:
		(./code/gradlew -p code/lifecycle assemblePhone)
		(./code/gradlew -p code/lifecycle publishReleasePublicationToMavenLocal -Pjitpack -x signReleasePublication)

### Identity 

identity-checkstyle:
		(./code/gradlew -p code/identity checkstyle)

identity-checkformat:
		(./code/gradlew -p code/identity spotlessCheck)

identity-format:
		(./code/gradlew -p code/identity spotlessApply)

identity-assemble-phone:
		(./code/gradlew -p code/identity assemblePhone)

identity-assemble-phone-release:		
		(./code/gradlew -p code/identity assemblePhoneRelease)

identity-unit-test:
		(./code/gradlew -p code/identity testPhoneDebugUnitTest)

identity-unit-test-coverage:
		(./code/gradlew -p code/identity createPhoneDebugUnitTestCoverageReport)

identity-functional-test:
		(./code/gradlew -p code/identity uninstallPhoneDebugAndroidTest)
		(./code/gradlew -p code/identity connectedPhoneDebugAndroidTest)		

identity-functional-test-coverage:
		(./code/gradlew -p code/identity createPhoneDebugAndroidTestCoverageReport)

identity-publish-snapshot: clean identity-assemble-phone
		(./code/gradlew -p code/identity publishReleasePublicationToSonatypeRepository --stacktrace)

identity-publish-main: clean identity-assemble-phone
		(./code/gradlew -p code/identity publishReleasePublicationToSonatypeRepository -Prelease)

identity-publish-maven-local:
		(./code/gradlew -p code/identity assemblePhone)
		(./code/gradlew -p code/identity publishReleasePublicationToMavenLocal)

identity-publish-maven-local-jitpack:
		(./code/gradlew -p code/identity assemblePhone)
		(./code/gradlew -p code/identity publishReleasePublicationToMavenLocal -Pjitpack -x signReleasePublication)

### TestUtils

testutils-checkformat:
		(./code/gradlew -p code/testutils spotlessCheck)

testutils-format:
		(./code/gradlew -p code/testutils spotlessApply)

testutils-unit-test:
		(./code/gradlew -p code/testutils testPhoneDebugUnitTest)

testutils-unit-test-coverage:
		(./code/gradlew -p code/testutils createPhoneDebugUnitTestCoverageReport)

testutils-publish-maven-local-jitpack:
		(./code/gradlew -p code/testutils assemblePhone)
		(./code/gradlew -p code/testutils publishReleasePublicationToMavenLocal -Pjitpack -x signReleasePublication)