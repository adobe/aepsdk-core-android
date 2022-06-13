///* ***********************************************************************
// * ADOBE CONFIDENTIAL
// * ___________________
// *
// * Copyright 2018 Adobe Systems Incorporated
// * All Rights Reserved.
// *
// * NOTICE:  All information contained herein is, and remains
// * the property of Adobe Systems Incorporated and its suppliers,
// * if any.  The intellectual and technical concepts contained
// * herein are proprietary to Adobe Systems Incorporated and its
// * suppliers and are protected by trade secret or copyright law.
// * Dissemination of this information or reproduction of this material
// * is strictly forbidden unless prior written permission is obtained
// * from Adobe Systems Incorporated.
// **************************************************************************/
//
//package com.adobe.marketing.mobile;
//
//import org.junit.Before;
//import org.junit.Test;
//
//import java.net.HttpURLConnection;
//import java.util.concurrent.TimeUnit;
//
//import static org.junit.Assert.*;
//
//
//public class SignalHitsDatabaseTest extends BaseTest {
//
//	private SignalHitsDatabase signalHitsDatabase;
//	private MockNetworkService networkService;
//	private MockHitQueue<SignalHit, SignalHitSchema> hitQueue;
//
//	@Before
//	public void setup() {
//		super.beforeEach();
//		hitQueue = new MockHitQueue<SignalHit, SignalHitSchema>(platformServices);
//		networkService = platformServices.getMockNetworkService();
//		signalHitsDatabase = new SignalHitsDatabase(platformServices, hitQueue);
//	}
//
//	@Test
//	public void testProcess_NotRetry_When_ConnectionIsNull() throws Exception {
//		SignalHit signalHit = createHit("id", 3000, "serverName2.com", "body", "GET", 5);
//		networkService.connectUrlReturnValue = null;
//
//		HitQueue.RetryType retryType = signalHitsDatabase.process(signalHit);
//		assertEquals(HitQueue.RetryType.NO, retryType);
//	}
//
//
//	@Test
//	public void testProcess_NotRetry_When_ResponseIsValid200OK() throws Exception {
//		SignalHit signalHit = createHit("id", 3000, "serverName2.com", "body", "GET", 5);
//		MockConnection mockConnection = new MockConnection("", 200, null,
//				null);
//		networkService.connectUrlReturnValue = mockConnection;
//
//		HitQueue.RetryType retryType = signalHitsDatabase.process(signalHit);
//		assertEquals(HitQueue.RetryType.NO, retryType);
//	}
//
//	@Test
//	public void testProcess_NotRetry_When_ResponseIsValid2xx() throws Exception {
//		SignalHit signalHit = createHit("id", 3000, "serverName2.com", "body", "GET", 5);
//		MockConnection mockConnection = new MockConnection("", 206, null,
//				null);
//		networkService.connectUrlReturnValue = mockConnection;
//
//		HitQueue.RetryType retryType = signalHitsDatabase.process(signalHit);
//		assertEquals(HitQueue.RetryType.NO, retryType);
//	}
//
//	@Test
//	public void testProcess_Retry_When_ConnectionTimeOout() throws Exception {
//		SignalHit signalHit = createHit("id", 3000, "serverName2.com", "body", "GET", 5);
//		MockConnection mockConnection = new MockConnection("", HttpURLConnection.HTTP_CLIENT_TIMEOUT, null,
//				null);
//		networkService.connectUrlReturnValue = mockConnection;
//
//		HitQueue.RetryType retryType = signalHitsDatabase.process(signalHit);
//		assertEquals(HitQueue.RetryType.YES, retryType);
//	}
//
//	@Test
//	public void testProcess_Retry_When_GateWayOout() throws Exception {
//		SignalHit signalHit = createHit("id", 3000, "serverName2.com", "body", "GET", 5);
//		MockConnection mockConnection = new MockConnection("", HttpURLConnection.HTTP_GATEWAY_TIMEOUT, null,
//				null);
//		networkService.connectUrlReturnValue = mockConnection;
//
//		HitQueue.RetryType retryType = signalHitsDatabase.process(signalHit);
//		assertEquals(HitQueue.RetryType.YES, retryType);
//	}
//
//
//	@Test
//	public void testProcess_Retry_When_HttpUnavailable() throws Exception {
//		SignalHit signalHit = createHit("id", 3000, "serverName2.com", "body", "GET", 5);
//		MockConnection mockConnection = new MockConnection("", HttpURLConnection.HTTP_UNAVAILABLE, null,
//				null);
//		networkService.connectUrlReturnValue = mockConnection;
//
//		HitQueue.RetryType retryType = signalHitsDatabase.process(signalHit);
//		assertEquals(HitQueue.RetryType.YES, retryType);
//	}
//
//	@Test
//	public void testProcess_NotRetry_When_OtherResponseCode() throws Exception {
//		SignalHit signalHit = createHit("id", 3000, "serverName2.com", "body", "GET", 5);
//		MockConnection mockConnection = new MockConnection("", 301, null,
//				null);
//		networkService.connectUrlReturnValue = mockConnection;
//
//		HitQueue.RetryType retryType = signalHitsDatabase.process(signalHit);
//		assertEquals(HitQueue.RetryType.NO, retryType);
//	}
//
//	@Test
//	public void testQueue_Happy_PrivacyStatusIsOptIn() throws Exception {
//		SignalHit signalHit = createHit("id", 3000, "serverName2.com", "body", "GET", 5);
//		SignalHitsDatabase hitsDatabase = new SignalHitsDatabase(platformServices, hitQueue);
//		long currentTimestamp = System.currentTimeMillis();
//
//		// test
//		hitsDatabase.queue(signalHit, currentTimestamp, MobilePrivacyStatus.OPT_IN);
//
//		// verify
//		assertTrue(hitQueue.queueWasCalled);
//		assertEquals("id", hitQueue.queueParametersHit.identifier);
//		assertEquals(TimeUnit.MILLISECONDS.toSeconds(currentTimestamp), hitQueue.queueParametersHit.timestamp);
//		assertEquals("body", hitQueue.queueParametersHit.body);
//		assertEquals("serverName2.com", hitQueue.queueParametersHit.url);
//		assertEquals(5, hitQueue.queueParametersHit.timeout);
//		assertTrue(hitQueue.bringOnlineWasCalled);
//	}
//
//	@Test
//	public void testQueue_NullHit() throws Exception {
//		SignalHitsDatabase hitsDatabase = new SignalHitsDatabase(platformServices, hitQueue);
//		long currentTimestamp = System.currentTimeMillis();
//
//		// test
//		hitsDatabase.queue(null, currentTimestamp, MobilePrivacyStatus.OPT_IN);
//
//		// verify
//		assertTrue(hitQueue.queueWasCalled);
//		assertNull(hitQueue.queueParametersHit);
//		assertTrue(hitQueue.bringOnlineWasCalled);
//	}
//
//	@Test
//	public void testQueue_When_PrivacyStatusUnknown() throws Exception {
//		SignalHit signalHit = createHit("id", 3000, "serverName2.com", "body", "GET", 5);
//		SignalHitsDatabase hitsDatabase = new SignalHitsDatabase(platformServices, hitQueue);
//		long currentTimestamp = System.currentTimeMillis();
//
//		// test
//		hitsDatabase.queue(signalHit, currentTimestamp, MobilePrivacyStatus.UNKNOWN);
//
//		// verify
//		assertTrue(hitQueue.queueWasCalled);
//		assertFalse(hitQueue.bringOnlineWasCalled);
//	}
//
//	@Test
//	public void testQueue_When_PrivacyStatusOptOut() throws Exception {
//		SignalHit signalHit = createHit("id", 3000, "serverName2.com", "body", "GET", 5);
//		SignalHitsDatabase hitsDatabase = new SignalHitsDatabase(platformServices, hitQueue);
//		long currentTimestamp = System.currentTimeMillis();
//
//		// test
//		hitsDatabase.queue(signalHit, currentTimestamp, MobilePrivacyStatus.OPT_OUT);
//
//		// verify
//		assertTrue(hitQueue.queueWasCalled);
//		assertFalse(hitQueue.bringOnlineWasCalled);
//	}
//
//	private SignalHit createHit(String identifier, long timeStamp, String url, String body,
//								String contentType, int timeout) {
//		SignalHit newHit = new SignalHit();
//		newHit.identifier = identifier;
//		newHit.url = url;
//		newHit.body = body;
//		newHit.contentType = contentType;
//		newHit.timestamp = timeStamp;
//		newHit.timeout = timeout;
//		return newHit;
//	}
//}