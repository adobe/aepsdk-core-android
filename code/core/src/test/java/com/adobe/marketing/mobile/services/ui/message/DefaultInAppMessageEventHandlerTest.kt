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

package com.adobe.marketing.mobile.services.ui.message

import android.webkit.ValueCallback
import android.webkit.WebView
import com.adobe.marketing.mobile.AdobeCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DefaultInAppMessageEventHandlerTest {

    @Mock
    private lateinit var mockWebView: WebView

    @Mock
    private lateinit var scriptHandler1: DefaultInAppMessageEventHandler.WebViewJavascriptInterface

    @Mock
    private lateinit var scriptHandler2: DefaultInAppMessageEventHandler.WebViewJavascriptInterface

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        `when`(
            mockWebView.evaluateJavascript(
                anyString(),
                any<ValueCallback<String>>()
            )
        ).thenAnswer {
            val callback = it.arguments[1] as ValueCallback<String>
            callback.onReceiveValue("EvaluatedValue")
        }
    }

    @Test
    fun `Test that #onNewWebView updates the internal webView reference`() {
        val defaultInAppMessageEventHandler = DefaultInAppMessageEventHandler(
            mutableMapOf(),
            CoroutineScope(Dispatchers.Unconfined)
        )

        runTest {
            // test
            defaultInAppMessageEventHandler.onNewWebView(mockWebView)

            // verify
            val webView = defaultInAppMessageEventHandler.webView.get()
            assertNotNull(webView)
            assertEquals(mockWebView, webView)
        }
    }

    @Test
    fun `Test that #onNewWebView re-adds existing javascript handlers`() {
        // simulate existing javascript handlers
        val scriptHandlers: MutableMap<String, DefaultInAppMessageEventHandler.WebViewJavascriptInterface> =
            mutableMapOf(
                "handler1" to scriptHandler1,
                "handler2" to scriptHandler2
            )
        val defaultInAppMessageEventHandler = DefaultInAppMessageEventHandler(
            scriptHandlers,
            CoroutineScope(Dispatchers.Unconfined)
        )

        runTest {
            // test
            defaultInAppMessageEventHandler.onNewWebView(mockWebView)

            // verify
            val webView = defaultInAppMessageEventHandler.webView.get()
            assertNotNull(webView)
            assertEquals(mockWebView, webView)
            verify(mockWebView).addJavascriptInterface(scriptHandler1, "handler1")
            verify(mockWebView).addJavascriptInterface(scriptHandler2, "handler2")
        }
    }

    @Test
    fun `Test that #handeJavascriptMessage adds a new javascript handler`() {
        val scriptHandlers: MutableMap<String, DefaultInAppMessageEventHandler.WebViewJavascriptInterface> =
            mutableMapOf()
        val defaultInAppMessageEventHandler = DefaultInAppMessageEventHandler(
            scriptHandlers,
            CoroutineScope(Dispatchers.Unconfined)
        )

        val callback = Mockito.mock(AdobeCallback::class.java) as AdobeCallback<String>

        runTest {
            defaultInAppMessageEventHandler.onNewWebView(mockWebView)
            // test
            defaultInAppMessageEventHandler.handleJavascriptMessage(
                "handler",
                callback
            )

            // verify that the new webView is set on the handler
            val webView = defaultInAppMessageEventHandler.webView.get()
            assertNotNull(webView)
            assertEquals(mockWebView, webView)

            // verify that the script handler is added to the webView
            val argumentCaptor =
                argumentCaptor<Any>()
            verify(mockWebView).addJavascriptInterface(argumentCaptor.capture(), eq("handler"))

            // verify that the script handler is called when the javascript interface is invoked
            assertTrue(argumentCaptor.firstValue is DefaultInAppMessageEventHandler.WebViewJavascriptInterface)
            (argumentCaptor.firstValue as DefaultInAppMessageEventHandler.WebViewJavascriptInterface).run(
                "test"
            )
            verify(callback).call("test")
        }
    }

    @Test
    fun `Test that #handleJavaScriptMessage calls the callback with the correct result`() {
        val scriptHandlers: MutableMap<String, DefaultInAppMessageEventHandler.WebViewJavascriptInterface> =
            mutableMapOf()
        val defaultInAppMessageEventHandler = DefaultInAppMessageEventHandler(
            scriptHandlers,
            CoroutineScope(Dispatchers.Unconfined)
        )

        val callback = Mockito.mock(AdobeCallback::class.java) as AdobeCallback<String>

        runTest {
            defaultInAppMessageEventHandler.onNewWebView(mockWebView)
            // test
            defaultInAppMessageEventHandler.handleJavascriptMessage(
                "handler",
                callback
            )

            // verify that the new webView is set on the handler
            val webView = defaultInAppMessageEventHandler.webView.get()
            assertNotNull(webView)
            assertEquals(mockWebView, webView)

            // verify that the javascript interface is added to the webView
            val argumentCaptor =
                argumentCaptor<Any>()
            verify(mockWebView).addJavascriptInterface(argumentCaptor.capture(), eq("handler"))

            // verify that the script handler is called when the javascript interface is invoked
            assertTrue(argumentCaptor.firstValue is DefaultInAppMessageEventHandler.WebViewJavascriptInterface)
            (argumentCaptor.firstValue as DefaultInAppMessageEventHandler.WebViewJavascriptInterface).run(
                "test"
            )
            verify(callback).call("test")
        }
    }

    @Test
    fun `Test #handleJavascriptMessage on null webView re-adds when new WebView is incident`() {
        val scriptHandlers: MutableMap<String, DefaultInAppMessageEventHandler.WebViewJavascriptInterface> =
            mutableMapOf()
        val defaultInAppMessageEventHandler = DefaultInAppMessageEventHandler(
            scriptHandlers,
            CoroutineScope(Dispatchers.Unconfined)
        )

        val callback = Mockito.mock(AdobeCallback::class.java) as AdobeCallback<String>

        runTest {
            // test
            defaultInAppMessageEventHandler.handleJavascriptMessage(
                "handler",
                callback
            )

            // verify
            val webView = defaultInAppMessageEventHandler.webView.get()
            assertNull(webView)

            // simulate new webView
            defaultInAppMessageEventHandler.onNewWebView(mockWebView)

            // verify that the script handler was re-added onto the new webView
            val argumentCaptor =
                argumentCaptor<Any>()
            verify(mockWebView).addJavascriptInterface(argumentCaptor.capture(), eq("handler"))
        }
    }

    @Test
    fun `Test that #evaluateJavascript with callback bails on empty javascript content`() {
        val scriptHandlers: MutableMap<String, DefaultInAppMessageEventHandler.WebViewJavascriptInterface> =
            mutableMapOf(
                "handler" to scriptHandler1
            )
        val defaultInAppMessageEventHandler = DefaultInAppMessageEventHandler(
            scriptHandlers,
            CoroutineScope(Dispatchers.Unconfined)
        )
        val callback = Mockito.mock(AdobeCallback::class.java) as AdobeCallback<String>

        runTest {
            defaultInAppMessageEventHandler.onNewWebView(mockWebView)

            defaultInAppMessageEventHandler.evaluateJavascript("", callback)
            verify(mockWebView, never()).evaluateJavascript(anyString(), any())
            verify(callback, never()).call(anyString())
        }
    }

    @Test
    fun `Test that #evaluateJavascript with callback executes immediately on webview`() {
        val scriptHandlers: MutableMap<String, DefaultInAppMessageEventHandler.WebViewJavascriptInterface> =
            mutableMapOf(
                "handler" to scriptHandler1
            )
        val defaultInAppMessageEventHandler = DefaultInAppMessageEventHandler(
            scriptHandlers,
            CoroutineScope(Dispatchers.Unconfined)
        )
        val callback = Mockito.mock(AdobeCallback::class.java) as AdobeCallback<String>

        val jsContent = "someMockJsContent"
        runTest {
            defaultInAppMessageEventHandler.onNewWebView(mockWebView)

            defaultInAppMessageEventHandler.evaluateJavascript(jsContent, callback)
            verify(mockWebView).evaluateJavascript(eq(jsContent), any())
            verify(callback).call("EvaluatedValue")
        }
    }
}
