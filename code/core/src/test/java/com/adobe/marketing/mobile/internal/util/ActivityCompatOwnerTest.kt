/*
  Copyright 2024 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.internal.util

import android.app.Activity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.findViewTreeLifecycleOwner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
internal class ActivityCompatOwnerTest {

    private lateinit var owner: ActivityCompatOwner

    @Before
    fun setUp() {
        owner = ActivityCompatOwner()
    }

    // --- Reference counting ---

    @Test
    fun `retain and single release returns true`() {
        owner.retain()
        assertTrue(owner.release())
    }

    @Test
    fun `two retains and first release returns false`() {
        owner.retain()
        owner.retain()
        assertFalse(owner.release())
    }

    @Test
    fun `two retains and second release returns true`() {
        owner.retain()
        owner.retain()
        owner.release()
        assertTrue(owner.release())
    }

    @Test
    fun `release without prior retain returns true`() {
        assertTrue(owner.release())
    }

    @Test
    fun `three retains require three releases before returning true`() {
        owner.retain()
        owner.retain()
        owner.retain()
        assertFalse(owner.release())
        assertFalse(owner.release())
        assertTrue(owner.release())
    }

    // --- Lifecycle state transitions ---

    @Test
    fun `onCreate advances lifecycle to CREATED`() {
        owner.onCreate()
        assertEquals(Lifecycle.State.CREATED, owner.lifecycle.currentState)
    }

    @Test
    fun `bindToHostLifecycle advances lifecycle to RESUMED`() {
        owner.onCreate()
        val activity = Robolectric.buildActivity(Activity::class.java)
            .create().start().resume().get()

        owner.bindToHostLifecycle(activity)

        assertEquals(Lifecycle.State.RESUMED, owner.lifecycle.currentState)
    }

    @Test
    fun `onDestroy from CREATED transitions to DESTROYED`() {
        owner.onCreate()
        owner.onDestroy()
        assertEquals(Lifecycle.State.DESTROYED, owner.lifecycle.currentState)
    }

    @Test
    fun `onDestroy from RESUMED transitions through intermediate states to DESTROYED`() {
        owner.onCreate()
        val activity = Robolectric.buildActivity(Activity::class.java)
            .create().start().resume().get()
        owner.bindToHostLifecycle(activity)
        assertEquals(Lifecycle.State.RESUMED, owner.lifecycle.currentState)

        owner.onDestroy()

        assertEquals(Lifecycle.State.DESTROYED, owner.lifecycle.currentState)
    }

    // --- Host lifecycle mirroring ---

    @Test
    fun `host onPause mirrors to proxy`() {
        owner.onCreate()
        val controller = Robolectric.buildActivity(Activity::class.java)
            .create().start().resume()
        owner.bindToHostLifecycle(controller.get())
        assertEquals(Lifecycle.State.RESUMED, owner.lifecycle.currentState)

        controller.pause()

        assertEquals(Lifecycle.State.STARTED, owner.lifecycle.currentState)
    }

    @Test
    fun `host onStop mirrors to proxy`() {
        owner.onCreate()
        val controller = Robolectric.buildActivity(Activity::class.java)
            .create().start().resume()
        owner.bindToHostLifecycle(controller.get())

        controller.pause().stop()

        assertEquals(Lifecycle.State.CREATED, owner.lifecycle.currentState)
    }

    @Test
    fun `host onResume after pause restores proxy to RESUMED`() {
        owner.onCreate()
        val controller = Robolectric.buildActivity(Activity::class.java)
            .create().start().resume()
        owner.bindToHostLifecycle(controller.get())

        controller.pause()
        assertEquals(Lifecycle.State.STARTED, owner.lifecycle.currentState)

        controller.resume()

        assertEquals(Lifecycle.State.RESUMED, owner.lifecycle.currentState)
    }

    @Test
    fun `host onStart after stop restores proxy to STARTED`() {
        owner.onCreate()
        val controller = Robolectric.buildActivity(Activity::class.java)
            .create().start().resume()
        owner.bindToHostLifecycle(controller.get())

        controller.pause().stop()
        assertEquals(Lifecycle.State.CREATED, owner.lifecycle.currentState)

        controller.start()

        assertEquals(Lifecycle.State.STARTED, owner.lifecycle.currentState)
    }

    @Test
    fun `lifecycle events from a different activity are ignored`() {
        owner.onCreate()
        val hostController = Robolectric.buildActivity(Activity::class.java)
            .create().start().resume()
        owner.bindToHostLifecycle(hostController.get())

        val otherController = Robolectric.buildActivity(Activity::class.java)
            .create().start().resume()
        otherController.pause()

        assertEquals(Lifecycle.State.RESUMED, owner.lifecycle.currentState)
    }

    @Test
    fun `onDestroy unregisters callbacks so host events after destroy do not crash`() {
        owner.onCreate()
        val controller = Robolectric.buildActivity(Activity::class.java)
            .create().start().resume()
        owner.bindToHostLifecycle(controller.get())

        owner.onDestroy()
        assertEquals(Lifecycle.State.DESTROYED, owner.lifecycle.currentState)

        // If callbacks were not unregistered, this would dispatch ON_PAUSE to the
        // DESTROYED LifecycleRegistry, causing an IllegalStateException.
        controller.pause().stop()

        assertEquals(Lifecycle.State.DESTROYED, owner.lifecycle.currentState)
    }

    // --- View tree attachment ---

    @Test
    fun `attachToView sets this owner as ViewTreeLifecycleOwner`() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        val decorView = activity.window.decorView

        owner.onCreate()
        owner.attachToView(decorView)

        assertSame(owner, decorView.findViewTreeLifecycleOwner())
    }

    @Test
    fun `detachFromView clears ViewTreeLifecycleOwner`() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        val decorView = activity.window.decorView

        owner.onCreate()
        owner.attachToView(decorView)
        assertNotNull(decorView.findViewTreeLifecycleOwner())

        owner.detachFromView(decorView)

        assertNull(decorView.findViewTreeLifecycleOwner())
    }

    @Test
    fun `attachToView with null view does not throw`() {
        owner.attachToView(null)
    }

    @Test
    fun `detachFromView with null view does not throw`() {
        owner.detachFromView(null)
    }
}
