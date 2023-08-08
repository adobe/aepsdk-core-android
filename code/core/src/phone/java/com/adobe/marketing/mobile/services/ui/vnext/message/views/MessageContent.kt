package com.adobe.marketing.mobile.services.ui.vnext.message.views

import android.webkit.WebSettings
import android.webkit.WebView
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.services.ServiceConstants
import com.adobe.marketing.mobile.services.ui.vnext.message.GestureTracker
import com.adobe.marketing.mobile.services.ui.vnext.message.InAppMessageSettings
import java.nio.charset.StandardCharsets
import kotlin.math.abs


@Composable
internal fun MessageContent(
    inAppMessageSettings: InAppMessageSettings,
    onCreated: (WebView) -> Unit,
    gestureTracker: GestureTracker
) {
    val currentConfiguration = LocalConfiguration.current
    val heightDp: Dp =
        ((currentConfiguration.screenHeightDp * inAppMessageSettings.height) / 100).dp
    val widthDp: Dp = ((currentConfiguration.screenWidthDp * inAppMessageSettings.width) / 100).dp
    val offsetX = remember { mutableStateOf(0f) }
    val offsetY = remember { mutableStateOf(0f) }
    val dragVelocity = remember { mutableStateOf(0f) }


    AndroidView(
        factory = {
            WebView(it).apply {
                Log.debug(
                    ServiceConstants.LOG_TAG, "MessageContent",
                    "Creating MessageContent"
                )
                onCreated(this) // call on created before any other settings

                // base settings
                settings.javaScriptEnabled = true
                settings.allowFileAccess = false
                settings.domStorageEnabled = true
                settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL
                settings.defaultTextEncodingName = "UTF-8"
                settings.mediaPlaybackRequiresUserGesture = false
                settings.databaseEnabled = true

                // ui settings
                isVerticalScrollBarEnabled = true
                isHorizontalScrollBarEnabled = true // do not enable if gestures have to be handled
                isScrollbarFadingEnabled = true
                scrollBarStyle = WebView.SCROLLBARS_INSIDE_OVERLAY
                //setBackgroundColor(0)

                // listeners to handle gesture events
                // val gestureListener = InAppMessageGestureListener(gestureTracker = gestureTracker)
                // val gestureDetector = GestureDetector(it.applicationContext, gestureListener)
                // setOnTouchListener { v, event ->
                //    performClick()
                //    gestureDetector.onTouchEvent(event)
                // }

                //loadData(inAppMessageSettings.content, "text/html", "UTF-8")
                loadDataWithBaseURL(
                    "file:///android_asset/",
                    inAppMessageSettings.content,
                    "text/html",
                    StandardCharsets.UTF_8.name(),
                    null
                )
            }
        },
        modifier = Modifier
            .height(heightDp)
            .width(widthDp)
            .clip(RoundedCornerShape(inAppMessageSettings.cornerRadius.dp))
            .draggable(
                state = rememberDraggableState { delta ->
                    offsetX.value += delta
                },
                orientation = Orientation.Horizontal,
                onDragStopped = {
                    Log.debug(
                        ServiceConstants.LOG_TAG, "MessageContent",
                        "Horizontal:onDragStopped: ${offsetX.value}, ${offsetY.value}, $it"
                    )
                    val gesture = detectGesture(offsetX.value, offsetY.value, it)
                    gesture?.let { gestureTracker.onGestureDetected(gesture) }
                    dragVelocity.value = 0f
                    offsetY.value = 0f
                    offsetX.value = 0f
                }
            )
            .draggable(
                state = rememberDraggableState { delta ->
                    offsetY.value += delta
                },
                orientation = Orientation.Vertical,
                onDragStopped = {
                    Log.debug(
                        ServiceConstants.LOG_TAG, "MessageContent",
                        "Vertical:onDragStopped: ${offsetX.value}, ${offsetY.value}, $it."
                    )
                    val gesture = detectGesture(offsetX.value, offsetY.value, it)
                    gesture?.let { gestureTracker.onGestureDetected(gesture) }
                    dragVelocity.value = 0f
                    offsetY.value = 0f
                    offsetX.value = 0f
                }
            )
    )
}

private fun detectGesture(
    x: Float,
    y: Float,
    velocity: Float
): InAppMessageSettings.MessageGesture? {
    Log.debug(ServiceConstants.LOG_TAG, "MessageContent", "detectGesture: $x, $y, $velocity")
    return if (abs(x) > abs(y)) {
        if (x > 0 && abs(velocity) > 400 && abs(x) > 400) {
            InAppMessageSettings.MessageGesture.SWIPE_RIGHT
        } else if (x < 0 && abs(velocity) > 400 && abs(x) > 400) {
            InAppMessageSettings.MessageGesture.SWIPE_LEFT
        } else null
    } else {
        if (y > 0 && abs(velocity) > 400 && abs(y) > 400) {
            InAppMessageSettings.MessageGesture.SWIPE_DOWN
        } else if (y < 0 && abs(velocity) > 400 && abs(y) > 400) {
            InAppMessageSettings.MessageGesture.SWIPE_UP
        } else null
    }
}