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
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import android.content.ComponentCallbacks2;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

@SuppressWarnings("all")
@RunWith(MockitoJUnitRunner.Silent.class)
public class AppLifecycleListenerTests {

	@org.junit.Rule
	public TestName name = new TestName();
	AppLifecycleListener appLifecycleListener;

	boolean isOnForeground = false;
	UIService.AppStateListener listener;
	@Before
	public void beforeEach() {
		appLifecycleListener = AppLifecycleListener.getInstance();
		listener = new UIService.AppStateListener() {

			@Override
			public void onForeground() {
				isOnForeground = true;
			}

			@Override
			public void onBackground() {
				isOnForeground = false;
			}
		};
		appLifecycleListener.registerListener(listener);
	}

	@After
	public void afterEach() {
		appLifecycleListener.unregisterListener(listener);
		isOnForeground = false;
		appLifecycleListener.onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN);
	}

	@Test
	public void isOnForeground_When_onActivityResumedIsCalled() {
		appLifecycleListener.onActivityResumed(null);
		assertTrue(isOnForeground);
	}

	@Test
	public void isOnBackground_When_onActivityResumeAndTrimMemoryIsCalled() {
		appLifecycleListener.onActivityResumed(null);
		appLifecycleListener.onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN);
		assertFalse(isOnForeground);
	}

	@Test
	public void isOnForeground_When_onActivityResumeAndTrimMemoryIsCalledWithWrongValue() {
		appLifecycleListener.onActivityResumed(null);
		appLifecycleListener.onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN - 1);
		assertTrue(isOnForeground);
	}

	@Test
	public void isOnForeground_When_onActivityResumeAndTrimMemoryIsCalledThenStartedAndResumed() {
		appLifecycleListener.onActivityResumed(null);
		appLifecycleListener.onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN);
		assertFalse(isOnForeground);
		appLifecycleListener.onActivityResumed(null);
		assertTrue(isOnForeground);
	}

	@Test
	public void isOnForeground_When_onActivityResumedIsCalledRightAfterOnActivityPaused() {
		appLifecycleListener.onActivityResumed(null);
		assertTrue(isOnForeground);
		appLifecycleListener.onActivityResumed(null);
		assertTrue(isOnForeground);
	}


}
