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
//package com.adobe.marketing.mobile.identity;
//
//import androidx.test.ext.junit.runners.AndroidJUnit4;
//
//import org.junit.After;
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.TimeUnit;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertFalse;
//
//import com.adobe.marketing.mobile.AdobeCallbackWithError;
//import com.adobe.marketing.mobile.AdobeError;
//import com.adobe.marketing.mobile.Identity;
//import com.adobe.marketing.mobile.LoggingMode;
//import com.adobe.marketing.mobile.MobileCore;
//import com.adobe.marketing.mobile.VisitorID;
//
//@RunWith(AndroidJUnit4.class)
//public class IdentityErrorCallbackFunctionalTests extends AbstractE2ETest {
//	private static final String LOG_TAG = "IdentityErrorCallbackFunctionalTests";
//
//	private IdentityTestHelper identityTestHelper;
//
//	@Before
//	public void setUp() {
//		super.setUp();
//		identityTestHelper = new IdentityTestHelper(this.testableNetworkService);
//		MobileCore.setLogLevel(LoggingMode.VERBOSE);
//		MobileCore.setApplication(defaultApplication);
//	}
//
//	@After
//	public void tearDown() {
//		super.tearDown();
//		resetCore();
//		Identity.resetIdentityCore();
//	}
//
//	@Test
//	public void test_appendVisitorInfoForURL_whenInvalidConfig_returnsCallbackTimeoutError() throws InterruptedException {
//		final AdobeError[] storedAdobeError = new AdobeError[1];
//		final CountDownLatch latch = new CountDownLatch(1);
//		final boolean[] wasCalled = new boolean[1];
//		AdobeCallbackWithError<String> callback = new AdobeCallbackWithError<String>() {
//			@Override
//			public void call(final String data) {
//				wasCalled[0] = true;
//			}
//
//			@Override
//			public void fail(final AdobeError adobeError) {
//				storedAdobeError[0] = adobeError;
//				latch.countDown();
//			}
//		};
//
//		//test
//		registerIdentityAndStartCore();
//		blockEventProcessing();
//		Identity.appendVisitorInfoForURL("http://testURL", callback);
//		latch.await(600, TimeUnit.MILLISECONDS); // callback timeout is currently set to 500
//
//		// verify
//		assertFalse(wasCalled[0]);
//		assertEquals(AdobeError.CALLBACK_TIMEOUT, storedAdobeError[0]);
//	}
//
//	@Test
//	public void test_appendVisitorInfoForURL_whenIdentityNotRegistered_returnsCallbackExtensionNotInitializedError() throws
//		InterruptedException {
//		final AdobeError[] storedAdobeError = new AdobeError[1];
//		final CountDownLatch errorLatch = new CountDownLatch(1);
//		final boolean[] wasCalled = new boolean[1];
//		AdobeCallbackWithError<String> callback = new AdobeCallbackWithError<String>() {
//			@Override
//			public void call(final String data) {
//				wasCalled[0] = true;
//			}
//
//			@Override
//			public void fail(final AdobeError adobeError) {
//				storedAdobeError[0] = adobeError;
//				errorLatch.countDown();
//			}
//		};
//
//		//test
//		startCore();
//		Identity.appendVisitorInfoForURL("http://testURL", callback);
//		errorLatch.await(600, TimeUnit.MILLISECONDS); // callback timeout is currently set to 500
//
//		// verify
//		assertFalse(wasCalled[0]);
//		assertEquals(AdobeError.EXTENSION_NOT_INITIALIZED, storedAdobeError[0]);
//	}
//
//	@Test
//	public void test_getUrlVariables_whenInvalidConfig_returnsCallbackTimeoutError() throws InterruptedException {
//		final AdobeError[] storedAdobeError = new AdobeError[1];
//		final CountDownLatch errorLatch = new CountDownLatch(1);
//		final boolean[] wasCalled = new boolean[1];
//		AdobeCallbackWithError<String> callback = new AdobeCallbackWithError<String>() {
//			@Override
//			public void call(final String data) {
//				wasCalled[0] = true;
//			}
//
//			@Override
//			public void fail(final AdobeError adobeError) {
//				storedAdobeError[0] = adobeError;
//				errorLatch.countDown();
//			}
//		};
//
//		//test
//		registerIdentityAndStartCore();
//		blockEventProcessing();
//		Identity.getUrlVariables(callback);
//		errorLatch.await(600, TimeUnit.MILLISECONDS); // callback timeout is currently set to 500
//
//		// verify
//		assertFalse(wasCalled[0]);
//		assertEquals(AdobeError.CALLBACK_TIMEOUT, storedAdobeError[0]);
//	}
//
//	@Test
//	public void test_getUrlVariables_whenIdentityNotRegistered_returnsCallbackExtensionNotInitializedError() throws
//		InterruptedException {
//		final AdobeError[] storedAdobeError = new AdobeError[1];
//		final CountDownLatch errorLatch = new CountDownLatch(1);
//		final boolean[] wasCalled = new boolean[1];
//		AdobeCallbackWithError<String> callback = new AdobeCallbackWithError<String>() {
//			@Override
//			public void call(final String data) {
//				wasCalled[0] = true;
//			}
//
//			@Override
//			public void fail(final AdobeError adobeError) {
//				storedAdobeError[0] = adobeError;
//				errorLatch.countDown();
//			}
//		};
//		//test
//		startCore();
//		Identity.getUrlVariables(callback);
//		errorLatch.await(600, TimeUnit.MILLISECONDS); // callback timeout is currently set to 500
//
//		// verify
//		assertFalse(wasCalled[0]);
//		assertEquals(AdobeError.EXTENSION_NOT_INITIALIZED, storedAdobeError[0]);
//	}
//
//	@Test
//	public void test_getExperienceCloudId_whenInvalidConfig_returnsCallbackTimeoutError() throws InterruptedException {
//		final AdobeError[] storedAdobeError = new AdobeError[1];
//		final CountDownLatch errorLatch = new CountDownLatch(1);
//		final boolean[] wasCalled = new boolean[1];
//		AdobeCallbackWithError<String> callback = new AdobeCallbackWithError<String>() {
//			@Override
//			public void call(final String data) {
//				wasCalled[0] = true;
//			}
//
//			@Override
//			public void fail(final AdobeError adobeError) {
//				storedAdobeError[0] = adobeError;
//				errorLatch.countDown();
//			}
//		};
//
//		//test
//		registerIdentityAndStartCore();
//		blockEventProcessing();
//		Identity.getExperienceCloudId(callback);
//		errorLatch.await(600, TimeUnit.MILLISECONDS); // callback timeout is currently set to 500
//
//		// verify
//		assertFalse(wasCalled[0]);
//		assertEquals(AdobeError.CALLBACK_TIMEOUT, storedAdobeError[0]);
//	}
//
//	@Test
//	public void test_getExperienceCloudId_whenIdentityNotRegistered_returnsCallbackExtensionNotInitializedError() throws
//		InterruptedException {
//		final AdobeError[] storedAdobeError = new AdobeError[1];
//		final CountDownLatch errorLatch = new CountDownLatch(1);
//		final boolean[] wasCalled = new boolean[1];
//		AdobeCallbackWithError<String> callback = new AdobeCallbackWithError<String>() {
//			@Override
//			public void call(final String data) {
//				wasCalled[0] = true;
//			}
//
//			@Override
//			public void fail(final AdobeError adobeError) {
//				storedAdobeError[0] = adobeError;
//				errorLatch.countDown();
//			}
//		};
//
//		//test
//		startCore();
//		Identity.getExperienceCloudId(callback);
//		errorLatch.await(600, TimeUnit.MILLISECONDS); // callback timeout is currently set to 500
//
//		// verify
//		assertFalse(wasCalled[0]);
//		assertEquals(AdobeError.EXTENSION_NOT_INITIALIZED, storedAdobeError[0]);
//	}
//
//	@Test
//	public void test_getIdentifiers_whenInvalidConfig_returnsCallbackTimeoutError() throws InterruptedException {
//		final AdobeError[] storedAdobeError = new AdobeError[1];
//		final CountDownLatch latch = new CountDownLatch(1);
//		final boolean[] wasCalled = new boolean[1];
//		AdobeCallbackWithError<List<VisitorID>> callback = new AdobeCallbackWithError<List<VisitorID>>() {
//			@Override
//			public void call(final List<VisitorID> data) {
//				wasCalled[0] = true;
//			}
//
//			@Override
//			public void fail(final AdobeError adobeError) {
//				storedAdobeError[0] = adobeError;
//				latch.countDown();
//			}
//		};
//
//		//test
//		registerIdentityAndStartCore();
//		blockEventProcessing();
//		Identity.getIdentifiers(callback);
//		latch.await(600, TimeUnit.MILLISECONDS); // callback timeout is currently set to 500
//
//		// verify
//		assertFalse(wasCalled[0]);
//		assertEquals(AdobeError.CALLBACK_TIMEOUT, storedAdobeError[0]);
//	}
//
//	@Test
//	public void test_getIdentifiers_whenIdentityNotRegistered_returnsCallbackExtensionNotInitializedError() throws
//		InterruptedException {
//		final AdobeError[] storedAdobeError = new AdobeError[1];
//		final CountDownLatch latch = new CountDownLatch(1);
//		final boolean[] wasCalled = new boolean[1];
//		AdobeCallbackWithError<List<VisitorID>> callback = new AdobeCallbackWithError<List<VisitorID>>() {
//			@Override
//			public void call(final List<VisitorID> data) {
//				wasCalled[0] = true;
//			}
//
//			@Override
//			public void fail(final AdobeError adobeError) {
//				storedAdobeError[0] = adobeError;
//				latch.countDown();
//			}
//		};
//
//		//test
//		startCore();
//		Identity.getIdentifiers(callback);
//		latch.await(600, TimeUnit.MILLISECONDS); // callback timeout is currently set to 500
//
//		// verify
//		assertFalse(wasCalled[0]);
//		assertEquals(AdobeError.EXTENSION_NOT_INITIALIZED, storedAdobeError[0]);
//	}
//
//	/**
//	 * Helper method for blocking the Identity event processing queue
//	 */
//	private void blockEventProcessing() {
//		// setup invalid Identity configuration
//		Map<String, Object> data = new HashMap<>();
//		data.put("experienceCloud.org", null);
//		MobileCore.updateConfiguration(data);
//		identityTestHelper.waitForConfigChange();
//
//		// add a sync call which will block the events queue processing when config is invalid
//		Identity.syncIdentifier("testType", "testId", VisitorID.AuthenticationState.AUTHENTICATED);
//	}
//
//	private void registerIdentityAndStartCore() {
//		try {
//			Identity.registerExtension();
//		} catch (InvalidInitException e) {
//			Assert.fail("Identity extension initialization failed");
//			e.printStackTrace();
//		}
//
//		MobileCore.start(null);
//	}
//
//	private void startCore() {
//		// skip registration part
//
//		MobileCore.start(null);
//	}
//
//	private void resetCore() {
//		MobileCore.setCore(null);
//		MobileCore.setApplication(this.defaultApplication);
//		Identity.resetIdentityCore();
//	}
//}
