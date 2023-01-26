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

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import android.app.Activity;
import android.content.ComponentCallbacks2;
import com.adobe.marketing.mobile.services.AppState;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@SuppressWarnings("all")
@RunWith(MockitoJUnitRunner.Silent.class)
public class AppTests {

    @Mock private Activity mockedActivity;

    private AppStateListener appStateListener;
    private boolean isOnForeground = false;
    private App app;

    @Before
    public void beforeEach() {
        app = App.INSTANCE;
        isOnForeground = false;
        app.registerListener(
                new AppStateListener() {
                    @Override
                    public void onForeground() {
                        isOnForeground = true;
                    }

                    @Override
                    public void onBackground() {
                        isOnForeground = false;
                    }
                });
    }

    @After
    public void afterEach() {
        app.resetInstance();
    }

    @Test
    public void isOnForeground_When_onActivityResumedIsCalled() {
        app.onActivityResumed(mockedActivity);
        assertTrue(isOnForeground);
        assertEquals(AppState.FOREGROUND, app.getAppState());
    }

    @Test
    public void isOnBackground_When_onActivityResumeAndTrimMemoryIsCalled() {
        app.onActivityResumed(mockedActivity);
        app.onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN);
        assertFalse(isOnForeground);
        assertEquals(AppState.BACKGROUND, app.getAppState());
    }

    @Test
    public void isOnForeground_When_onActivityResumeAndTrimMemoryIsCalledWithWrongValue() {
        app.onActivityResumed(mockedActivity);
        app.onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN - 1);
        assertTrue(isOnForeground);
        assertEquals(AppState.FOREGROUND, app.getAppState());
    }

    @Test
    public void isOnForeground_When_onActivityResumeAndTrimMemoryIsCalledThenStartedAndResumed() {
        app.onActivityResumed(mockedActivity);
        app.onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN);
        assertFalse(isOnForeground);
        app.onActivityResumed(mockedActivity);
        assertTrue(isOnForeground);
        assertEquals(AppState.FOREGROUND, app.getAppState());
    }

    @Test
    public void isOnForeground_When_onActivityResumedIsCalledRightAfterOnActivityPaused() {
        app.onActivityResumed(mockedActivity);
        assertTrue(isOnForeground);
        app.onActivityResumed(mockedActivity);
        assertTrue(isOnForeground);
        assertEquals(AppState.FOREGROUND, app.getAppState());
    }
}
