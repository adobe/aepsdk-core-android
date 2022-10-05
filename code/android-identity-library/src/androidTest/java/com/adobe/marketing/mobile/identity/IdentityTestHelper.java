///* ************************************************************************
// * ADOBE CONFIDENTIAL
// * ___________________
// *
// * Copyright 2020 Adobe
// * All Rights Reserved.
// *
// * NOTICE: All information contained herein is, and remains
// * the property of Adobe and its suppliers, if any. The intellectual
// * and technical concepts contained herein are proprietary to Adobe
// * and its suppliers and are protected by all applicable intellectual
// * property laws, including trade secret and copyright laws.
// * Dissemination of this information or reproduction of this material
// * is strictly forbidden unless prior written permission is obtained
// * from Adobe.
// **************************************************************************/
//
//
//package com.adobe.marketing.mobile.identity;
//
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//import org.junit.Assert;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.TimeUnit;
//
//import static org.junit.Assert.assertEquals;
//
//import com.adobe.marketing.mobile.AdobeCallback;
//import com.adobe.marketing.mobile.Identity;
//import com.adobe.marketing.mobile.LoggingMode;
//import com.adobe.marketing.mobile.MobileCore;
//import com.adobe.marketing.mobile.MobilePrivacyStatus;
//import com.adobe.marketing.mobile.VisitorID;
//
//public class IdentityTestHelper {
//	private static final String LOG_TAG = "IdentityTestHelper";
//	private static final String CID_DELIMITER = "%01";
//	private AsyncHelper asyncHelper = new AsyncHelper();
//	private E2ETestableNetworkService testableNetworkService;
//
//	IdentityTestHelper(E2ETestableNetworkService networkService) {
//		testableNetworkService = networkService;
//	}
//
//	String stringFromVisitorIdList(final List<VisitorID> visitorIDs) {
//		if (visitorIDs == null) {
//			return "";
//		}
//
//		final StringBuilder customerIdString = new StringBuilder();
//
//		for (final VisitorID visitorID : visitorIDs) {
//			customerIdString.append("&");
//			customerIdString.append(IdentityConstants.UrlKeys.VISITOR_ID);
//			customerIdString.append("=");
//			customerIdString.append(visitorID.getIdType());
//			customerIdString.append(CID_DELIMITER);
//
//			if (visitorID.getId() != null) {
//				customerIdString.append(visitorID.getId());
//			}
//
//			customerIdString.append(CID_DELIMITER);
//			customerIdString.append(visitorID.getAuthenticationState().getValue());
//		}
//
//		return customerIdString.toString();
//	}
//
//	void setupIdentityModuleForE2E(String mid) {
//		String idSyncMatcher = "d_cid_ic=type%01value%011";
//		E2ERequestMatcher networkMatcher = new E2ERequestMatcher(idSyncMatcher);
//		String json =
//			"{\"d_mid\":" + "\"" + mid + "\"" +
//			",\"id_sync_ttl\":604800,\"d_blob\":\"hmk_Lq6TPIBMW925SPhw3Q\",\"dcs_region\":9,\"d_ottl\":7200,\"ibs\":[],\"subdomain\":\"obumobile5\",\"tid\":\"d47JfAKTTsU=\"}";
//		E2ETestableNetworkService.NetworkResponse networkResponse = new E2ETestableNetworkService.NetworkResponse(json, 200,
//				null);
//		testableNetworkService.setResponse(networkMatcher, networkResponse);
//		Identity.syncIdentifier("type", "value", VisitorID.AuthenticationState.AUTHENTICATED);
//		assertEquals(1, testableNetworkService.waitAndGetCount(1, 5000));
//		asyncHelper.waitForAppThreads(0, true);
//		testableNetworkService.resetNetworkRequestList();;
//	}
//
//	String getECID() {
//		final String[] result = new String[1];
//		final CountDownLatch latch = new CountDownLatch(1);
//		Identity.getExperienceCloudId(new AdobeCallback<String>() {
//			@Override
//			public void call(String s) {
//				result[0] = s;
//				latch.countDown();
//			}
//		});
//
//		try {
//			if (!latch.await(5, TimeUnit.SECONDS)) {
//				Log.error(LOG_TAG, "Timeout met while waiting for getExperienceCloudId callback.");
//				return null;
//			}
//		} catch (InterruptedException e) {
//			Log.error(LOG_TAG, "Exception waiting for getExperienceCloudId callback.", e);
//			return null;
//		}
//
//		return result[0];
//	}
//
//	/**
//	 * Helper method for fetching the VisitorIds list synchronously for testing, by calling the
//	 * {@code Identity.getIdentifiers()} API
//	 */
//	List<VisitorID> getVisitorIDs() {
//		final List<VisitorID> returnedVisitorIds = new ArrayList<VisitorID>();
//		final CountDownLatch latch = new CountDownLatch(1);
//		Identity.getIdentifiers(new AdobeCallback<List<VisitorID>>() {
//			@Override
//			public void call(final List<VisitorID> visitorIds) {
//				if (visitorIds != null) {
//					returnedVisitorIds.addAll(visitorIds);
//				}
//
//				latch.countDown();
//			}
//		});
//
//		try {
//			if (!latch.await(5, TimeUnit.SECONDS)) {
//				Assert.fail("Timeout met while waiting for the getIdentifiers callback.");
//				return null;
//			}
//		} catch (InterruptedException e) {
//			Assert.fail("Exception waiting for the getIdentifiers callback.");
//			return null;
//		}
//
//		return returnedVisitorIds;
//	}
//
//	/**
//	 * Helper method for fetching the VisitorIds list synchronously for testing, by calling the
//	 * {@code MobileCore.getSdkIdentities()} API. It returns a List of ids, where each id has the
//	 * elements: namespace, value, type
//	 */
//	List<Map<String, String>> getVisitorIDsFromSDKIdentifiers() {
//		final List<String> getSdkIdentitiesResult = new ArrayList<String>();
//		final CountDownLatch latch = new CountDownLatch(1);
//		MobileCore.getSdkIdentities(new AdobeCallback<String>() {
//			@Override
//			public void call(final String s) {
//				getSdkIdentitiesResult.add(s);
//				latch.countDown();
//			}
//		});
//
//		try {
//			if (!latch.await(5, TimeUnit.SECONDS)) {
//				Assert.fail("Timeout met while waiting for the getSdkIdentities callback.");
//				return null;
//			}
//		} catch (InterruptedException e) {
//			Assert.fail("Exception waiting for the getSdkIdentities callback.");
//			return null;
//		}
//
//		if (getSdkIdentitiesResult.isEmpty() || getSdkIdentitiesResult.get(0) == null) {
//			return null;
//		}
//
//		MobileCore.log(LoggingMode.VERBOSE, LOG_TAG, "getSdkIdentities API returned " + getSdkIdentitiesResult.get(0));
//
//		JSONArray usersJsonArray = null;
//
//		try {
//			JSONObject jsonObject = new JSONObject(getSdkIdentitiesResult.get(0));
//			usersJsonArray = jsonObject.getJSONArray(TestConstants.SDK_IDENTIFIERS_USERS);
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//
//		if (usersJsonArray == null) {
//			return null;
//		}
//
//		List<Map<String, String>> returnedVisitorIds = new ArrayList<>();
//		final int size = usersJsonArray.length();
//
//		for (int i = 0; i < size; i++) {
//			try {
//				JSONObject elem = usersJsonArray.getJSONObject(i);
//				JSONArray userIDsJsonArray = elem != null ? elem.getJSONArray(TestConstants.SDK_IDENTIFIERS_VISITOR_IDS) : null;
//
//				if (userIDsJsonArray == null) {
//					continue;
//				}
//
//				// found userIds node, iterate and extract the visitor ids
//				for (int j = 0; j < userIDsJsonArray.length(); j++) {
//					JSONObject userId = userIDsJsonArray.getJSONObject(j);
//					Map<String, String> newUserId = new HashMap<String, String>();
//					newUserId.put(TestConstants.SDK_IDENTIFIERS_NAMESPACE, userId.getString(TestConstants.SDK_IDENTIFIERS_NAMESPACE));
//					newUserId.put(TestConstants.SDK_IDENTIFIERS_TYPE, userId.getString(TestConstants.SDK_IDENTIFIERS_TYPE));
//					newUserId.put(TestConstants.SDK_IDENTIFIERS_VALUE, userId.getString(TestConstants.SDK_IDENTIFIERS_VALUE));
//					returnedVisitorIds.add(newUserId);
//				}
//			} catch (JSONException e) {
//				e.printStackTrace();
//			}
//		}
//
//		return returnedVisitorIds;
//	}
//
//	void waitForConfigChange() {
//		final CountDownLatch latch = new CountDownLatch(1);
//		MobileCore.getPrivacyStatus(new AdobeCallback<MobilePrivacyStatus>() {
//			@Override
//			public void call(MobilePrivacyStatus mobilePrivacyStatus) {
//				latch.countDown();
//			}
//		});
//
//		try {
//			latch.await(10, TimeUnit.SECONDS);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//			Assert.fail("Timed out waiting for config change");
//		}
//	}
//
//	/**
//	 * Helper method to reset identity extension during a test;
//	 * requires changes once https://jira.corp.adobe.com/browse/AMSDK-8864 is implemented
//	 */
//	void resetIdentity() {
//
//		try {
//			Identity.registerExtension();
//		} catch (InvalidInitException e) {
//			e.printStackTrace();
//		}
//
//		MobileCore.start(null);
//		asyncHelper.waitForAppThreads(3000, false);
//		assertEquals(1, testableNetworkService.waitAndGetCount(1));
//	}
//
//	/**
//	 * @param testableNetworkService will be overwritten and used in subsequent helper calls
//	 */
//	void resetTestableNetworkService(final E2ETestableNetworkService testableNetworkService) {
//		this.testableNetworkService = testableNetworkService;
//	}
//
//	/**
//	 * Sets the test configuration for Identity
//	 */
//	void setupTestConfiguration() {
//		HashMap<String, Object> data = new HashMap<String, Object>();
//		data.put("global.privacy", "optedin");
//		data.put("experienceCloud.org", "972C898555E9F7BC7F000101@AdobeOrg");
//		data.put("experienceCloud.server", "identity.com");
//		MobileCore.updateConfiguration(data);
//		waitForConfigChange();
//	}
//}
