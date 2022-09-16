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

package com.adobe.marketing.mobile.services.internal.context;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import android.app.Activity;
import android.content.ComponentCallbacks2;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import com.adobe.marketing.mobile.services.internal.context.AppLifecycleListener;

@SuppressWarnings("all")
@RunWith(MockitoJUnitRunner.Silent.class)
public class AppLifecycleListenerTests {

	@org.junit.Rule
	public TestName name = new TestName();
	AppLifecycleListener appLifecycleListener;

	@Mock
    private Activity mockedActivity;

	boolean isOnForeground = false;
	AppStateListener listener;
	@Before
	public void beforeEach() {
		appLifecycleListener = AppLifecycleListener.getInstance();
		listener = new AppStateListener() {

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
		appLifecycleListener.onActivityResumed(mockedActivity);
		assertTrue(isOnForeground);
	}

	@Test
	public void isOnBackground_When_onActivityResumeAndTrimMemoryIsCalled() {
		appLifecycleListener.onActivityResumed(mockedActivity);
		appLifecycleListener.onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN);
		assertFalse(isOnForeground);
	}

	@Test
	public void isOnForeground_When_onActivityResumeAndTrimMemoryIsCalledWithWrongValue() {
		appLifecycleListener.onActivityResumed(mockedActivity);
		appLifecycleListener.onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN - 1);
		assertTrue(isOnForeground);
	}

	@Test
	public void isOnForeground_When_onActivityResumeAndTrimMemoryIsCalledThenStartedAndResumed() {
		appLifecycleListener.onActivityResumed(mockedActivity);
		appLifecycleListener.onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN);
		assertFalse(isOnForeground);
		appLifecycleListener.onActivityResumed(mockedActivity);
		assertTrue(isOnForeground);
	}

	@Test
	public void isOnForeground_When_onActivityResumedIsCalledRightAfterOnActivityPaused() {
		appLifecycleListener.onActivityResumed(mockedActivity);
		assertTrue(isOnForeground);
		appLifecycleListener.onActivityResumed(mockedActivity);
		assertTrue(isOnForeground);
	}


}
