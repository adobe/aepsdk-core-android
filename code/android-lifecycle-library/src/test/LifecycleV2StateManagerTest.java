/* **************************************************************************
 *
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 * Copyright 2021 Adobe Systems Incorporated
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Adobe Systems Incorporated and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Adobe Systems Incorporated and its
 * suppliers and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 *
 * *************************************************************************/

package com.adobe.marketing.mobile;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class LifecycleV2StateManagerTest extends BaseTest {
	/**
	 * This should always be > than {@link LifecycleV2Constants#STATE_UPDATE_TIMEOUT_MILLIS}
	 */
	private static final int CALLBACK_TIMEOUT_MS = LifecycleV2Constants.STATE_UPDATE_TIMEOUT_MILLIS + 10;
	private LifecycleV2StateManager stateManager;

	@Before
	public void beforeEach() {
		stateManager = new LifecycleV2StateManager();
	}

	@Test
	public void testUpdateState_whenConsecutiveStart_updatesOnce() {
		final int times = 5;
		final List<Boolean> updates = new ArrayList<Boolean>();
		final CountDownLatch latch = new CountDownLatch(times);
		AdobeCallback<Boolean> callback = new AdobeCallback<Boolean>() {
			@Override
			public void call(final Boolean updated) {
				updates.add(updated);
				latch.countDown();
			}
		};

		for (int i = 0; i < times; i++) {
			stateManager.updateState(LifecycleV2StateManager.State.START, callback);
		}

		try {
			latch.await(CALLBACK_TIMEOUT_MS, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			fail("CountDownLatch not met");
		}

		assertTrue(updates.get(0));

		for (int i = 1; i < times; i++) {
			assertFalse(updates.get(i));
		}
	}

	@Test
	public void testUpdateState_whenConsecutivePause_updatesOnce() {
		final int times = 5;
		final List<Boolean> updates = new ArrayList<Boolean>();
		final CountDownLatch latch = new CountDownLatch(times + 1);
		AdobeCallback<Boolean> callback = new AdobeCallback<Boolean>() {
			@Override
			public void call(final Boolean updated) {
				updates.add(updated);
				latch.countDown();
			}
		};

		stateManager.updateState(LifecycleV2StateManager.State.START, callback);

		for (int i = 0; i < times; i++) {
			stateManager.updateState(LifecycleV2StateManager.State.PAUSE, callback);
		}

		try {
			latch.await(CALLBACK_TIMEOUT_MS, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			fail("CountDownLatch not met");
		}

		assertTrue(updates.get(0));
		assertTrue(updates.get(5));

		for (int i = 1; i < times - 1; i++) {
			assertFalse(updates.get(i));
		}
	}

	@Test
	public void testUpdateState_whenConsecutiveStartPauseStart_updatesOnce() {
		final List<Boolean> updates = new ArrayList<Boolean>();
		final CountDownLatch latch = new CountDownLatch(5);
		AdobeCallback<Boolean> callback = new AdobeCallback<Boolean>() {
			@Override
			public void call(final Boolean updated) {
				updates.add(updated);
				latch.countDown();
			}
		};

		stateManager.updateState(LifecycleV2StateManager.State.START, callback);
		stateManager.updateState(LifecycleV2StateManager.State.PAUSE, callback);
		stateManager.updateState(LifecycleV2StateManager.State.START, callback);
		stateManager.updateState(LifecycleV2StateManager.State.PAUSE, callback);
		stateManager.updateState(LifecycleV2StateManager.State.START, callback);

		try {
			latch.await(CALLBACK_TIMEOUT_MS, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			fail("CountDownLatch not met");
		}

		assertTrue(updates.get(0));

		for (int i = 1; i < 5; i++) {
			assertFalse(updates.get(i));
		}
	}

	@Test
	public void testUpdateState_whenConsecutiveStartPause_updatesCorrectly() {
		final List<Boolean> updates = new ArrayList<Boolean>();
		final CountDownLatch latch1 = new CountDownLatch(2);
		AdobeCallback<Boolean> callback = new AdobeCallback<Boolean>() {
			@Override
			public void call(final Boolean updated) {
				updates.add(updated);
				latch1.countDown();
			}
		};

		stateManager.updateState(LifecycleV2StateManager.State.START, callback);
		stateManager.updateState(LifecycleV2StateManager.State.PAUSE, callback);

		try {
			latch1.await(CALLBACK_TIMEOUT_MS, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			fail("CountDownLatch not met");
		}

		assertTrue(updates.get(0));
		assertTrue(updates.get(1));
		updates.clear();
		final CountDownLatch latch2 = new CountDownLatch(2);

		stateManager.updateState(LifecycleV2StateManager.State.START, callback);
		stateManager.updateState(LifecycleV2StateManager.State.PAUSE, callback);

		try {
			latch2.await(CALLBACK_TIMEOUT_MS, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			fail("CountDownLatch not met");
		}

		assertTrue(updates.get(0));
		assertTrue(updates.get(1));
	}

	@Test
	public void testUpdateState_whenConsecutiveStartPausePause_resetsTimerToSecondPause() {
		final List<Boolean> updates = new ArrayList<Boolean>();
		final CountDownLatch latch1 = new CountDownLatch(3);
		AdobeCallback<Boolean> callback = new AdobeCallback<Boolean>() {
			@Override
			public void call(final Boolean updated) {
				updates.add(updated);
				latch1.countDown();
			}
		};

		stateManager.updateState(LifecycleV2StateManager.State.START, callback);
		stateManager.updateState(LifecycleV2StateManager.State.PAUSE, callback);
		stateManager.updateState(LifecycleV2StateManager.State.PAUSE, callback);

		try {
			latch1.await(CALLBACK_TIMEOUT_MS, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			fail("CountDownLatch not met");
		}

		assertTrue(updates.get(0));
		assertFalse(updates.get(1)); // this update was canceled in favor of last pause update
		assertTrue(updates.get(2));
	}

	@Test
	public void testUpdateState_whenNullParams_doesNotCrash() {
		stateManager.updateState(null, new AdobeCallback<Boolean>() {
			@Override
			public void call(Boolean aBoolean) {

			}
		});

		stateManager.updateState(LifecycleV2StateManager.State.PAUSE, null);
		stateManager.updateState(null, null);
	}
}
