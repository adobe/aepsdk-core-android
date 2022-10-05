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

package com.adobe.marketing.mobile.identity;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.adobe.marketing.mobile.AdobeCallback;
import com.adobe.marketing.mobile.Identity;
import com.adobe.marketing.mobile.LoggingMode;
import com.adobe.marketing.mobile.MobileCore;
import com.adobe.marketing.mobile.MobilePrivacyStatus;
import com.adobe.marketing.mobile.VisitorID;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
@RunWith(AndroidJUnit4.class)
public class OldIdentityFunctionalTests {
	private static final String LOG_TAG = "IdentityFunctionalTests";
	private static final String IDENTITY_DATA_STORE_NAME = "visitorIDServiceDataStore";
	private static final String VISITOR_IDS_KEY = "ADOBEMOBILE_VISITORID_IDS";
	private static final int TIMEOUT_IN_MS = 5000;



	private static boolean firstTestRun = true;
//	private AsyncHelper asyncHelper = new AsyncHelper();
//	private IdentityTestHelper identityTestHelper;

	@Before
	public void setUp() {
//		identityTestHelper = new IdentityTestHelper(this.testableNetworkService);
		MobileCore.setLogLevel(LoggingMode.VERBOSE);
//		MobileCore.setApplication(defaultApplication);

//		try {
//			Identity.registerExtension();
//		} catch (InvalidInitException e) {
//			Assert.fail("Identity extension initialization failed");
//			e.printStackTrace();
//		}

		MobileCore.start(null);
//		identityTestHelper.setupTestConfiguration();
//		assertEquals(1, testableNetworkService.waitAndGetCount(1, 10000));
//		testableNetworkService.resetTestableNetworkService();

		if (firstTestRun) {
//			Log.debug(LOG_TAG, "Testing with Identity version: (%s), Core version: (%s)", Identity.extensionVersion(),
//					  MobileCore.extensionVersion());
		}
	}

	@After
	public void tearDown() {
		firstTestRun = false;
	}

	@Test
	public void test_syncIdentifier_validateQueryParams_happy() throws InterruptedException {
//		//setup
//		final CountDownLatch latch1 = new CountDownLatch(1);
//		final String[] storedData = new String[2];
//		//test
//		Identity.syncIdentifier("idTypeSYNC", "idValueSYNC", VisitorID.AuthenticationState.AUTHENTICATED);
//		assertEquals(1, testableNetworkService.waitAndGetCount(1));
//		E2ETestableNetworkService.NetworkRequest request = testableNetworkService.getItem(0);
//		AdobeCallback callback = new AdobeCallback<String>() {
//			@Override
//			public void call(String data) {
//				storedData[0] = data;
//				latch1.countDown();
//			}
//		};
//		Identity.getExperienceCloudId(callback);
//		latch1.await(5, TimeUnit.SECONDS);
//		assertEquals(1, testableNetworkService.waitAndGetCount(1));
//		request = testableNetworkService.getItem(0);
//		//verify
//		assertTrue(request.url.contains("https://identity.com/id?"));
//		assertTrue(request.url.contains("d_cid_ic=idTypeSYNC"));
//		assertTrue(request.url.contains("idValueSYNC"));
//		assertTrue(request.url.contains("d_ver=2"));
//		assertTrue(request.url.contains("d_orgid=972C898555E9F7BC7F000101%40AdobeOrg"));
//		assertTrue(request.url.contains("d_mid=" + storedData[0]));
	}
//
//	@Test
//	public void test_syncIdentifiers_validateQueryParams_happy() {
//		//setup
//		String ecid = identityTestHelper.getECID();
//		identityTestHelper.setupIdentityModuleForE2E(ecid);
//		//test
//		Identity.syncIdentifiers(new HashMap<String, String>() {
//			{
//				put("abc", "def");
//				put("123", "456");
//				put("ghi", "jkl");
//			}
//		});
//		assertEquals(1, testableNetworkService.waitAndGetCount(1, TIMEOUT_IN_MS));
//		E2ETestableNetworkService.NetworkRequest request = testableNetworkService.getItem(0);
//		String requestString = testableNetworkService.getAllRequestsAsString();
//
//		//verify
//		assertTrue(requestString, request.url.contains("https://identity.com/id?"));
//		assertTrue(requestString, request.url.contains("d_blob=hmk_Lq6TPIBMW925SPhw3Q"));
//		assertTrue(requestString, request.url.contains("d_ver=2"));
//		assertTrue(requestString, request.url.contains("d_orgid=972C898555E9F7BC7F000101%40AdobeOrg"));
//		assertTrue(requestString, request.url.contains("d_cid_ic=ghi%01jkl%010"));
//		assertTrue(requestString, request.url.contains("d_cid_ic=123%01456%010"));
//		assertTrue(requestString, request.url.contains("d_cid_ic=abc%01def%010"));
//	}
//
//	@Test
//	public void test_syncIdentifiers_nullAndEmptyIdTypeAndIdentifier_ValidateQueryParams() {
//		//setup
//		String ecid = identityTestHelper.getECID();
//		identityTestHelper.setupIdentityModuleForE2E(ecid);
//		//test
//		Identity.syncIdentifiers(new HashMap<String, String>() {
//			{
//				put("keya", "value1");
//				put("keyb", "");
//				put("", "value3");
//				put(null, "value4");
//				put("keye", null);
//				put("keyf", "value6");
//			}
//		}, VisitorID.AuthenticationState.AUTHENTICATED);
//		assertEquals(1, testableNetworkService.waitAndGetCount(1));
//		E2ETestableNetworkService.NetworkRequest request = testableNetworkService.getItem(0);
//		String requestString = testableNetworkService.getAllRequestsAsString();
//
//		//verify
//		assertTrue(requestString, request.url.contains("https://identity.com/id?"));
//		assertTrue(requestString, request.url.contains("d_blob=hmk_Lq6TPIBMW925SPhw3Q"));
//		assertTrue(requestString, request.url.contains("d_ver=2"));
//		assertTrue(requestString, request.url.contains("d_orgid=972C898555E9F7BC7F000101%40AdobeOrg"));
//		assertTrue(requestString, request.url.contains("d_cid_ic=keya%01value1%011"));
//		assertTrue(requestString, request.url.contains("d_cid_ic=keyf%01value6%011"));
//		assertFalse(requestString, request.url.contains("keyb"));
//		assertFalse(requestString, request.url.contains("value3"));
//		assertFalse(requestString, request.url.contains("value4"));
//		assertFalse(requestString, request.url.contains("keye"));
//	}
//
//	@Test
//	public void test_syncIdentifiers_nullMap_doesNotSync() {
//		//test
//		Identity.syncIdentifiers(null, VisitorID.AuthenticationState.AUTHENTICATED);
//		Identity.syncIdentifiers(null);
//
//		// verify
//		asyncHelper.waitForAppThreads(1000, false);
//		assertEquals(0, testableNetworkService.waitAndGetCount(0));
//	}
//
//	@Test
//	public void test_syncIdentifiers_emptyMap_doesNotSync() {
//		//test
//		Identity.syncIdentifiers(new HashMap<String, String>(), VisitorID.AuthenticationState.AUTHENTICATED);
//		Identity.syncIdentifiers(new HashMap<String, String>());
//
//		// verify
//		asyncHelper.waitForAppThreads(1000, false);
//		assertEquals(0, testableNetworkService.waitAndGetCount(0));
//	}
//
//	@Test
//	public void test_setAdvertisingIdentifier_validateIDFA_happy() {
//		//setup
//		Random rand = new Random();
//		String randomString = String.valueOf(rand.nextInt(10000000) + 1);
//		String testAdvertisingId = "TestAdvertisingID" + randomString;
//		//test
//		MobileCore.setAdvertisingIdentifier(testAdvertisingId);
//		assertEquals(1, testableNetworkService.waitAndGetCount(1, TIMEOUT_IN_MS));
//
//		E2ETestableNetworkService.NetworkRequest request =
//			testableNetworkService.getItem(0);
//		//verify
//		assertTrue(request.url.contains("https://identity.com/id?"));
//		assertTrue(request.url.contains(testAdvertisingId));
//	}
//
//	@Test
//	public void test_setAdvertisingIdentifier_valueChanged_syncCallsSentForValidValues() {
//		//setup
//		Random rand = new Random();
//		String testAdvertisingId1 = "TestAdvertisingID" + String.valueOf(rand.nextInt(10000000) + 1);
//		String testAdvertisingId2 = "TestAdvertisingID" + String.valueOf(rand.nextInt(10000000) + 1);
//
//		//test&verify
//		MobileCore.setAdvertisingIdentifier(testAdvertisingId1);
//		assertEquals(1, testableNetworkService.waitAndGetCount(1, TIMEOUT_IN_MS));
//		testableNetworkService.resetNetworkRequestList();
//
//		MobileCore.setAdvertisingIdentifier(null);
//		asyncHelper.waitForAppThreads(1000, false);
//		assertEquals(1, testableNetworkService.waitAndGetCount(1, TIMEOUT_IN_MS));
//		testableNetworkService.resetNetworkRequestList();
//
//		MobileCore.setAdvertisingIdentifier(testAdvertisingId2);
//		assertEquals(1, testableNetworkService.waitAndGetCount(1, TIMEOUT_IN_MS));
//		testableNetworkService.resetNetworkRequestList();
//
//		MobileCore.setAdvertisingIdentifier("");
//		asyncHelper.waitForAppThreads(1000, false);
//		assertEquals(1, testableNetworkService.waitAndGetCount(1, TIMEOUT_IN_MS));
//	}
//
//	@Test
//	public void test_setAdvertisingIdentifier_sameValueTwice_syncsOnlyOnce() {
//		//setup
//		Random rand = new Random();
//		String testAdvertisingId1 = "TestAdvertisingID" + String.valueOf(rand.nextInt(10000000) + 1);
//
//		//test&verify
//		MobileCore.setAdvertisingIdentifier(testAdvertisingId1);
//		assertEquals(1, testableNetworkService.waitAndGetCount(1, TIMEOUT_IN_MS));
//		testableNetworkService.resetNetworkRequestList();
//
//		MobileCore.setAdvertisingIdentifier(testAdvertisingId1);
//		asyncHelper.waitForAppThreads(1000, false);
//		assertEquals(0, testableNetworkService.waitAndGetCount(0));
//	}
//
//	@Test
//	public void test_setAdvertisingIdentifier_sameValueTwice_getIdentifiersReturnsOne() {
//		//setup
//		Random rand = new Random();
//		String testAdvertisingId1 = "TestAdvertisingID" + String.valueOf(rand.nextInt(10000000) + 1);
//
//		//test
//		MobileCore.setAdvertisingIdentifier(testAdvertisingId1);
//		MobileCore.setAdvertisingIdentifier(testAdvertisingId1);
//
//		//verify
//		List<VisitorID> returnedIds = identityTestHelper.getVisitorIDs();
//		assertEquals(1, returnedIds.size());
//		VisitorID visitorID1 = returnedIds.get(0);
//		assertEquals("d_cid_ic", visitorID1.getIdOrigin());
//		assertEquals("DSID_20914", visitorID1.getIdType());
//		assertEquals(testAdvertisingId1, visitorID1.getId());
//		assertEquals(VisitorID.AuthenticationState.AUTHENTICATED, visitorID1.getAuthenticationState());
//	}
//
//	@Test
//	public void test_setAdvertisingIdentifier_newValue_getIdentifiersReturnsOne() {
//		//setup
//		Random rand = new Random();
//		String testAdvertisingId1 = "TestAdvertisingID" + String.valueOf(rand.nextInt(10000000) + 1);
//		String testAdvertisingId2 = "TestAdvertisingID" + String.valueOf(rand.nextInt(10000000) + 1);
//
//		//test
//		MobileCore.setAdvertisingIdentifier(testAdvertisingId1);
//		MobileCore.setAdvertisingIdentifier(testAdvertisingId2);
//
//		//verify
//		List<VisitorID> returnedIds = identityTestHelper.getVisitorIDs();
//		assertEquals(1, returnedIds.size());
//		VisitorID visitorID1 = returnedIds.get(0);
//		assertEquals("d_cid_ic", visitorID1.getIdOrigin());
//		assertEquals("DSID_20914", visitorID1.getIdType());
//		assertEquals(testAdvertisingId2, visitorID1.getId());
//		assertEquals(VisitorID.AuthenticationState.AUTHENTICATED, visitorID1.getAuthenticationState());
//	}
//
//	@Test
//	public void test_setAdvertisingIdentifier_getSdkIdentitiesReturnsCorrectValue() {
//		//setup
//		String ecid = identityTestHelper.getECID();
//		Random rand = new Random();
//		String testAdvertisingId1 = "TestAdvertisingID" + String.valueOf(rand.nextInt(10000000) + 1);
//
//		//test
//		MobileCore.setAdvertisingIdentifier(testAdvertisingId1);
//
//		//verify
//		assertEquals(1, testableNetworkService.waitAndGetCount(1));
//		testableNetworkService.resetNetworkRequestList();
//		List<Map<String, String>> returnedIds = identityTestHelper.getVisitorIDsFromSDKIdentifiers();
//		assertNotNull(returnedIds);
//		assertEquals(2, returnedIds.size());
//		Map<String, String> id0 = returnedIds.get(0);
//		assertEquals(ecid, id0.get(TestConstants.SDK_IDENTIFIERS_VALUE));
//		assertEquals("4", id0.get(TestConstants.SDK_IDENTIFIERS_NAMESPACE));
//		assertEquals("namespaceId", id0.get(TestConstants.SDK_IDENTIFIERS_TYPE));
//
//		Map<String, String> id1 = returnedIds.get(1);
//		assertEquals(testAdvertisingId1, id1.get(TestConstants.SDK_IDENTIFIERS_VALUE));
//		assertEquals("DSID_20914", id1.get(TestConstants.SDK_IDENTIFIERS_NAMESPACE));
//		assertEquals("integrationCode", id1.get(TestConstants.SDK_IDENTIFIERS_TYPE));
//	}
//
//	@Test
//	public void test_getExperienceCloudId_verifyValidMidRetrieval_happy() throws Exception {
//		//setup
//		final String[] storedData = new String[1];
//		final CountDownLatch latch = new CountDownLatch(1);
//		//test
//		AdobeCallback callback = new AdobeCallback <String> () {
//			@Override
//			public void call(String data) {
//				storedData[0] = data;
//				latch.countDown();
//			}
//		};
//		Identity.getExperienceCloudId(callback);
//		latch.await(5, TimeUnit.SECONDS);
//		//verify
//		assertNotNull(storedData[0]);
//		assertEquals(storedData[0].length(), 38);
//	}
//
//	@Test
//	public void test_getIdentifiers_validateReturnedIdentifiers_happy() throws Exception {
//		//setup
//		final CountDownLatch latch = new CountDownLatch(1);
//		final HashMap<String, String> storedData = new HashMap<>();
//		//test
//		Identity.syncIdentifiers(new HashMap<String, String>() {
//			{
//				put("abc", "def");
//				put("123", "456");
//				put("ghi", "jkl");
//			}
//		});
//		AdobeCallback<List<VisitorID>> callback = new AdobeCallback<List<VisitorID>>() {
//			@Override
//			public void call(final List<VisitorID> data) {
//				if (data != null) {
//					for (int i = 0; i < data.size(); i++) {
//						VisitorID currentID = data.get(i);
//						storedData.put(currentID.getIdType(), currentID.getId());
//					}
//				}
//
//				latch.countDown();
//			}
//		};
//
//		Identity.getIdentifiers(callback);
//		latch.await(5, TimeUnit.SECONDS);
//		//verify
//		assertEquals(3, storedData.size());
//		assertEquals(storedData.get("abc"), ("def"));
//		assertEquals(storedData.get("123"), ("456"));
//		assertEquals(storedData.get("ghi"), ("jkl"));
//	}
//
//	@Test
//	public void test_getIdentifiers_invalidIdentifiers_validateReturnedIdentifiers() throws Exception {
//		//setup
//		final CountDownLatch latch = new CountDownLatch(1);
//		final HashMap<String, String> storedData = new HashMap<>();
//		//test
//		Identity.syncIdentifiers(new HashMap<String, String>() {
//			{
//				put("keya", "value1");
//				put("keyb", "");
//				put("", "value3");
//				put(null, "value4");
//				put("keye", null);
//				put("keyf", "value6");
//			}
//		}, VisitorID.AuthenticationState.AUTHENTICATED);
//		AdobeCallback<List<VisitorID>> callback = new AdobeCallback<List<VisitorID>>() {
//			@Override
//			public void call(List<VisitorID> data) {
//				if (data != null) {
//					for (int i = 0; i < data.size(); i++) {
//						VisitorID currentID = data.get(i);
//						storedData.put(currentID.getIdType(), currentID.getId());
//					}
//				}
//
//				latch.countDown();
//			}
//		};
//
//		Identity.getIdentifiers(callback);
//		latch.await(5, TimeUnit.SECONDS);
//		//verify
//		assertEquals(2, storedData.size());
//		assertEquals(storedData.get("keya"), ("value1"));
//		assertEquals(storedData.get("keyf"), ("value6"));
//	}
//
//	@Test
//	public void test_getIdentifiers_afterExtensionRestart_restoresIdentifiers() throws Exception {
//		resetCore();
//
//		// set visitor ids in persistence
//		AndroidLocalStorageService localStorageService = new AndroidLocalStorageService();
//		LocalStorageService.DataStore identityDataStore = localStorageService.getDataStore(IDENTITY_DATA_STORE_NAME);
//		List<VisitorID> testVisitorIds = new ArrayList<>();
//		testVisitorIds.add(new VisitorID("idOrigin1", "idType1", "id1", VisitorID.AuthenticationState.AUTHENTICATED));
//		testVisitorIds.add(new VisitorID("idOrigin2", "idType2", "", VisitorID.AuthenticationState.AUTHENTICATED));
//		testVisitorIds.add(new VisitorID("idOrigin3", "idType3", "id3", VisitorID.AuthenticationState.AUTHENTICATED));
//		testVisitorIds.add(new VisitorID("idOrigin4", "idType4", null, VisitorID.AuthenticationState.UNKNOWN));
//		testVisitorIds.add(new VisitorID("idOrigin5", "ab独角兽", "独角兽", VisitorID.AuthenticationState.LOGGED_OUT));
//		identityDataStore.setString(VISITOR_IDS_KEY, identityTestHelper.stringFromVisitorIdList(testVisitorIds));
//
//		// simulate restart
//		identityTestHelper.resetIdentity();
//
//		//setup
//		final CountDownLatch latch = new CountDownLatch(1);
//		final HashMap<String, String> storedData = new HashMap<>();
//		//test
//		AdobeCallback<List<VisitorID>> callback = new AdobeCallback<List<VisitorID>>() {
//			@Override
//			public void call(List<VisitorID> data) {
//				if (data != null) {
//					for (int i = 0; i < data.size(); i++) {
//						VisitorID currentID = data.get(i);
//						storedData.put(currentID.getIdType(), currentID.getId());
//					}
//				}
//
//				latch.countDown();
//			}
//		};
//
//		Identity.getIdentifiers(callback);
//		latch.await(5, TimeUnit.SECONDS);
//		//verify
//		assertEquals(3, storedData.size());
//		assertEquals(storedData.get("idType1"), ("id1"));
//		assertEquals(storedData.get("idType3"), ("id3"));
//		assertEquals(storedData.get("ab"), ("独角兽"));
//
//		// cleanup after test
//		identityDataStore.removeAll();
//	}
//
//	@Test
//	public void test_getIdentifiers_afterExtensionRestart_removesVisitorIdsWithDuplicatedIdTypes() throws Exception {
//		resetCore();
//
//		// set visitor ids in persistence
//		AndroidLocalStorageService localStorageService = new AndroidLocalStorageService();
//		LocalStorageService.DataStore identityDataStore = localStorageService.getDataStore(IDENTITY_DATA_STORE_NAME);
//		List<VisitorID> testVisitorIds = new ArrayList<>();
//		testVisitorIds.add(new VisitorID("idOrigin", "idType0", "value0", VisitorID.AuthenticationState.LOGGED_OUT));
//		testVisitorIds.add(new VisitorID("idOrigin", "idType1", "value1", VisitorID.AuthenticationState.AUTHENTICATED));
//		testVisitorIds.add(new VisitorID("idOrigin", "anotherIdType", "value1000", VisitorID.AuthenticationState.UNKNOWN));
//		testVisitorIds.add(new VisitorID("idOrigin", "idType1", "value2", VisitorID.AuthenticationState.LOGGED_OUT));
//		testVisitorIds.add(new VisitorID("idOrigin", "idType1", "value3", VisitorID.AuthenticationState.UNKNOWN));
//		testVisitorIds.add(new VisitorID("idOrigin", "anotherIdType", "value1001",
//										 VisitorID.AuthenticationState.AUTHENTICATED));
//		testVisitorIds.add(new VisitorID("idOrigin", "anotherIdType", "value1002", VisitorID.AuthenticationState.LOGGED_OUT));
//		identityDataStore.setString(VISITOR_IDS_KEY, identityTestHelper.stringFromVisitorIdList(testVisitorIds));
//
//		// simulate restart
//		identityTestHelper.resetIdentity();
//
//		// verify
//		List<VisitorID> returnedIds = identityTestHelper.getVisitorIDs();
//
//		List<VisitorID> expectedVisitorIds = new ArrayList<VisitorID>();
//		expectedVisitorIds.add(new VisitorID("idOrigin", "idType0", "value0", VisitorID.AuthenticationState.LOGGED_OUT));
//		expectedVisitorIds.add(new VisitorID("idOrigin", "idType1", "value3", VisitorID.AuthenticationState.UNKNOWN));
//		expectedVisitorIds.add(new VisitorID("idOrigin", "anotherIdType", "value1002",
//											 VisitorID.AuthenticationState.LOGGED_OUT));
//
//		assertEquals(expectedVisitorIds.size(), returnedIds.size());
//		assertEquals(expectedVisitorIds, returnedIds);
//
//		// test&verify
//		Identity.syncIdentifier("idType1", "value5", VisitorID.AuthenticationState.AUTHENTICATED);
//		assertEquals(1, testableNetworkService.waitAndGetCount(1));
//
//		returnedIds = identityTestHelper.getVisitorIDs();
//		expectedVisitorIds.remove(new VisitorID("idOrigin", "idType1", "value3", VisitorID.AuthenticationState.LOGGED_OUT));
//		expectedVisitorIds.add(new VisitorID("idOrigin", "idType1", "value5", VisitorID.AuthenticationState.AUTHENTICATED));
//		assertEquals(expectedVisitorIds.size(), returnedIds.size());
//		assertEquals(expectedVisitorIds, returnedIds);
//
//		// cleanup after test
//		identityDataStore.removeAll();
//	}
//
//	@Test
//	public void test_syncIdentifiers_afterExtensionRestart_removesDuplicatesWithSameIdType() throws Exception {
//		resetCore();
//
//		// set visitor ids in persistence
//		AndroidLocalStorageService localStorageService = new AndroidLocalStorageService();
//		LocalStorageService.DataStore identityDataStore = localStorageService.getDataStore(IDENTITY_DATA_STORE_NAME);
//		List<VisitorID> testVisitorIds = new ArrayList<>();
//		testVisitorIds.add(new VisitorID("idOrigin1", "idType1", "value1", VisitorID.AuthenticationState.AUTHENTICATED));
//		testVisitorIds.add(new VisitorID("idOrigin1", "idType1", "value2", VisitorID.AuthenticationState.LOGGED_OUT));
//		testVisitorIds.add(new VisitorID("idOrigin1", "idType1", "value3", VisitorID.AuthenticationState.UNKNOWN));
//		identityDataStore.setString(VISITOR_IDS_KEY, identityTestHelper.stringFromVisitorIdList(testVisitorIds));
//
//		// simulate restart
//		identityTestHelper.resetIdentity();
//
//		List<VisitorID> returnedIds = identityTestHelper.getVisitorIDs();
//
//		//verify
//		assertEquals(1, returnedIds.size());
//		VisitorID id = returnedIds.get(0);
//		assertEquals("idType1", id.getIdType());
//		assertEquals("value3", id.getId());
//		assertEquals(VisitorID.AuthenticationState.UNKNOWN, id.getAuthenticationState());
//
//		// cleanup after test
//		identityDataStore.removeAll();
//	}
//
//	@Test
//	public void test_appendToUrl_verifyExperienceCloudIdentifierPresentInUrl() throws Exception {
//		//setup
//		String ecid = identityTestHelper.getECID();
//
//		final String[] storedData = new String[1];
//		final CountDownLatch latch = new CountDownLatch(1);
//		AdobeCallback callback = new AdobeCallback<String>() {
//			@Override
//			public void call(String data) {
//				storedData[0] = data;
//				latch.countDown();
//			}
//		};
//		//test
//		Identity.appendVisitorInfoForURL("http://testURL", callback);
//		latch.await(5, TimeUnit.SECONDS);
//
//		assertTrue(storedData[0].contains("http://testURL"));
//		assertTrue(storedData[0].contains("TS%3D"));
//		assertTrue(storedData[0].contains("MCMID%3D" + ecid));
//		assertTrue(storedData[0].contains("MCORGID%3D972C898555E9F7BC7F000101%40AdobeOrg"));
//	}
//
//	@Test
//	public void test_appendToUrl_passNullUrl_returnsNull() throws Exception {
//		//setup
//		String ecid = identityTestHelper.getECID();
//
//		final String[] storedData = new String[1];
//		final CountDownLatch latch = new CountDownLatch(1);
//		AdobeCallback callback = new AdobeCallback<String>() {
//			@Override
//			public void call(String data) {
//				storedData[0] = data;
//				latch.countDown();
//			}
//		};
//		//test
//		Identity.appendVisitorInfoForURL(null, callback);
//		latch.await(5, TimeUnit.SECONDS);
//
//		assertNull(storedData[0]);
//	}
//
//	@Test
//	public void test_appendToUrl_passEmptyUrl_returnsEmpty() throws Exception {
//		//setup
//		String ecid = identityTestHelper.getECID();
//
//		final String[] storedData = new String[1];
//		final CountDownLatch latch = new CountDownLatch(1);
//		AdobeCallback callback = new AdobeCallback<String>() {
//			@Override
//			public void call(String data) {
//				storedData[0] = data;
//				latch.countDown();
//			}
//		};
//		//test
//		Identity.appendVisitorInfoForURL("", callback);
//		latch.await(5, TimeUnit.SECONDS);
//
//		assertEquals("", storedData[0]);
//	}
//
//	@Test
//	public void test_appendToUrl_passInvalidUrl_returnsAppendedParameters() throws Exception {
//		//setup
//		String ecid = identityTestHelper.getECID();
//
//		final String[] storedData = new String[1];
//		final CountDownLatch latch = new CountDownLatch(1);
//		AdobeCallback callback = new AdobeCallback<String>() {
//			@Override
//			public void call(String data) {
//				storedData[0] = data;
//				latch.countDown();
//			}
//		};
//		//test
//		Identity.appendVisitorInfoForURL("invalid <url> ^^string%", callback);
//		latch.await(5, TimeUnit.SECONDS);
//
//		assertTrue(storedData[0].contains("invalid <url> ^^string%"));
//		assertTrue(storedData[0].contains("TS%3D"));
//		assertTrue(storedData[0].contains("MCMID%3D" + ecid));
//		assertTrue(storedData[0].contains("MCORGID%3D972C898555E9F7BC7F000101%40AdobeOrg"));
//	}
//
//	@Test
//	public void test_appendToUrl_passNullCallback_doesNotThrow() {
//		Identity.appendVisitorInfoForURL("http://testURL", null);
//	}
//
//	@Test
//	public void test_setPushIdentifier_SyncsNewValue() {
//		String token = "D52DB39EEE21395B2B67B895FC478301CE6E936D82521E095902A5E0F57EE0B3";
//		String newToken = "D52DB39EEE21395B2B67B895FC478301CE6E936D82521E095902A5E0F57EE1A3";
//
//		// test
//		MobileCore.setPushIdentifier(token);
//		MobileCore.setPushIdentifier(newToken);
//
//		// verify
//		assertEquals(2, testableNetworkService.waitAndGetCount(2, TIMEOUT_IN_MS));
//		String requestsAsString = testableNetworkService.getAllRequestsAsString();
//		E2ETestableNetworkService.NetworkRequest request0 = testableNetworkService.getItem(0);
//		assertTrue(requestsAsString,
//				   request0.url.contains("d_cid=20919%01D52DB39EEE21395B2B67B895FC478301CE6E936D82521E095902A5E0F57EE0B3"));
//		E2ETestableNetworkService.NetworkRequest request1 = testableNetworkService.getItem(1);
//		assertTrue(requestsAsString,
//				   request1.url.contains("d_cid=20919%01D52DB39EEE21395B2B67B895FC478301CE6E936D82521E095902A5E0F57EE1A3"));
//	}
//
//	@Test
//	public void test_getUrlVariables_VerifyMarketingCloudIdentifiersPresentInVariables() throws Exception {
//		//setup
//		String ecid = identityTestHelper.getECID();
//
//		final String[] storedData = new String[1];
//		final CountDownLatch latch = new CountDownLatch(1);
//
//		Identity.getUrlVariables(new AdobeCallback<String>() {
//			@Override
//			public void call(String s) {
//				storedData[0] = s;
//				latch.countDown();
//			}
//		});
//
//		if (!latch.await(5, TimeUnit.SECONDS)) {
//			fail("Timeout while waiting for getUrlVariables callback.");
//		}
//
//		assertTrue(storedData[0].contains("TS%3D"));
//		assertTrue(storedData[0].contains("MCMID%3D" + ecid));
//		assertTrue(storedData[0].contains("MCORGID%3D972C898555E9F7BC7F000101%40AdobeOrg"));
//		assertFalse(storedData[0].contains("?"));
//		assertFalse(storedData[0].contains("MCAID"));
//		assertFalse(storedData[0].contains("adobe_aa_vid"));
//	}
//
//	@Test
//	public void test_getUrlVariables_setPrivacyOptOut_verifyOrgIdPresentInVariables() throws Exception {
//		MobileCore.setPrivacyStatus(MobilePrivacyStatus.OPT_OUT);
//		identityTestHelper.waitForConfigChange();
//
//		final CountDownLatch latch = new CountDownLatch(1);
//		final String[] storedData = new String[1];
//
//		Identity.getUrlVariables(new AdobeCallback<String>() {
//			@Override
//			public void call(String s) {
//				storedData[0] = s;
//				latch.countDown();
//			}
//		});
//
//		if (!latch.await(5, TimeUnit.SECONDS)) {
//			fail("Timeout while waiting for getUrlVariables callback.");
//		}
//
//		assertTrue(storedData[0].contains("TS%3D"));
//		assertTrue(storedData[0].contains("MCORGID%3D972C898555E9F7BC7F000101%40AdobeOrg"));
//		assertFalse(storedData[0].contains("MCMID"));
//		assertFalse(storedData[0].contains("?"));
//		assertFalse(storedData[0].contains("MCAID"));
//		assertFalse(storedData[0].contains("adobe_aa_vid"));
//	}
//
//	@Test
//	public void test_getUrlVariables_passNullCallback_doesNotThrow()  {
//		Identity.getUrlVariables(null);
//	}
//
//	@Test
//	public void test_syncIdentifier_firstWithNullIdentifier_doesNotCrash() throws InterruptedException {
//		//setup
//		String ecid = identityTestHelper.getECID();
//
//		//test
//		Identity.syncIdentifier("test1234", null, VisitorID.AuthenticationState.UNKNOWN);
//		Identity.syncIdentifier("test1234", "ca", VisitorID.AuthenticationState.UNKNOWN);
//
//		//verify
//		asyncHelper.waitForAppThreads(1000, false);
//		assertEquals(1, testableNetworkService.waitAndGetCount(1, TIMEOUT_IN_MS));
//		E2ETestableNetworkService.NetworkRequest  request = testableNetworkService.getItem(0);
//		assertTrue(request.url.contains("https://identity.com/id?"));
//		assertTrue(request.url.contains("d_cid_ic=test1234%01ca%010"));
//		assertTrue(request.url.contains("ca"));
//		assertTrue(request.url.contains("d_ver=2"));
//		assertTrue(request.url.contains("d_orgid=972C898555E9F7BC7F000101%40AdobeOrg"));
//		assertTrue(request.url.contains("d_mid=" + ecid));
//	}
//
//	@Test
//	public void test_syncIdentifier_withTwoNullIdentifiers_doesNotCrash() throws InterruptedException {
//		// setup
//		String ecid = identityTestHelper.getECID();
//
//		//test
//		Identity.syncIdentifier("test1234", null, VisitorID.AuthenticationState.UNKNOWN);
//		Identity.syncIdentifier("test1234", null, VisitorID.AuthenticationState.UNKNOWN);
//
//		asyncHelper.waitForAppThreads(1000, false);
//		assertEquals(0, testableNetworkService.waitAndGetCount(0));
//	}
//
//	@Test
//	public void test_syncIdentifier_withSecondNullIdentifier_doesNotCrash() throws InterruptedException {
//		//setup
//		String ecid = identityTestHelper.getECID();
//
//		//test
//		Identity.syncIdentifier("test1234", "ca", VisitorID.AuthenticationState.UNKNOWN);
//		Identity.syncIdentifier("test1234", null, VisitorID.AuthenticationState.UNKNOWN);
//
//		//verify
//		asyncHelper.waitForAppThreads(1000, false);
//		assertEquals(1, testableNetworkService.waitAndGetCount(1, TIMEOUT_IN_MS));
//		E2ETestableNetworkService.NetworkRequest  request = testableNetworkService.getItem(0);
//		assertTrue(request.url.contains("https://identity.com/id?"));
//		assertTrue(request.url.contains("d_cid_ic=test1234%01ca%010"));
//		assertTrue(request.url.contains("d_ver=2"));
//		assertTrue(request.url.contains("d_orgid=972C898555E9F7BC7F000101%40AdobeOrg"));
//		assertTrue(request.url.contains("d_mid=" + ecid));
//	}
//
//	@Test
//	public void test_syncIdentifier_withSecondNullIdentifier_clearsIdentifier() throws InterruptedException {
//		//test
//		Identity.syncIdentifier("test1234", "ca", VisitorID.AuthenticationState.UNKNOWN);
//		Identity.syncIdentifier("test1234", null, VisitorID.AuthenticationState.UNKNOWN);
//
//		//verify
//		List<VisitorID> returnedVisitorIds = identityTestHelper.getVisitorIDs();
//		assertNotNull(returnedVisitorIds);
//		assertTrue(returnedVisitorIds.isEmpty());
//	}
//
//	@Test
//	public void test_whenPrivacyChangedToOptout_sendsOptOutRequest() {
//		//setup
//		String ecid = identityTestHelper.getECID();
//
//		//test
//		MobileCore.setPrivacyStatus(MobilePrivacyStatus.OPT_OUT);
//
//		//verify
//		asyncHelper.waitForAppThreads(1000, false);
//		assertEquals(1, testableNetworkService.waitAndGetCount(1));
//		E2ETestableNetworkService.NetworkRequest  request = testableNetworkService.getItem(0);
//		assertEquals(NetworkService.HttpCommand.GET, request.command);
//		assertTrue(request.url.contains("https://identity.com/demoptout.jpg?"));
//		assertTrue(request.url.contains("d_orgid=972C898555E9F7BC7F000101%40AdobeOrg"));
//		assertTrue(request.url.contains("d_mid=" + ecid));
//	}
//
//	@Test
//	public void test_whenPrivacyChangedToOptoutThenOptoutAgain_sendsOptOutRequestOnce() {
//		//setup
//		String ecid = identityTestHelper.getECID();
//
//		//test
//		MobileCore.setPrivacyStatus(MobilePrivacyStatus.OPT_OUT);
//		MobileCore.setPrivacyStatus(MobilePrivacyStatus.OPT_OUT);
//		MobileCore.setPrivacyStatus(MobilePrivacyStatus.OPT_OUT);
//
//		//verify
//		asyncHelper.waitForAppThreads(1000, false);
//		assertEquals(1, testableNetworkService.waitAndGetCount(1));
//		E2ETestableNetworkService.NetworkRequest  request = testableNetworkService.getItem(0);
//		assertTrue(request.url.contains("https://identity.com/demoptout.jpg?"));
//		assertTrue(request.url.contains("d_orgid=972C898555E9F7BC7F000101%40AdobeOrg"));
//		assertTrue(request.url.contains("d_mid=" + ecid));
//	}
//
//	@Test
//	public void test_whenPrivacyChangedToOptoutOptinOptout_sendsOptOutRequestTwice() {
//		//setup
//		String ecid = identityTestHelper.getECID();
//
//		//test first optout
//		MobileCore.setPrivacyStatus(MobilePrivacyStatus.OPT_OUT);
//
//		//verify
//		asyncHelper.waitForAppThreads(1000, false);
//		assertEquals(1, testableNetworkService.waitAndGetCount(1));
//		E2ETestableNetworkService.NetworkRequest  request = testableNetworkService.getItem(0);
//		assertTrue(request.url.contains("https://identity.com/demoptout.jpg?"));
//		assertTrue(request.url.contains("d_orgid=972C898555E9F7BC7F000101%40AdobeOrg"));
//		assertTrue(request.url.contains("d_mid=" + ecid));
//		testableNetworkService.resetTestableNetworkService();
//
//		// test optin (new ecid), then optout
//		MobileCore.setPrivacyStatus(MobilePrivacyStatus.OPT_IN);
//		identityTestHelper.waitForConfigChange();
//		String ecid2 = identityTestHelper.getECID();
//		MobileCore.setPrivacyStatus(MobilePrivacyStatus.OPT_OUT);
//
//		//verify
//		asyncHelper.waitForAppThreads(1000, false);
//		assertEquals(2, testableNetworkService.waitAndGetCount(2)); // one sync, one optout
//		request = testableNetworkService.getItem(1);
//		assertTrue(request.url.contains("https://identity.com/demoptout.jpg?"));
//		assertTrue(request.url.contains("d_orgid=972C898555E9F7BC7F000101%40AdobeOrg"));
//		assertTrue(String.format("Expected mid (%s), but requestUrl was: (%s)", ecid2, request.url),
//				   request.url.contains("d_mid=" + ecid2));
//	}
//
//	@Test
//	public void test_whenPrivacyChangedToUnknownThenOptout_sendsOptOutRequestOnce() {
//		//setup
//		String ecid = identityTestHelper.getECID();
//
//		//test
//		MobileCore.setPrivacyStatus(MobilePrivacyStatus.UNKNOWN);
//		MobileCore.setPrivacyStatus(MobilePrivacyStatus.OPT_OUT);
//
//		//verify
//		asyncHelper.waitForAppThreads(1000, false);
//		assertEquals(1, testableNetworkService.waitAndGetCount(1));
//		E2ETestableNetworkService.NetworkRequest  request = testableNetworkService.getItem(0);
//		assertTrue(request.url.contains("https://identity.com/demoptout.jpg?"));
//		assertTrue(request.url.contains("d_orgid=972C898555E9F7BC7F000101%40AdobeOrg"));
//		assertTrue(request.url.contains("d_mid=" + ecid));
//	}
//
//	void resetCore() {
//		MobileCore.setCore(null);
//		asyncHelper.waitForAppThreads(1000, false);
//		TestingPlatform testingPlatform = new TestingPlatform();
//		testableNetworkService = testingPlatform.e2EAndroidNetworkService;
//		MobileCore.setPlatformServices(testingPlatform);
//
//		MobileCore.setApplication(this.defaultApplication);
//		testableNetworkService.resetTestableNetworkService();
//		identityTestHelper.resetTestableNetworkService(testableNetworkService);
//	}
}
