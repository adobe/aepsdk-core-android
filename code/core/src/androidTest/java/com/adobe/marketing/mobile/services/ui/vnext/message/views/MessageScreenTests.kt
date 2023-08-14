package com.adobe.marketing.mobile.services.ui.vnext.message.views

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performGesture
import androidx.compose.ui.test.swipeDown
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.adobe.marketing.mobile.services.ui.vnext.common.PresentationStateManager
import com.adobe.marketing.mobile.services.ui.vnext.message.GestureTracker
import com.adobe.marketing.mobile.services.ui.vnext.message.InAppMessageSettings
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*

import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MessageTests {
    @get: Rule
    val composeTestRule = createComposeRule()   // compose rule is required to get access to the composable component
    val HTML_TEXT_SAMPLE = "<html>\n" +
            "<head>\n" +
            "<title>A Sample HTML Page</title>\n" +
            "</head>\n" +
            "<body>\n" +
            "\n" +
            "<h1>This is a sample HTML page</h1>\n" +
            "\n" +
            "</body>\n" +
            "</html>"

    private val inAppMessageSettings =  InAppMessageSettings.Builder()
        .backdropOpacity(0.5f)
        .backgroundColor("#000000")
        .cornerRadius(10f)
        .displayAnimation(InAppMessageSettings.MessageAnimation.BOTTOM)
        .dismissAnimation(InAppMessageSettings.MessageAnimation.TOP)
        .height(60)
        .width(80)
        .horizontalAlignment(InAppMessageSettings.MessageAlignment.CENTER)
        .shouldTakeOverUi(false)
        .content(HTML_TEXT_SAMPLE)
        .build()

    private val transitionState = MutableTransitionState(false)
    private var onCreatedCalled = false
    private var onDisposedCalled = false
    private var onBackPressed = false
    private val detectedGestures = mutableListOf<InAppMessageSettings.MessageGesture>()
    private val presentationStateManager = PresentationStateManager()
    private val gestureTracker = GestureTracker(
        acceptedGestures = setOf(InAppMessageSettings.MessageGesture.SWIPE_DOWN),
        onGestureDetected = { gesture -> detectedGestures.add(gesture) },
    )

    @Before
    fun setUp() {
        composeTestRule.setContent {    // setting our composable as content for test
            MessageScreen(
                presentationStateManager = presentationStateManager,
                inAppMessageSettings = inAppMessageSettings,
                onCreated = { onCreatedCalled = true },
                onDisposed = { onDisposedCalled = true },
                onGestureDetected = { gesture -> detectedGestures.add(gesture) },
                onBackPressed = { onBackPressed = true },
            )
        }
    }

    @Test
    fun testMessage() {
        presentationStateManager.onShown()
        composeTestRule.waitForIdle()
        assertTrue(onCreatedCalled)
        composeTestRule.onNodeWithTag("MessageFrame").assertExists()

        composeTestRule.onNodeWithTag("MessageFrame").performGesture {
            swipeDown()
        }

        composeTestRule.waitForIdle()
        assertTrue(detectedGestures.contains(InAppMessageSettings.MessageGesture.SWIPE_DOWN))

        presentationStateManager.onDetached()
        composeTestRule.waitForIdle()

        assertTrue(onDisposedCalled)
    }


    @After
    fun tearDown() {
    }
}