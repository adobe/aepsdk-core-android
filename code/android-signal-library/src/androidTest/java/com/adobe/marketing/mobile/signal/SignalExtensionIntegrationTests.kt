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
package com.adobe.marketing.mobile.signal

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.adobe.marketing.mobile.MobileCore
import com.adobe.marketing.mobile.services.HttpConnecting
import com.adobe.marketing.mobile.services.Networking
import com.adobe.marketing.mobile.services.ServiceProvider
import org.junit.Assert
import org.junit.Assert.fail
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import java.io.InputStream

@RunWith(AndroidJUnit4::class)
class SignalExtensionIntegrationTests {

    companion object {
        @BeforeClass
        @JvmStatic
        fun setupClass() {
            ServiceProvider.getInstance().networkService = Networking { request, callback ->
                with(request.url) {
                    when {
                        startsWith("https://adobe.com") && contains("rules_signal.zip") -> {
                            MockedHttpConnecting("rules_signal")
                        }
                        startsWith("https://adobe.com") && contains("rules_pii.zip") -> {
                            MockedHttpConnecting("rules_pii")
                        }
                        else -> {
                            fail("Unexpected url: ${request.url}")
                        }
                    }
                }
            }

        }
    }

    @Before
    fun setUP() {
        EventHubProxy.resetEventhub()
        MobileCore.start {

        }
    }

    @Test
    fun `x`() {
// Context of the app under test.

        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        Assert.assertEquals("com.adobe.marketing.mobile.signal.test", appContext.packageName)
    }
}

private class MockedHttpConnecting(val rulesFileName: String) : HttpConnecting {
    val rulesStream: InputStream = this::class.java.classLoader?.getResource("$rulesFileName.zip")
        ?.openStream()!!

    override fun getInputStream(): InputStream {
        return rulesStream
    }

    override fun getErrorStream(): InputStream? {
        return null
    }

    override fun getResponseCode(): Int {
        return 200
    }

    override fun getResponseMessage(): String {
        return ""
    }

    override fun getResponsePropertyValue(responsePropertyKey: String?): String {
        return ""
    }

    override fun close() {
        rulesStream.close()
    }

}
