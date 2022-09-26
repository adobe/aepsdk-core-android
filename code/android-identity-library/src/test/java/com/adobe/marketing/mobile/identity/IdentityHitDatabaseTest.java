/* *****************************************************************************
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 * Copyright 2017 Adobe
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains
 * the property of Adobe and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Adobe
 * and its suppliers and are protected by all applicable intellectual
 * property laws, including trade secret and copyright laws.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe.
 ******************************************************************************/

package com.adobe.marketing.mobile.identity;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;

import static org.junit.Assert.*;

import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.EventSource;
import com.adobe.marketing.mobile.EventType;
import com.adobe.marketing.mobile.MobilePrivacyStatus;

public class IdentityHitDatabaseTest extends BaseTest {

	private IdentityHitsDatabase identityHitsDatabase;
	private MockIdentityExtension parentModule;
	private MockNetworkService networkService;
	private MockHitQueue<IdentityHit, IdentityHitSchema> hitQueue;
	private IdentityHit identityHit = createHit("id", 3000, "serverName2.com", "pairId", 3, true);

	@Before
	public void setup() {
		super.beforeEach();
		hitQueue = new MockHitQueue<IdentityHit, IdentityHitSchema>(platformServices);
		parentModule = new MockIdentityExtension(eventHub, platformServices);
		networkService = platformServices.getMockNetworkService();
		identityHitsDatabase = new IdentityHitsDatabase(parentModule, platformServices, hitQueue);
	}

	@Test
	public void testProcess() {
		Event event = new Event.Builder("IdentityExtension Request", EventType.IDENTITY, EventSource.REQUEST_CONTENT)
		.setResponsePairID("pairId").setTimestamp(123456000).build();
		event.setEventNumber(20);

		ConfigurationSharedStateIdentity configSharedState = new ConfigurationSharedStateIdentity();
		configSharedState.privacyStatus = MobilePrivacyStatus.OPT_IN;

		hitQueue.queueReturnValue = true;

		identityHitsDatabase.queue("url", event, configSharedState);
		IdentityHit identityHit = hitQueue.queueParametersHit;
		assertEquals("url", identityHit.url);
		assertEquals(123456, identityHit.timestamp);
		assertEquals(true, identityHit.configSSL);
		assertEquals("pairId", identityHit.pairId);
		assertEquals(20, identityHit.eventNumber);
		assertTrue(hitQueue.bringOnlineWasCalled);
		assertTrue(hitQueue.queueWasCalled);
	}

	@Test
	public void testProcess_NotRetry_When_HitUrlIsNull() {
		// setup
		IdentityHit identityHit = createHit("id", 3000, null, "pariId", 3, false);

		// test
		HitQueue.RetryType retryType = identityHitsDatabase.process(identityHit);

		// verify
		assertEquals(HitQueue.RetryType.NO, retryType);
	}

	@Test
	public void testProcess_MakesTheCorrect_networkConnection_sslFalse() {
		// setup
		IdentityHit identityHit = createHit("id", 3000, "serverName2.com", "pariId", 3, false);
		networkService.connectUrlReturnValue = null;

		// test
		HitQueue.RetryType retryType = identityHitsDatabase.process(identityHit);

		// verify
		assertEquals(HitQueue.RetryType.NO, retryType);
		assertTrue(networkService.connectUrlWasCalled);
		assertEquals(2, networkService.connectUrlParametersRequestProperty.size());
		assertEquals("close", networkService.connectUrlParametersRequestProperty.get("connection"));
		assertEquals(2000, networkService.connectUrlParametersConnectTimeout);
		assertEquals(2000, networkService.connectUrlParametersReadTimeout);
		assertNull(networkService.connectUrlParametersConnectPayload);
		assertEquals(NetworkService.HttpCommand.GET, networkService.connectUrlParametersCommand);
		assertEquals("serverName2.com", networkService.connectUrlParametersUrl);

	}

	@Test
	public void testProcess_MakesTheCorrect_networkConnection_sslTrue() {
		// setup
		networkService.connectUrlReturnValue = null;

		// test
		HitQueue.RetryType retryType = identityHitsDatabase.process(identityHit);

		// verify
		assertEquals(HitQueue.RetryType.NO, retryType);
		assertTrue(networkService.connectUrlWasCalled);
		assertEquals(1, networkService.connectUrlParametersRequestProperty.size());
		assertNull(networkService.connectUrlParametersRequestProperty.get("connection"));
	}

	@Test
	public void testProcess_NotRetry_When_ConnectionIsNull() {
		// setup
		networkService.connectUrlReturnValue = null;

		// test
		HitQueue.RetryType retryType = identityHitsDatabase.process(identityHit);

		// verify
		assertTrue(parentModule.networkResponseLoadedCalled);
		assertNull(parentModule.networkResponseLoadedResult);
		assertEquals(identityHit.pairId, parentModule.networkResponseLoadedEventPairID);
		assertEquals(identityHit.eventNumber, parentModule.networkResponseLoadedEventStateVersion);
		assertEquals(HitQueue.RetryType.NO, retryType);
	}

	@Test
	public void testProcess_NoRetry_When_200OKWithNoInputStream() {

		// setup
		IdentityMockConnection mockConnection = new IdentityMockConnection(HttpURLConnection.HTTP_OK, null);
		networkService.connectUrlReturnValue = mockConnection;

		// test
		HitQueue.RetryType retryType = identityHitsDatabase.process(identityHit);

		// verify
		assertEquals(HitQueue.RetryType.NO, retryType);
		assertTrue(parentModule.networkResponseLoadedCalled);
		assertNull(parentModule.networkResponseLoadedResult);
		assertEquals(identityHit.pairId, parentModule.networkResponseLoadedEventPairID);
		assertEquals(identityHit.eventNumber, parentModule.networkResponseLoadedEventStateVersion);
		assertTrue(mockConnection.getInputStreamCalled);
	}

	@Test
	public void testProcess_Retries_When_RecoverableCodeWithNoInputStream() {

		// setup
		IdentityMockConnection mockConnection = new IdentityMockConnection(HttpURLConnection.HTTP_GATEWAY_TIMEOUT, null);
		networkService.connectUrlReturnValue = mockConnection;

		// test
		HitQueue.RetryType retryType = identityHitsDatabase.process(identityHit);

		// verify
		assertEquals(HitQueue.RetryType.YES, retryType);
		assertFalse(parentModule.networkResponseLoadedCalled);
		assertFalse(mockConnection.getInputStreamCalled);
	}

	@Test
	public void testProcess_NoRetry_When_NotRecoverableCodeWithNoInputStream() {

		// setup
		IdentityMockConnection mockConnection = new IdentityMockConnection(HttpURLConnection.HTTP_ENTITY_TOO_LARGE, null);
		networkService.connectUrlReturnValue = mockConnection;

		// test
		HitQueue.RetryType retryType = identityHitsDatabase.process(identityHit);

		// verify
		assertEquals(HitQueue.RetryType.NO, retryType);
		assertTrue(parentModule.networkResponseLoadedCalled);
		assertNull(parentModule.networkResponseLoadedResult);
		assertEquals(identityHit.pairId, parentModule.networkResponseLoadedEventPairID);
		assertEquals(identityHit.eventNumber, parentModule.networkResponseLoadedEventStateVersion);
		assertFalse(mockConnection.getInputStreamCalled);
	}

	@Test
	public void testProcess_Retries_When_RecoverableCodeWithInputStream() {

		// setup
		String responseMessage = "504 Gateway Timeout";
		IdentityMockConnection mockConnection = new IdentityMockConnection(HttpURLConnection.HTTP_GATEWAY_TIMEOUT,
				new ByteArrayInputStream(responseMessage.getBytes(Charset.forName("UTF-8"))));
		networkService.connectUrlReturnValue = mockConnection;

		// test
		HitQueue.RetryType retryType = identityHitsDatabase.process(identityHit);

		// verify
		assertEquals(HitQueue.RetryType.YES, retryType);
		assertFalse(parentModule.networkResponseLoadedCalled);
		assertFalse(mockConnection.getInputStreamCalled);
	}

	@Test
	public void testProcess_NoRetry_When_NotRecoverableCodeWithInputStream() {

		// setup
		String responseMessage = "Request payload too large";
		IdentityMockConnection mockConnection = new IdentityMockConnection(HttpURLConnection.HTTP_ENTITY_TOO_LARGE,
				new ByteArrayInputStream(responseMessage.getBytes(Charset.forName("UTF-8"))));
		networkService.connectUrlReturnValue = mockConnection;

		// test
		HitQueue.RetryType retryType = identityHitsDatabase.process(identityHit);

		// verify
		assertEquals(HitQueue.RetryType.NO, retryType);
		assertTrue(parentModule.networkResponseLoadedCalled);
		assertNull(parentModule.networkResponseLoadedResult);
		assertEquals(identityHit.pairId, parentModule.networkResponseLoadedEventPairID);
		assertEquals(identityHit.eventNumber, parentModule.networkResponseLoadedEventStateVersion);
		assertFalse(mockConnection.getInputStreamCalled);
	}

	@Test
	public void testProcess_NotRetry_When_ResponseIsValid() {
		// setup
		MockConnection mockConnection = new MockConnection("{\n" +
				"\"d_blob\":\"blob\",\n" +
				"\"dcs_region\":45\n" +
				"}", 200, null,
				null);
		networkService.connectUrlReturnValue = mockConnection;

		// test
		HitQueue.RetryType retryType = identityHitsDatabase.process(identityHit);


		// verify
		assertEquals(HitQueue.RetryType.NO, retryType);
		assertTrue(parentModule.networkResponseLoadedCalled);
		assertEquals(identityHit.pairId, parentModule.networkResponseLoadedEventPairID);
		assertEquals(identityHit.eventNumber, parentModule.networkResponseLoadedEventStateVersion);
		assertEquals("blob", parentModule.networkResponseLoadedResult.blob);
		assertEquals("45", parentModule.networkResponseLoadedResult.hint);
	}

	@Test
	public void testProcess_Retry_When_ConnectionTimeout() {
		// setup
		MockConnection mockConnection = new MockConnection("", HttpURLConnection.HTTP_CLIENT_TIMEOUT, null,
				null);
		networkService.connectUrlReturnValue = mockConnection;

		// test
		HitQueue.RetryType retryType = identityHitsDatabase.process(identityHit);

		// verify
		assertEquals(HitQueue.RetryType.YES, retryType);
		assertFalse(parentModule.networkResponseLoadedCalled);
	}

	@Test
	public void testProcess_Retry_When_Gateway_Timeout() {
		// setup
		MockConnection mockConnection = new MockConnection("", HttpURLConnection.HTTP_GATEWAY_TIMEOUT, null,
				null);
		networkService.connectUrlReturnValue = mockConnection;

		// test
		HitQueue.RetryType retryType = identityHitsDatabase.process(identityHit);

		// verify
		assertEquals(HitQueue.RetryType.YES, retryType);
		assertFalse(parentModule.networkResponseLoadedCalled);
	}


	@Test
	public void testProcess_Retry_When_HttpUnavailable() {
		// setup
		MockConnection mockConnection = new MockConnection("", HttpURLConnection.HTTP_UNAVAILABLE, null,
				null);
		networkService.connectUrlReturnValue = mockConnection;

		// test
		HitQueue.RetryType retryType = identityHitsDatabase.process(identityHit);

		// verify
		assertEquals(HitQueue.RetryType.YES, retryType);
		assertFalse(parentModule.networkResponseLoadedCalled);
	}

	@Test
	public void testProcess_NotRetry_When_OtherResponseCode() {
		// setup
		MockConnection mockConnection = new MockConnection("", 301, null,
				null);
		networkService.connectUrlReturnValue = mockConnection;

		// test
		HitQueue.RetryType retryType = identityHitsDatabase.process(identityHit);

		// verify
		assertEquals(HitQueue.RetryType.NO, retryType);
		assertTrue(parentModule.networkResponseLoadedCalled);
		assertNull(parentModule.networkResponseLoadedResult);
		assertEquals(identityHit.pairId, parentModule.networkResponseLoadedEventPairID);
		assertEquals(identityHit.eventNumber, parentModule.networkResponseLoadedEventStateVersion);
	}


	@Test
	public void testQueue() {
		// setup
		Event event = new Event.Builder("IdentityExtension Request", EventType.IDENTITY, EventSource.REQUEST_CONTENT)
		.setResponsePairID("pairId").setTimestamp(123456000).build();
		event.setEventNumber(20);

		ConfigurationSharedStateIdentity configSharedState = new ConfigurationSharedStateIdentity();
		configSharedState.privacyStatus = MobilePrivacyStatus.OPT_IN;
		hitQueue.queueReturnValue = true;

		// test
		identityHitsDatabase.queue("url", event, configSharedState);

		// verify
		IdentityHit identityHit = hitQueue.queueParametersHit;
		assertEquals("url", identityHit.url);
		assertEquals(123456, identityHit.timestamp);
		assertEquals(true, identityHit.configSSL);
		assertEquals("pairId", identityHit.pairId);
		assertEquals(20, identityHit.eventNumber);
		assertTrue(hitQueue.bringOnlineWasCalled);
		assertTrue(hitQueue.queueWasCalled);
	}

	@Test
	public void testCreateIdentityObjectFromResponseJsonObject_ShouldSetValues() {
		String json = "{\n" +
					  " \"d_mid\": \"11055975576108377572226656299476126353\",\n" +
					  " \"d_optout\": [\"global\"],\n" +
					  " \"id_sync_ttl\": 7200\n" +
					  " }";

		FakeJSONObject fakeJSONObject = (FakeJSONObject) platformServices.getJsonUtilityService().createJSONObject(json);

		IdentityResponseObject result = identityHitsDatabase.createIdentityObjectFromResponseJsonObject(fakeJSONObject);
		assertEquals(7200, result.ttl);
		assertEquals("11055975576108377572226656299476126353", result.mid);
		assertEquals(1, result.optOutList.size());
		assertEquals("global", result.optOutList.get(0));
	}

	@Test
	public void testCreateIdentityObjectFromEmptyResponseJsonObjectCatchesExceptions() {
		String json = "{}";

		FakeJSONObject fakeJSONObject = (FakeJSONObject) platformServices.getJsonUtilityService().createJSONObject(json);

		IdentityResponseObject result = identityHitsDatabase.createIdentityObjectFromResponseJsonObject(fakeJSONObject);
		assertEquals(null, result.mid);
		assertNull(result.optOutList);
	}

	@Test
	public void testUpdatePrivacyStatusToOptInExpectBringOnlineCalled() {
		identityHitsDatabase.updatePrivacyStatus(MobilePrivacyStatus.OPT_IN);
		assertTrue(hitQueue.bringOnlineWasCalled);
		assertFalse(hitQueue.isSuspended());
	}

	@Test
	public void testUpdatePrivacyStatusToOptOutExpectSuspendAndClearAllHitsCalled() {
		identityHitsDatabase.updatePrivacyStatus(MobilePrivacyStatus.OPT_OUT);
		assertTrue(hitQueue.suspendWasCalled);
		assertTrue(hitQueue.deleteAllHitsWasCalled);
	}

	@Test
	public void testUpdatePrivacyStatusToUnknownExpectSuspendCalled() {
		identityHitsDatabase.updatePrivacyStatus(MobilePrivacyStatus.UNKNOWN);
		assertTrue(hitQueue.suspendWasCalled);
		assertFalse(hitQueue.deleteAllHitsWasCalled);
	}

	private IdentityHit createHit(String identifier, long timeStamp, String url, String pairId,
								  int eventNumber, boolean ssl) {
		IdentityHit newHit = new IdentityHit();
		newHit.identifier = identifier;
		newHit.eventNumber = eventNumber;
		newHit.pairId = pairId;
		newHit.url = url;
		newHit.timestamp = timeStamp;
		newHit.configSSL = ssl;
		return newHit;
	}
}

