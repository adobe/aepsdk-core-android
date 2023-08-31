/*
  Copyright 2023 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.services.ui.vnext.common

import android.app.Activity
import android.content.Context
import android.view.ViewGroup
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import com.adobe.marketing.mobile.services.ui.vnext.InAppMessage
import com.adobe.marketing.mobile.services.ui.vnext.Presentable
import com.adobe.marketing.mobile.services.ui.vnext.PresentationDelegate
import com.adobe.marketing.mobile.services.ui.vnext.PresentationUtilityProvider
import com.adobe.marketing.mobile.services.ui.vnext.message.InAppMessageEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.anyInt
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import kotlin.test.assertEquals

internal class AEPPresentableTest {
    @Mock
    private lateinit var mockPresentation: InAppMessage

    @Mock
    private lateinit var mockPresentationUtilityProvider: PresentationUtilityProvider

    @Mock
    private lateinit var mockPresentationDelegate: PresentationDelegate

    @Mock
    private lateinit var mockAppLifecycleProvider: AppLifecycleProvider

    @Mock
    private lateinit var mockPresentationStateManager: PresentationStateManager

    @Mock
    private lateinit var mockActivity: Activity

    @Mock
    private lateinit var mockViewGroup: ViewGroup

    @Mock
    private lateinit var mockComposeView: ComposeView

    private var mockMainScope: CoroutineScope = CoroutineScope(Dispatchers.Unconfined)

    @Mock
    private lateinit var mockPresentationListener: InAppMessageEventListener

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        `when`(mockPresentation.listener).thenReturn(mockPresentationListener)
    }

    @Test
    fun `Test AEPPresentable#show attaches ComposeView when all conditions are met`() {
        // setup
        val aepPresentableWithGatedDisplay = TestAEPPresentableGatedDisplay(
            mockPresentation,
            mockPresentationUtilityProvider,
            mockPresentationDelegate,
            mockAppLifecycleProvider,
            mockPresentationStateManager,
            mockMainScope
        )

        // simulate initial detached state
        `when`(mockPresentationStateManager.presentableState).thenReturn(mutableStateOf(Presentable.State.DETACHED))
        // simulate that the presentation delegate allows the presentation to be shown
        `when`(mockPresentationDelegate.canShow(aepPresentableWithGatedDisplay)).thenReturn(true)
        // simulate a valid activity being present
        `when`(mockPresentationUtilityProvider.getCurrentActivity()).thenReturn(mockActivity)
        // simulate no existing ComposeView being present
        `when`(mockActivity.findViewById<ComposeView?>(aepPresentableWithGatedDisplay.contentIdentifier)).thenReturn(
            null
        )
        `when`(mockActivity.findViewById<ViewGroup>(eq(android.R.id.content))).thenReturn(
            mockViewGroup
        )

        runTest {
            // test
            aepPresentableWithGatedDisplay.show()

            // verify that the presentation delegate is called because display is gated
            verify(mockPresentationDelegate).canShow(aepPresentableWithGatedDisplay)

            // verify that the lifecycle provider is called to register the listener
            verify(mockAppLifecycleProvider).registerListener(aepPresentableWithGatedDisplay)

            // Verify that the compose view is added to the viewgroup
            verify(mockViewGroup).addView(any<ComposeView>())

            // verify that the listener, delegate, and state manager are notified of content show
            verify(mockPresentationDelegate).onShow(aepPresentableWithGatedDisplay)
            verify(mockPresentationListener).onShow(aepPresentableWithGatedDisplay)
            verify(mockPresentationStateManager).onShown()
        }
    }

    @Test
    fun `Test that AEPPresentable#show does not attach ComposeView when presentation delegate blocks showing`() {
        // setup
        val aepPresentableWithGatedDisplay = TestAEPPresentableGatedDisplay(
            mockPresentation,
            mockPresentationUtilityProvider,
            mockPresentationDelegate,
            mockAppLifecycleProvider,
            mockPresentationStateManager,
            mockMainScope
        )

        // simulate initial detached state
        `when`(mockPresentationStateManager.presentableState).thenReturn(mutableStateOf(Presentable.State.DETACHED))
        // simulate a valid activity being present
        `when`(mockPresentationUtilityProvider.getCurrentActivity()).thenReturn(mockActivity)
        // simulate no existing ComposeView being present
        `when`(mockActivity.findViewById<ComposeView?>(anyInt())).thenReturn(null)
        `when`(mockActivity.findViewById<ViewGroup>(anyInt())).thenReturn(mockViewGroup)

        // simulate that the presentation delegate does not allow the presentation to be shown
        `when`(mockPresentationDelegate.canShow(aepPresentableWithGatedDisplay)).thenReturn(false)

        runTest {
            // test
            aepPresentableWithGatedDisplay.show()

            // verify that the presentation delegate is called because display is gated
            verify(mockPresentationDelegate).canShow(aepPresentableWithGatedDisplay)

            // verify that the lifecycle provider is called to register the listener
            verify(mockAppLifecycleProvider, never()).registerListener(
                aepPresentableWithGatedDisplay
            )

            // Verify that the compose view is never added to the viewgroup
            verify(mockViewGroup, never()).addView(any<ComposeView>())

            // verify that the listener, delegate, and state manager are not notified of anything
            verify(mockPresentationDelegate, never()).onShow(aepPresentableWithGatedDisplay)
            verify(mockPresentationListener, never()).onShow(aepPresentableWithGatedDisplay)
            verify(mockPresentationStateManager, never()).onShown()
        }
    }

    @Test
    fun `Test that AEPPresentable#show succeeds when presentation delegate is null and all conditions are met`() {
        // setup
        val aepPresentableWithGatedDisplay = TestAEPPresentableGatedDisplay(
            mockPresentation,
            mockPresentationUtilityProvider,
            null, // simulate null presentation delegate
            mockAppLifecycleProvider,
            mockPresentationStateManager,
            mockMainScope
        )

        // simulate initial detached state
        `when`(mockPresentationStateManager.presentableState).thenReturn(mutableStateOf(Presentable.State.DETACHED))
        // simulate a valid activity being present
        `when`(mockPresentationUtilityProvider.getCurrentActivity()).thenReturn(mockActivity)
        // simulate no existing ComposeView being present
        `when`(mockActivity.findViewById<ComposeView?>(aepPresentableWithGatedDisplay.contentIdentifier)).thenReturn(
            null
        )
        `when`(mockActivity.findViewById<ViewGroup>(eq(android.R.id.content))).thenReturn(
            mockViewGroup
        )

        runTest {
            // test
            aepPresentableWithGatedDisplay.show()

            // verify that the lifecycle provider is called to register the listener
            verify(mockAppLifecycleProvider).registerListener(aepPresentableWithGatedDisplay)

            // Verify that the compose view is added to the viewgroup
            verify(mockViewGroup).addView(any<ComposeView>())

            // verify that the listener, delegate, and state manager are notified of content show
            verify(mockPresentationListener).onShow(aepPresentableWithGatedDisplay)
            verify(mockPresentationStateManager).onShown()
        }
    }

    @Test
    fun `Test that AEPPresentable#show bails when presentation is already being shown`() {
        // setup
        val aepPresentableWithGatedDisplay = TestAEPPresentableGatedDisplay(
            mockPresentation,
            mockPresentationUtilityProvider,
            mockPresentationDelegate,
            mockAppLifecycleProvider,
            mockPresentationStateManager,
            mockMainScope
        )

        // simulate that the presentation delegate allows the presentation to be shown
        `when`(mockPresentationDelegate.canShow(aepPresentableWithGatedDisplay)).thenReturn(true)
        // simulate a valid activity being present
        `when`(mockPresentationUtilityProvider.getCurrentActivity()).thenReturn(mockActivity)
        // simulate an existing ComposeView being present
        `when`(mockActivity.findViewById<ComposeView?>(aepPresentableWithGatedDisplay.contentIdentifier)).thenReturn(
            mockComposeView
        )
        `when`(mockActivity.findViewById<ViewGroup>(eq(android.R.id.content))).thenReturn(
            mockViewGroup
        )

        // simulate VISIBLE state when show is called
        `when`(mockPresentationStateManager.presentableState).thenReturn(mutableStateOf(Presentable.State.VISIBLE))

        runTest {
            // test
            aepPresentableWithGatedDisplay.show()

            // verify that the presentation delegate is never queried
            verify(mockPresentationDelegate, never()).canShow(aepPresentableWithGatedDisplay)

            // verify that the lifecycle provider is not called to register the listener
            verify(mockAppLifecycleProvider, never()).registerListener(
                aepPresentableWithGatedDisplay
            )

            // Verify that the compose view is not added to the viewgroup
            verify(mockViewGroup, never()).addView(any<ComposeView>())

            // verify that the listener, delegate, and state manager are never notified of anything
            verify(mockPresentationListener, never()).onShow(aepPresentableWithGatedDisplay)
            verify(mockPresentationStateManager, never()).onShown()
        }
    }

    @Test
    fun `Test that AEPPresentable#show bails when composable is already attached`() {
        // setup
        val aepPresentableWithGatedDisplay = TestAEPPresentableGatedDisplay(
            mockPresentation,
            mockPresentationUtilityProvider,
            mockPresentationDelegate,
            mockAppLifecycleProvider,
            mockPresentationStateManager,
            mockMainScope
        )

        // simulate that the presentation delegate allows the presentation to be shown
        `when`(mockPresentationDelegate.canShow(aepPresentableWithGatedDisplay)).thenReturn(true)
        // simulate a valid activity being present
        `when`(mockPresentationUtilityProvider.getCurrentActivity()).thenReturn(mockActivity)
        // simulate no existing ComposeView being present
        `when`(mockActivity.findViewById<ComposeView?>(anyInt())).thenReturn(mockComposeView)
        `when`(mockActivity.findViewById<ViewGroup>(anyInt())).thenReturn(mockViewGroup)

        // simulate VISIBLE state when show is called
        `when`(mockPresentationStateManager.presentableState).thenReturn(mutableStateOf(Presentable.State.VISIBLE))

        runTest {
            // test
            aepPresentableWithGatedDisplay.show()

            // verify that the presentation delegate is never queried
            verify(mockPresentationDelegate, never()).canShow(aepPresentableWithGatedDisplay)

            // verify that the lifecycle provider is not called to register the listener
            verify(mockAppLifecycleProvider, never()).registerListener(
                aepPresentableWithGatedDisplay
            )

            // Verify that the compose view is not added to the viewgroup
            verify(mockViewGroup, never()).addView(any<ComposeView>())

            // verify that the listener, delegate, and state manager are never notified of anything
            verify(mockPresentationListener, never()).onShow(aepPresentableWithGatedDisplay)
            verify(mockPresentationStateManager, never()).onShown()
        }
    }

    @Test
    fun `Test that AEPPresentable#show bails when current activity is null`() {
        // setup
        val aepPresentableWithGatedDisplay = TestAEPPresentableGatedDisplay(
            mockPresentation,
            mockPresentationUtilityProvider,
            mockPresentationDelegate,
            mockAppLifecycleProvider,
            mockPresentationStateManager,
            mockMainScope
        )

        // simulate a null activity being the current activity
        `when`(mockPresentationUtilityProvider.getCurrentActivity()).thenReturn(null)

        runTest {
            // test
            aepPresentableWithGatedDisplay.show()

            // verify that the lifecycle provider is never called to register the listener
            verify(mockAppLifecycleProvider, never()).registerListener(
                aepPresentableWithGatedDisplay
            )

            // verify that the presentation delegate is never queried
            verify(mockPresentationDelegate, never()).canShow(aepPresentableWithGatedDisplay)

            // verify that the listener, delegate, and state manager are never notified of anything
            verify(mockPresentationListener, never()).onShow(aepPresentableWithGatedDisplay)
            verify(mockPresentationDelegate, never()).onShow(aepPresentableWithGatedDisplay)
            verify(mockPresentationStateManager, never()).onShown()
        }
    }

    @Test
    fun `Test that AEPPresentable#dismiss bails when current activity is null`() {
        // setup
        val aepPresentableWithGatedDisplay = TestAEPPresentableGatedDisplay(
            mockPresentation,
            mockPresentationUtilityProvider,
            mockPresentationDelegate,
            mockAppLifecycleProvider,
            mockPresentationStateManager,
            mockMainScope
        )

        // simulate a null activity being the current activity
        `when`(mockPresentationUtilityProvider.getCurrentActivity()).thenReturn(null)

        runTest {
            // test
            aepPresentableWithGatedDisplay.dismiss()

            // verify that the lifecycle provider listener is unregistered
            verify(mockAppLifecycleProvider).unregisterListener(aepPresentableWithGatedDisplay)

            // verify that the listener, delegate, and state manager are never notified of anything
            verify(mockPresentationListener, never()).onDismiss(aepPresentableWithGatedDisplay)
            verify(mockPresentationDelegate, never()).onDismiss(aepPresentableWithGatedDisplay)
            verify(mockPresentationStateManager, never()).onDetached()
        }
    }

    @Test
    fun `Test AEPPresentable#dismiss when no presentable view exists to dismiss`() {
        // setup
        val aepPresentableWithGatedDisplay = TestAEPPresentableGatedDisplay(
            mockPresentation,
            mockPresentationUtilityProvider,
            mockPresentationDelegate,
            mockAppLifecycleProvider,
            mockPresentationStateManager,
            mockMainScope
        )

        // simulate VISIBLE state when show is called
        `when`(mockPresentationStateManager.presentableState).thenReturn(mutableStateOf(Presentable.State.HIDDEN))

        // simulate a valid activity being present
        `when`(mockPresentationUtilityProvider.getCurrentActivity()).thenReturn(mockActivity)

        `when`(mockActivity.findViewById<ViewGroup>(eq(android.R.id.content))).thenReturn(
            mockViewGroup
        )
        // simulate no existing ComposeView being present
        `when`(mockActivity.findViewById<ComposeView?>(aepPresentableWithGatedDisplay.contentIdentifier)).thenReturn(
            null
        )

        runTest {
            // test
            aepPresentableWithGatedDisplay.dismiss()

            // verify that the lifecycle provider listener is unregistered
            verify(mockAppLifecycleProvider).unregisterListener(aepPresentableWithGatedDisplay)

            // verify that nothing is removed from the viewgroup
            verify(mockViewGroup, never()).removeView(any<ComposeView>())

            // verify that the listener, delegate, and state manager are notified of content dismiss
            verify(mockPresentationListener).onDismiss(aepPresentableWithGatedDisplay)
            verify(mockPresentationDelegate).onDismiss(aepPresentableWithGatedDisplay)
            verify(mockPresentationStateManager).onDetached()
        }
    }

    @Test
    fun `Test AEPPresentable#dismiss when all conditions are met`() {
        // setup
        val aepPresentableWithGatedDisplay = TestAEPPresentableGatedDisplay(
            mockPresentation,
            mockPresentationUtilityProvider,
            mockPresentationDelegate,
            mockAppLifecycleProvider,
            mockPresentationStateManager,
            mockMainScope
        )

        // simulate a valid activity being present
        `when`(mockPresentationUtilityProvider.getCurrentActivity()).thenReturn(mockActivity)

        `when`(mockActivity.findViewById<ViewGroup>(eq(android.R.id.content))).thenReturn(
            mockViewGroup
        )
        // simulate an existing ComposeView being present
        `when`(mockActivity.findViewById<ComposeView?>(aepPresentableWithGatedDisplay.contentIdentifier)).thenReturn(
            mockComposeView
        )

        // simulate VISIBLE state when dismiss is called
        `when`(mockPresentationStateManager.presentableState).thenReturn(mutableStateOf(Presentable.State.VISIBLE))

        runTest {
            // test
            aepPresentableWithGatedDisplay.dismiss()

            // verify that the lifecycle provider listener is unregistered
            verify(mockAppLifecycleProvider).unregisterListener(aepPresentableWithGatedDisplay)

            // verify that the compose view is removed from the viewgroup
            verify(mockViewGroup).removeView(mockComposeView)

            // verify that the listener, delegate, and state manager are notified of content dismiss
            verify(mockPresentationListener).onDismiss(aepPresentableWithGatedDisplay)
            verify(mockPresentationDelegate).onDismiss(aepPresentableWithGatedDisplay)
            verify(mockPresentationStateManager).onDetached()
        }
    }

    @Test
    fun `Test AEPPresentable#dismiss bail when already DETACHED`() {
        // setup
        val aepPresentableWithGatedDisplay = TestAEPPresentableGatedDisplay(
            mockPresentation,
            mockPresentationUtilityProvider,
            mockPresentationDelegate,
            mockAppLifecycleProvider,
            mockPresentationStateManager,
            mockMainScope
        )

        // simulate a valid activity being present
        `when`(mockPresentationUtilityProvider.getCurrentActivity()).thenReturn(mockActivity)

        // simulate DETACHED state when dismiss is called
        `when`(mockPresentationStateManager.presentableState).thenReturn(mutableStateOf(Presentable.State.DETACHED))

        runTest {
            // test
            aepPresentableWithGatedDisplay.dismiss()

            // verify that the lifecycle provider listener is unregistered
            verify(mockAppLifecycleProvider).unregisterListener(aepPresentableWithGatedDisplay)

            // verify that viewgroup is never altered
            verify(mockViewGroup, never()).removeView(mockComposeView)

            // verify that the listener, delegate, and state manager are not notified of anything
            verify(mockPresentationListener, never()).onDismiss(aepPresentableWithGatedDisplay)
            verify(mockPresentationDelegate, never()).onDismiss(aepPresentableWithGatedDisplay)
            verify(mockPresentationStateManager, never()).onDetached()
        }
    }

    @Test
    fun `Test that AEPPresentable#hide bails when current presentable state is not visible`() {
        // setup
        val aepPresentableWithGatedDisplay = TestAEPPresentableGatedDisplay(
            mockPresentation,
            mockPresentationUtilityProvider,
            mockPresentationDelegate,
            mockAppLifecycleProvider,
            mockPresentationStateManager,
            mockMainScope
        )

        // simulate the presentable state being not visible
        `when`(mockPresentationStateManager.presentableState).thenReturn(mutableStateOf(Presentable.State.DETACHED))

        runTest {
            // test
            aepPresentableWithGatedDisplay.hide()

            // verify that the lifecycle provider is not changed
            verifyNoInteractions(mockAppLifecycleProvider)

            // verify that the listener, delegate, and state manager are never notified of anything
            verify(mockPresentationListener, never()).onHide(aepPresentableWithGatedDisplay)
            verify(mockPresentationDelegate, never()).onHide(aepPresentableWithGatedDisplay)
            verify(mockPresentationStateManager, never()).onHidden()
        }
    }

    @Test
    fun `Test that AEPPresentable#hide changes presentable state when all conditions are met`() {
        // setup
        val aepPresentableWithGatedDisplay = TestAEPPresentableGatedDisplay(
            mockPresentation,
            mockPresentationUtilityProvider,
            mockPresentationDelegate,
            mockAppLifecycleProvider,
            mockPresentationStateManager,
            mockMainScope
        )

        // simulate the presentable state being visible
        `when`(mockPresentationStateManager.presentableState).thenReturn(mutableStateOf(Presentable.State.VISIBLE))

        runTest {
            // test
            aepPresentableWithGatedDisplay.hide()

            // verify that the lifecycle provider is not changed
            verifyNoInteractions(mockAppLifecycleProvider)

            // verify that the listener, delegate, and state manager are never notified of anything
            verify(mockPresentationListener).onHide(aepPresentableWithGatedDisplay)
            verify(mockPresentationDelegate).onHide(aepPresentableWithGatedDisplay)
            verify(mockPresentationStateManager).onHidden()
        }
    }

    @Test
    fun `Test that AEPPresentable#onActivityResumed attaches to the activity without changing state when no existing view exists`() {
        // setup
        val aepPresentableWithGatedDisplay = TestAEPPresentableGatedDisplay(
            mockPresentation,
            mockPresentationUtilityProvider,
            mockPresentationDelegate,
            mockAppLifecycleProvider,
            mockPresentationStateManager,
            mockMainScope
        )

        // simulate the presentable state being visible
        `when`(mockPresentationStateManager.presentableState).thenReturn(mutableStateOf(Presentable.State.VISIBLE))

        `when`(mockActivity.findViewById<ViewGroup>(eq(android.R.id.content))).thenReturn(
            mockViewGroup
        )
        // simulate no existing ComposeView being present
        `when`(mockActivity.findViewById<ComposeView?>(aepPresentableWithGatedDisplay.contentIdentifier)).thenReturn(
            null
        )

        runTest {
            // test
            aepPresentableWithGatedDisplay.onActivityResumed(mockActivity)

            // verify that the compose view is added to the viewgroup
            verify(mockViewGroup).addView(any<ComposeView>())

            // verify that the lifecycle provider is not changed
            verifyNoInteractions(mockAppLifecycleProvider)

            // verify that the listener, delegate, and state manager are never notified of anything
            verifyNoInteractions(mockPresentationListener)
            verifyNoInteractions(mockPresentationDelegate)
            verify(mockPresentationStateManager, never()).onShown()
        }
    }

    @Test
    fun `Test that AEPPresentable#onActivityResumed attaches to the activity without changing state when compose view exists`() {
        // setup
        val aepPresentableWithGatedDisplay = TestAEPPresentableGatedDisplay(
            mockPresentation,
            mockPresentationUtilityProvider,
            mockPresentationDelegate,
            mockAppLifecycleProvider,
            mockPresentationStateManager,
            mockMainScope
        )

        // simulate the presentable state being visible
        `when`(mockPresentationStateManager.presentableState).thenReturn(mutableStateOf(Presentable.State.VISIBLE))

        `when`(mockActivity.findViewById<ViewGroup>(eq(android.R.id.content))).thenReturn(
            mockViewGroup
        )
        // simulate an existing ComposeView being present
        `when`(mockActivity.findViewById<ComposeView?>(aepPresentableWithGatedDisplay.contentIdentifier)).thenReturn(
            mockComposeView
        )

        runTest {
            // test
            aepPresentableWithGatedDisplay.onActivityResumed(mockActivity)

            // verify that no compose view is added to the viewgroup
            verify(mockViewGroup, never()).addView(mockComposeView)

            // verify that the lifecycle provider is not changed
            verifyNoInteractions(mockAppLifecycleProvider)

            // verify that the listener, delegate, and state manager are never notified of anything
            verifyNoInteractions(mockPresentationListener)
            verifyNoInteractions(mockPresentationDelegate)
            verify(mockPresentationStateManager, never()).onShown()
        }
    }

    @Test
    fun `Test that AEPPresentable#onActivityResumed bails when presentable is HIDDEN`() {
        // setup
        val aepPresentableWithGatedDisplay = TestAEPPresentableGatedDisplay(
            mockPresentation,
            mockPresentationUtilityProvider,
            mockPresentationDelegate,
            mockAppLifecycleProvider,
            mockPresentationStateManager,
            mockMainScope
        )

        // simulate the presentable state being hidden
        `when`(mockPresentationStateManager.presentableState).thenReturn(mutableStateOf(Presentable.State.HIDDEN))

        `when`(mockActivity.findViewById<ViewGroup>(eq(android.R.id.content))).thenReturn(
            mockViewGroup
        )
        // simulate an existing ComposeView being present
        `when`(mockActivity.findViewById<ComposeView?>(aepPresentableWithGatedDisplay.contentIdentifier)).thenReturn(
            mockComposeView
        )

        runTest {
            // test
            aepPresentableWithGatedDisplay.onActivityResumed(mockActivity)

            // verify that no compose view is added to the viewgroup
            verify(mockViewGroup, never()).addView(mockComposeView)

            // verify that the lifecycle provider is not changed
            verifyNoInteractions(mockAppLifecycleProvider)

            // verify that the listener, delegate, and state manager are never notified of anything
            verifyNoInteractions(mockPresentationListener)
            verifyNoInteractions(mockPresentationDelegate)
            verify(mockPresentationStateManager, never()).onShown()
        }
    }

    @Test
    fun `Test that AEPPresentable#onActivityResumed bails when presentable is DETACHED`() {
        // setup
        val aepPresentableWithGatedDisplay = TestAEPPresentableGatedDisplay(
            mockPresentation,
            mockPresentationUtilityProvider,
            mockPresentationDelegate,
            mockAppLifecycleProvider,
            mockPresentationStateManager,
            mockMainScope
        )

        // simulate the presentable state being DETACHED
        `when`(mockPresentationStateManager.presentableState).thenReturn(mutableStateOf(Presentable.State.DETACHED))

        `when`(mockActivity.findViewById<ViewGroup>(eq(android.R.id.content))).thenReturn(
            mockViewGroup
        )

        runTest {
            // test
            aepPresentableWithGatedDisplay.onActivityResumed(mockActivity)

            // verify that no compose view is added to the viewgroup
            verify(mockViewGroup, never()).addView(mockComposeView)

            // verify that the lifecycle provider is not changed
            verifyNoInteractions(mockAppLifecycleProvider)

            // verify that the listener, delegate, and state manager are never notified of anything
            verifyNoInteractions(mockPresentationListener)
            verifyNoInteractions(mockPresentationDelegate)
            verify(mockPresentationStateManager, never()).onShown()
        }
    }

    @Test
    fun `Test that AEPPresentable#onDestroyed detaches compose view from activity without altering state`() {
        // setup
        val aepPresentableWithGatedDisplay = TestAEPPresentableGatedDisplay(
            mockPresentation,
            mockPresentationUtilityProvider,
            mockPresentationDelegate,
            mockAppLifecycleProvider,
            mockPresentationStateManager,
            mockMainScope
        )

        // simulate the presentable state being visible
        `when`(mockPresentationStateManager.presentableState).thenReturn(mutableStateOf(Presentable.State.VISIBLE))

        `when`(mockActivity.findViewById<ViewGroup>(eq(android.R.id.content))).thenReturn(
            mockViewGroup
        )
        // simulate an existing ComposeView being present
        `when`(mockActivity.findViewById<ComposeView?>(aepPresentableWithGatedDisplay.contentIdentifier)).thenReturn(
            mockComposeView
        )

        runTest {
            // test
            aepPresentableWithGatedDisplay.onActivityDestroyed(mockActivity)

            // verify that the compose view is removed from the viewgroup
            verify(mockViewGroup).removeView(mockComposeView)

            verify(mockComposeView).removeAllViews()

            assertEquals(Presentable.State.VISIBLE, aepPresentableWithGatedDisplay.getState())
        }
    }

    @After
    fun tearDown() {
    }

    class TestAEPPresentableGatedDisplay(
        private val presentation: InAppMessage,
        presentationUtilityProvider: PresentationUtilityProvider,
        presentationDelegate: PresentationDelegate?,
        appLifecycleProvider: AppLifecycleProvider,
        presentationStateManager: PresentationStateManager,
        mainScope: CoroutineScope
    ) : AEPPresentable<InAppMessage>(
        presentation,
        presentationUtilityProvider,
        presentationDelegate,
        appLifecycleProvider,
        presentationStateManager,
        mainScope
    ) {
        override fun getPresentation(): InAppMessage {
            return presentation
        }

        override fun getContent(activityContext: Context): ComposeView {
            return ComposeView(activityContext)
        }

        override fun gateDisplay(): Boolean {
            return true
        }
    }
}
