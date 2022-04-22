/*
  Copyright 2022 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
 */

package com.adobe.marketing.mobile;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.adobe.marketing.mobile.E2ETestableNetworkService.NetworkResponse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class AndroidConfigurationFunctionalTests extends AbstractE2ETest {
	private TestHelper testHelper = new TestHelper();
	private static String OPTEDIN = "optedin";
	private static String OPTEDOUT = "optedout";
	private static final String CONFIGURATION_URL_BASE    = "https://assets.adobedtm.com/%s.json";

	@Before
	public void setUp() {
		super.setUp();
		testHelper.cleanCache(defaultContext);
		MobileCore.setApplication(defaultApplication);
		MobileCore.setLogLevel(LoggingMode.DEBUG);
		MobileCore.start(null);
		testableNetworkService.resetTestableNetworkService();
	}

	@After
	public void tearDown() {
		super.tearDown();
	}

	//==========================================================================================================================================
	// helper methods
	//==========================================================================================================================================
	private void setupRemoteConfigJsonNetworkResponse(String testType) {
		String remoteConfigJson = null;
		String configMatcher = "https://assets.adobedtm.com/E2ETest.json";
		E2ERequestMatcher networkMatcher = new E2ERequestMatcher(configMatcher);

		if (testType.equals(OPTEDIN)) {
			remoteConfigJson =
				"{ \"lastModified\": \"2016-10-21T22:02:56.699Z\", \"experienceCloud.org\": \"3CE342C75100435B0A490D4C@AdobeOrg\", \"experienceCloud.server\": \"remoteConfig.identity.server\", \"target.clientCode\": \"amsdk\", \"target.timeout\": 5, \"audience.server\": \"remoteConfig.audience.server\", \"audience.timeout\": 5, \"acquisition.appid\": \"E2ETest\", \"acquisition.server\": \"remoteConfig.acquisition.server\", \"acquisition.timeout\": 5, \"analytics.rsids\": \"mobile1pravinsample\", \"analytics.server\": \"remoteConfig.analytics.server\", \"analytics.aamForwardingEnabled\": false, \"analytics.offlineEnabled\": true, \"analytics.batchLimit\": 0, \"global.ssl\": false, \"global.privacy\": \"optedin\", \"global.timezone\": \"PDT\", \"global.timezoneOffset\": -420, \"lifecycle.sessionTimeout\": 1, \"analytics.backdatePreviousSessionInfo\": false, \"places.poiDatabaseUrl\": \"https://assets.adobedtm.com/b213090c5204bf94318f4ef0539a38b487d10368/scripts/satellite-57b64ee764746d36190028bd.json\" }";
		} else if (testType.equals(OPTEDOUT)) {
			remoteConfigJson =
				"{ \"lastModified\": \"2016-10-21T22:02:56.699Z\", \"experienceCloud.org\": \"3CE342C75100435B0A490D4C@AdobeOrg\", \"experienceCloud.server\": \"remoteConfig.identity.server\", \"target.clientCode\": \"amsdk\", \"target.timeout\": 5, \"audience.server\": \"remoteConfig.audience.server\", \"audience.timeout\": 5, \"acquisition.appid\": \"E2ETest\", \"acquisition.server\": \"remoteConfig.acquisition.server\", \"acquisition.timeout\": 5, \"analytics.rsids\": \"mobile1pravinsample\", \"analytics.server\": \"remoteConfig.analytics.server\", \"analytics.aamForwardingEnabled\": false, \"analytics.offlineEnabled\": true, \"analytics.batchLimit\": 0, \"global.ssl\": false, \"global.privacy\": \"optedout\", \"global.timezone\": \"PDT\", \"global.timezoneOffset\": -420, \"lifecycle.sessionTimeout\": 1, \"analytics.backdatePreviousSessionInfo\": false, \"places.poiDatabaseUrl\": \"https://assets.adobedtm.com/b213090c5204bf94318f4ef0539a38b487d10368/scripts/satellite-57b64ee764746d36190028bd.json\" }";
		}

		Map<String, String> headers = new HashMap<>();
		SimpleDateFormat simpleDateFormat = TestHelper.createRFC2822Formatter();
		headers.put("Last-Modified", simpleDateFormat.format(new Date()));
		NetworkResponse networkResponse = new NetworkResponse(remoteConfigJson, 200, headers);
		testableNetworkService.setResponse(networkMatcher, networkResponse);
	}

	//	//==========================================================================================================================================
	//	// Happy Tests
	//	//==========================================================================================================================================
	//	// Test Case No : 1
	//	@Test
	//	public void test_Functional_Happy_Configuration_configureWithAppID_CheckWithValidID() {
	//		//test setting from bundled config
	//
	//		MobileCore.trackAction("testBundledConfig", null);
	//		assertEquals(1, testableNetworkService.waitAndGetCount(5000));
	//		NetworkRequest analyticsHit = testableNetworkService.getItem(0);
	//		assertTrue(analyticsHit.url.contains("bundledConfig.analytics.server"));
	//		testableNetworkService.resetTestableNetworkService();
	//		//test setting app id then fetching and applying remote config
	//
	//		setupRemoteConfigJsonNetworkResponse(OPTEDIN);
	//		MobileCore.configureWithAppID("E2ETest");
	//		assertEquals(1, testableNetworkService.waitAndGetCount(5000));
	//		NetworkRequest remoteConfigRequest = testableNetworkService.getItem(0);
	//		//verify fetch for remote config from server contains the specified app id
	//		assertTrue(remoteConfigRequest.url.contains(String.format(CONFIGURATION_URL_BASE, "E2ETest")));
	//		testableNetworkService.resetTestableNetworkService();
	//
	//		MobileCore.trackAction("testConfigureWithAppId", null);
	//		assertEquals(1, testableNetworkService.waitAndGetCount(5000));
	//		analyticsHit = testableNetworkService.getItem(0);
	//		//verify the analytics server hit is being sent to remote config URL
	//		assertTrue(analyticsHit.url.contains("remoteConfig.analytics.server"));
	//	}
	//
	//	// Test Case No : 2
	//	@Test
	//	public void test_Functional_Happy_Configuration_configureWithFileInPath_CheckWithBundledConfiguration() {
	//		//test setting from bundled config
	//
	//		MobileCore.trackAction("testBundledConfig", null);
	//		assertEquals(1, testableNetworkService.waitAndGetCount(5000));
	//		NetworkRequest analyticsHit = testableNetworkService.getItem(0);
	//		assertTrue(analyticsHit.url.contains("bundledConfig.analytics.server"));
	//		testableNetworkService.resetTestableNetworkService();
	//
	//
	//		//test setting config from local file by loading config json from assets/test/, writing to external storage, then giving path of written file to API
	//		try {
	//			InputStream is = defaultContext.getAssets().open("test/localConfig.json");
	//			FileOutputStream fos =
	//				new FileOutputStream(new File(defaultContext.getFilesDir() + "/local.json"));
	//			int read = 0;
	//			byte[] bytes = new byte[1024];
	//
	//			while ((read = is.read(bytes)) != -1) {
	//				fos.write(bytes, 0, read);
	//			}
	//		} catch (Exception e) {
	//			e.printStackTrace();
	//		}
	//
	//		System.out.println("Writing to file directory " + defaultContext.getFilesDir().getAbsolutePath() + " done");
	//		MobileCore.configureWithFileInPath(defaultContext.getFilesDir() + "/local.json");
	//		testHelper.waitForThreadsWithFailIfTimedOut(100);
	//		System.out.println("Configured with local json");
	//		MobileCore.trackAction("testLocalConfig", null);
	//		assertEquals(1, testableNetworkService.waitAndGetCount(5000));
	//		analyticsHit = testableNetworkService.getItem(0);
	//		//verify the analytics server hit is being sent to local config URL
	//		assertTrue(analyticsHit.url.contains("localConfig.analytics.server"));
	//	}
	//
	//	// Test Case No : 3
	//	@Test
	//	public void test_Functional_Happy_Configuration_configureWithAppID_CheckForCorrectTargetCall() {
	//		//test setting from bundled config
	//
	//		MobileCore.trackAction("testBundledConfig", null);
	//		assertEquals(1, testableNetworkService.waitAndGetCount(5000));
	//		NetworkRequest analyticsHit = testableNetworkService.getItem(0);
	//		assertTrue(analyticsHit.url.contains("bundledConfig.analytics.server"));
	//		testableNetworkService.resetTestableNetworkService();
	//
	//		//test setting app id then fetching and applying remote config
	//		setupRemoteConfigJsonNetworkResponse(OPTEDIN);
	//		testHelper.waitForThreadsWithFailIfTimedOut(500);
	//		MobileCore.configureWithAppID("E2ETest");
	//		assertEquals(1, testableNetworkService.waitAndGetCount(5000));
	//		NetworkRequest remoteConfigRequest = testableNetworkService.getItem(0);
	//		//verify fetch for remote config from server contains the specified app id
	//		assertTrue(remoteConfigRequest.url.contains(String.format(CONFIGURATION_URL_BASE, "E2ETest")));
	//		testableNetworkService.resetTestableNetworkService();
	//
	//		MobileCore.trackAction("configureWithAppId", null);
	//		assertEquals(1, testableNetworkService.waitAndGetCount(5000));
	//		analyticsHit = testableNetworkService.getItem(0);
	//		//verify the analytics server hit is being sent to remote config URL
	//		assertTrue(analyticsHit.url.contains("remoteConfig.analytics.server"));
	//	}
	//
	//	// Test Case No : 4
	//	@Test
	//	public void test_Functional_Happy_Configuration_DefaultADBMobileConfig_CheckDefaultADBMobileConfigWorks() {
	//		//test setting from bundled config
	//
	//		MobileCore.trackAction("testBundledConfig", null);
	//		assertEquals(1, testableNetworkService.waitAndGetCount(5000));
	//		NetworkRequest analyticsHit = testableNetworkService.getItem(0);
	//		assertTrue(analyticsHit.url.contains("bundledConfig.analytics.server"));
	//	}
	//
	//	// Test Case No : 5
	//	@Test
	//	public void test_Functional_Happy_Configuration_setPrivacyStatus_getPrivacyStatus_CheckThePrivacyStatusChange() throws
	//		InterruptedException {
	//		//setup
	//		final MobilePrivacyStatus privacyStatus[] = new MobilePrivacyStatus[3];
	//		final CountDownLatch latch = new CountDownLatch(1);
	//		final CountDownLatch latch2 = new CountDownLatch(1);
	//		final CountDownLatch latch3 = new CountDownLatch(1);
	//		AdobeCallback callback = new AdobeCallback<MobilePrivacyStatus>() {
	//			@Override
	//			public void call(MobilePrivacyStatus status) {
	//				privacyStatus[0] = status;
	//				latch.countDown();
	//			}
	//		};
	//		AdobeCallback callback2 = new AdobeCallback<MobilePrivacyStatus>() {
	//			@Override
	//			public void call(MobilePrivacyStatus status) {
	//				privacyStatus[1] = status;
	//				latch2.countDown();
	//			}
	//		};
	//		AdobeCallback callback3 = new AdobeCallback<MobilePrivacyStatus>() {
	//			@Override
	//			public void call(MobilePrivacyStatus status) {
	//				privacyStatus[2] = status;
	//				latch3.countDown();
	//			}
	//		};
	//		//test
	//		MobileCore.getPrivacyStatus(callback);
	//		latch.await(5, TimeUnit.SECONDS);
	//		MobileCore.setPrivacyStatus(MobilePrivacyStatus.UNKNOWN);
	//		MobileCore.getPrivacyStatus(callback2);
	//		latch2.await(5, TimeUnit.SECONDS);
	//		MobileCore.setPrivacyStatus(MobilePrivacyStatus.OPT_OUT);
	//		MobileCore.getPrivacyStatus(callback3);
	//		latch3.await(5, TimeUnit.SECONDS);
	//		//verify
	//		assertEquals(MobilePrivacyStatus.OPT_IN, privacyStatus[0]);
	//		assertEquals(MobilePrivacyStatus.UNKNOWN, privacyStatus[1]);
	//		assertEquals(MobilePrivacyStatus.OPT_OUT, privacyStatus[2]);
	//	}
	//
	//	// Test Case No : 6
	//	@Test
	//	public void
	//	test_Functional_Happy_Configuration_UpdateConfiguration_CheckTheUpdatedConfigurationOverridesSetPrivacyStatusAPI() {
	//		//setup
	//		MobileCore.setPrivacyStatus(MobilePrivacyStatus.OPT_OUT);
	//		//test
	//		MobileCore.trackAction(OPTEDOUT, null);
	//		assertEquals(0, testableNetworkService.waitAndGetCount(1));
	//
	//		//setup with updateConfiguration to override setPrivacyStatus API
	//		Map<String, Object> data = new HashMap<String, Object>();
	//		data.put("global.privacy", OPTEDIN);
	//		MobileCore.updateConfiguration(data);
	//		testHelper.waitForThreadsWithFailIfTimedOut(500);
	//		//test
	//		MobileCore.trackAction(OPTEDIN, null);
	//		assertEquals(1, testableNetworkService.waitAndGetCount(5000));
	//	}
	//
	//	// Test Case No : 7
	//	@Test
	//	public void test_Functional_Happy_Configuration_VariousConfigurationAPI_TestConfigurationSettingPriority() {
	//		//setup then test setting from bundled config
	//
	//		MobileCore.trackAction("testBundledConfig", null);
	//		//verify bundled config in use
	//		assertEquals(1, testableNetworkService.waitAndGetCount(5000));
	//		NetworkRequest analyticsHit = testableNetworkService.getItem(0);
	//		assertTrue(analyticsHit.url.contains("bundledConfig.analytics.server"));
	//		testableNetworkService.resetTestableNetworkService();
	//
	//		//setup then test setting config from local file by loading config json from assets/test/, writing to external storage, then giving path of written file to API
	//		try {
	//			InputStream is = defaultContext.getAssets().open("test/localConfig.json");
	//			FileOutputStream fos =
	//				new FileOutputStream(new File(defaultContext.getFilesDir() + "/local.json"));
	//			int read = 0;
	//			byte[] bytes = new byte[1024];
	//
	//			while ((read = is.read(bytes)) != -1) {
	//				fos.write(bytes, 0, read);
	//			}
	//		} catch (Exception e) {
	//			e.printStackTrace();
	//		}
	//
	//		System.out.println("Writing to file directory " + defaultContext.getFilesDir().getAbsolutePath() + " done");
	//		MobileCore.configureWithFileInPath(defaultContext.getFilesDir() + "/local.json");
	//
	//		MobileCore.trackAction("testLocalConfig", null);
	//		//verify local config in use
	//		assertEquals(1, testableNetworkService.waitAndGetCount(5000));
	//		analyticsHit = testableNetworkService.getItem(0);
	//		assertTrue(analyticsHit.url.contains("localConfig.analytics.server"));
	//		testableNetworkService.resetTestableNetworkService();
	//		//setup then test setting app id then fetching and applying remote config
	//
	//		setupRemoteConfigJsonNetworkResponse(OPTEDIN);
	//		testHelper.waitForThreadsWithFailIfTimedOut(500);
	//		MobileCore.configureWithAppID("E2ETest");
	//		//verify fetch for remote config from server contains the specified app id
	//		assertEquals(1, testableNetworkService.waitAndGetCount(5000));
	//		NetworkRequest remoteConfigRequest = testableNetworkService.getItem(0);
	//		assertTrue(remoteConfigRequest.url.contains(String.format(CONFIGURATION_URL_BASE, "E2ETest")));
	//		testableNetworkService.resetTestableNetworkService();
	//		//setup then test remote config
	//
	//		MobileCore.trackAction("testConfigureWithAppId", null);
	//		//verify the analytics server hit is being sent to remote config URL
	//		assertEquals(1, testableNetworkService.waitAndGetCount(5000));
	//		analyticsHit = testableNetworkService.getItem(0);
	//		assertTrue(analyticsHit.url.contains("remoteConfig.analytics.server"));
	//		testableNetworkService.resetTestableNetworkService();
	//		//setup using programmatic config change and verify that it overrides the previously set values
	//
	//		HashMap<String, Object> data = new HashMap<String, Object> ();
	//		data.put("acquisition.server", "programmatic.acquisition.server");
	//		data.put("analytics.server", "programmatic.analytics.server");
	//		data.put("analytics.rsids", "rsid1,rsid2");
	//		data.put("analytics.referrerTimeout", 1);
	//		data.put("analytics.offlineEnabled", false);
	//		data.put("global.privacy", "optedin");
	//		data.put("global.ssl", true);
	//		data.put("experienceCloud.org", "972C898555E9F7BC7F000101@AdobeOrg");
	//		data.put("experienceCloud.server", "programmatic.identity.server");
	//		data.put("rulesEngine.url", null);
	//		data.put("messaging.url", null);
	//		MobileCore.updateConfiguration(data);
	//		MobileCore.trackAction("testProgrammaticConfig", null);
	//		//verify the analytics server hit is being sent to programmatic config URL
	//		assertEquals(1, testableNetworkService.waitAndGetCount(5000));
	//		analyticsHit = testableNetworkService.getItem(0);
	//		assertTrue(analyticsHit.url.contains("programmatic.analytics.server"));
	//	}
	//
	//	// Test Case No : 8
	//	@Test
	//	public void test_Functional_Happy_Configuration_TestConfigIsUpdatedOnLifecycleSessionStart() {
	//		//test setting from bundled config
	//
	//		MobileCore.trackAction("testBundledConfig", null);
	//		assertEquals(1, testableNetworkService.waitAndGetCount(5000));
	//		NetworkRequest analyticsHit = testableNetworkService.getItem(0);
	//		assertTrue(analyticsHit.url.contains("bundledConfig.analytics.server"));
	//		testableNetworkService.resetTestableNetworkService();
	//
	//		//test setting app id then fetching and applying remote config
	//		setupRemoteConfigJsonNetworkResponse(OPTEDIN);
	//		testHelper.waitForThreadsWithFailIfTimedOut(500);
	//		MobileCore.configureWithAppID("E2ETest");
	//		assertEquals(1, testableNetworkService.waitAndGetCount(5000));
	//		NetworkRequest remoteConfigRequest = testableNetworkService.getItem(0);
	//		//verify fetch for remote config from server contains the specified app id
	//		assertTrue(remoteConfigRequest.url.contains(String.format(CONFIGURATION_URL_BASE, "E2ETest")));
	//		testableNetworkService.resetTestableNetworkService();
	//
	//		MobileCore.trackAction("testConfigureWithAppId", null);
	//		assertEquals(1, testableNetworkService.waitAndGetCount(5000));
	//		analyticsHit = testableNetworkService.getItem(0);
	//		//verify the analytics server hit is being sent to remote config URL
	//		assertTrue(analyticsHit.url.contains("remoteConfig.analytics.server"));
	//		testableNetworkService.resetTestableNetworkService();
	//		//set lifecycle timeout to 1, start a lifecycle session, wait for it to expire, then start a new lifecycle session
	//		HashMap<String, Object> data = new HashMap<String, Object> ();
	//		data.put("lifecycle.sessionTimeout", 1);
	//		MobileCore.updateConfiguration(data);
	//		MobileCore.lifecycleStart(null);
	//		MobileCore.lifecyclePause();
	//
	//		// there are three requests, one config, one analytics, and one AAM, the order is undetermined
	//		assertEquals(3, testableNetworkService.waitAndGetCount(5000));
	//
	//
	//		testableNetworkService.resetTestableNetworkService();
	//		testHelper.waitForThreadsWithFailIfTimedOut(2000);
	//		MobileCore.lifecycleStart(null);
	//		MobileCore.lifecyclePause();
	//		setupRemoteConfigJsonNetworkResponse(OPTEDIN);
	//
	//		// there are three requests, one config, one analytics, and one AAM, the order is undetermined
	//		assertEquals(3, testableNetworkService.waitAndGetCount(5000));
	//
	//		//verify fetch for remote config occurs again on lifecycle session restart after session timeout
	//		// need to find the configuration request first
	//		boolean foundConfig = false;
	//
	//		for (int i = 0; i < 3; i++) {
	//			remoteConfigRequest = testableNetworkService.getItem(i);
	//
	//			if (remoteConfigRequest.url.contains(String.format(CONFIGURATION_URL_BASE, "E2ETest"))) {
	//				foundConfig = true;
	//			}
	//		}
	//
	//		assertTrue("Did not find request URL with expected Configuration URL!", foundConfig);
	//	}

	// Test Case No : 9
	@Test
	public void test_Functional_Happy_Configuration_SetThenGetSmallIconResourceID_TestTheSetSmallIconWorks() {
		App.setSmallIconResourceID(12345);
		int smallResID = App.getSmallIconResourceID();
		assertEquals(smallResID, 12345);
	}

	// Test Case No : 10
	@Test
	public void test_Functional_Happy_Configuration_SetThenGetLargeIconResourceID_TestTheSetLargeIconWorks() {
		App.setLargeIconResourceID(67890);
		int largeResID = App.getLargeIconResourceID();
		assertEquals(largeResID, 67890);
	}
}