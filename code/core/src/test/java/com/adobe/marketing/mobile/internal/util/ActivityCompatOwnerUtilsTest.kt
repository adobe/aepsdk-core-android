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
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
internal class ActivityCompatOwnerUtilsTest {

    private lateinit var utils: ActivityCompatOwnerUtils

    @Before
    fun setUp() {
        utils = ActivityCompatOwnerUtils()
    }

    // --- attachActivityCompatOwner ---

    @Test
    fun `attach installs proxy on plain Activity with no existing LifecycleOwner`() {
        val activity = Robolectric.buildActivity(Activity::class.java)
            .create().start().resume().get()
        val decorView = activity.window.decorView
        assertNull(decorView.findViewTreeLifecycleOwner())

        utils.attachActivityCompatOwner(activity)

        val owner = decorView.findViewTreeLifecycleOwner()
        assertNotNull(owner)
        assertTrue(owner is ActivityCompatOwner)
    }

    @Test
    fun `attach advances proxy lifecycle to RESUMED`() {
        val activity = Robolectric.buildActivity(Activity::class.java)
            .create().start().resume().get()

        utils.attachActivityCompatOwner(activity)

        val proxy = activity.window.decorView.findViewTreeLifecycleOwner() as ActivityCompatOwner
        assertEquals(Lifecycle.State.RESUMED, proxy.lifecycle.currentState)
    }

    @Test
    fun `second attach retains existing proxy instead of creating a new one`() {
        val activity = Robolectric.buildActivity(Activity::class.java)
            .create().start().resume().get()

        utils.attachActivityCompatOwner(activity)
        val firstProxy = activity.window.decorView.findViewTreeLifecycleOwner()

        utils.attachActivityCompatOwner(activity)
        val secondProxy = activity.window.decorView.findViewTreeLifecycleOwner()

        assertSame(firstProxy, secondProxy)
    }

    @Test
    fun `attach does nothing when host already has a non-proxy LifecycleOwner`() {
        val activity = Robolectric.buildActivity(Activity::class.java)
            .create().start().resume().get()
        val decorView = activity.window.decorView

        val existingOwner = object : LifecycleOwner {
            override val lifecycle: Lifecycle = LifecycleRegistry(this)
        }
        decorView.setViewTreeLifecycleOwner(existingOwner)

        utils.attachActivityCompatOwner(activity)

        assertSame(existingOwner, decorView.findViewTreeLifecycleOwner())
    }

    // --- detachActivityCompatOwner ---

    @Test
    fun `detach after single attach destroys and removes proxy`() {
        val activity = Robolectric.buildActivity(Activity::class.java)
            .create().start().resume().get()
        val decorView = activity.window.decorView

        utils.attachActivityCompatOwner(activity)
        assertNotNull(decorView.findViewTreeLifecycleOwner())

        utils.detachActivityCompatOwner(activity)

        assertNull(decorView.findViewTreeLifecycleOwner())
    }

    @Test
    fun `detach after single attach transitions proxy to DESTROYED`() {
        val activity = Robolectric.buildActivity(Activity::class.java)
            .create().start().resume().get()

        utils.attachActivityCompatOwner(activity)
        val proxy = activity.window.decorView.findViewTreeLifecycleOwner() as ActivityCompatOwner

        utils.detachActivityCompatOwner(activity)

        assertEquals(Lifecycle.State.DESTROYED, proxy.lifecycle.currentState)
    }

    @Test
    fun `first detach after two attaches does not destroy proxy`() {
        val activity = Robolectric.buildActivity(Activity::class.java)
            .create().start().resume().get()
        val decorView = activity.window.decorView

        utils.attachActivityCompatOwner(activity)
        utils.attachActivityCompatOwner(activity)

        utils.detachActivityCompatOwner(activity)

        val owner = decorView.findViewTreeLifecycleOwner()
        assertNotNull(owner)
        assertTrue(owner is ActivityCompatOwner)
        assertEquals(Lifecycle.State.RESUMED, owner.lifecycle.currentState)
    }

    @Test
    fun `second detach after two attaches destroys proxy`() {
        val activity = Robolectric.buildActivity(Activity::class.java)
            .create().start().resume().get()
        val decorView = activity.window.decorView

        utils.attachActivityCompatOwner(activity)
        utils.attachActivityCompatOwner(activity)

        utils.detachActivityCompatOwner(activity)
        utils.detachActivityCompatOwner(activity)

        assertNull(decorView.findViewTreeLifecycleOwner())
    }

    @Test
    fun `detach with no prior attach is a no-op`() {
        val activity = Robolectric.buildActivity(Activity::class.java)
            .create().start().resume().get()

        utils.detachActivityCompatOwner(activity)
    }

    @Test
    fun `detach does nothing when existing LifecycleOwner is not an AEP proxy`() {
        val activity = Robolectric.buildActivity(Activity::class.java)
            .create().start().resume().get()
        val decorView = activity.window.decorView

        val existingOwner = object : LifecycleOwner {
            override val lifecycle: Lifecycle = LifecycleRegistry(this)
        }
        decorView.setViewTreeLifecycleOwner(existingOwner)

        utils.detachActivityCompatOwner(activity)

        assertSame(existingOwner, decorView.findViewTreeLifecycleOwner())
    }

    // --- Full attach-detach-reattach cycle ---

    @Test
    fun `re-attach after full detach creates a new proxy`() {
        val activity = Robolectric.buildActivity(Activity::class.java)
            .create().start().resume().get()
        val decorView = activity.window.decorView

        utils.attachActivityCompatOwner(activity)
        val firstProxy = decorView.findViewTreeLifecycleOwner()
        assertNotNull(firstProxy)

        utils.detachActivityCompatOwner(activity)
        assertNull(decorView.findViewTreeLifecycleOwner())

        utils.attachActivityCompatOwner(activity)
        val secondProxy = decorView.findViewTreeLifecycleOwner()
        assertNotNull(secondProxy)
        assertTrue(secondProxy is ActivityCompatOwner)
        assertNotSame(firstProxy, secondProxy)
        assertEquals(Lifecycle.State.RESUMED, secondProxy.lifecycle.currentState)
    }

    // --- Lifecycle mirroring through utils ---

    @Test
    fun `proxy installed via attach mirrors host pause and resume`() {
        val controller = Robolectric.buildActivity(Activity::class.java)
            .create().start().resume()

        utils.attachActivityCompatOwner(controller.get())
        val proxy = controller.get().window.decorView
            .findViewTreeLifecycleOwner() as ActivityCompatOwner
        assertEquals(Lifecycle.State.RESUMED, proxy.lifecycle.currentState)

        controller.pause()
        assertEquals(Lifecycle.State.STARTED, proxy.lifecycle.currentState)

        controller.resume()
        assertEquals(Lifecycle.State.RESUMED, proxy.lifecycle.currentState)
    }

    @Test
    fun `detach unregisters lifecycle callbacks so host events after destroy do not crash`() {
        val controller = Robolectric.buildActivity(Activity::class.java)
            .create().start().resume()

        utils.attachActivityCompatOwner(controller.get())
        val proxy = controller.get().window.decorView
            .findViewTreeLifecycleOwner() as ActivityCompatOwner

        utils.detachActivityCompatOwner(controller.get())
        assertEquals(Lifecycle.State.DESTROYED, proxy.lifecycle.currentState)

        // If callbacks were not unregistered this would dispatch ON_PAUSE to the
        // DESTROYED LifecycleRegistry, causing an IllegalStateException.
        controller.pause().stop()
    }
}
