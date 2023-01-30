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

package com.adobe.marketing.mobile.signal.internal

import com.adobe.marketing.mobile.Event
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import org.junit.Test

class EventExtensionsTests {
    @Test
    fun `Test isPostback() - happy`() {
        val event = Event.Builder("name", "type", "source")
            .setEventData(
                mapOf(
                    "triggeredconsequence" to mapOf(
                        "id" to "",
                        "type" to "pb",
                        "detail" to null
                    )
                )
            ).build()
        assertTrue(event.isPostback())
    }

    @Test
    fun `Test isPostback() - other consequence types`() {
        val event = Event.Builder("name", "type", "source")
            .setEventData(
                mapOf(
                    "triggeredconsequence" to mapOf(
                        "id" to "",
                        "type" to "pii",
                        "detail" to null
                    )
                )
            ).build()
        assertFalse(event.isPostback())
    }

    @Test
    fun `Test isPostback() - no type key`() {
        val event = Event.Builder("name", "type", "source")
            .setEventData(
                mapOf(
                    "triggeredconsequence" to mapOf(
                        "id" to "",
                        "detail" to null
                    )
                )
            ).build()
        assertFalse(event.isPostback())
    }

    @Test
    fun `Test isPostback() - null type`() {
        val event = Event.Builder("name", "type", "source")
            .setEventData(
                mapOf(
                    "triggeredconsequence" to mapOf(
                        "id" to "",
                        "type" to null,
                        "detail" to null
                    )
                )
            ).build()
        assertFalse(event.isPostback())
    }

    @Test
    fun `Test isPostback() - not a consequence event`() {
        val event = Event.Builder("name", "type", "source")
            .setEventData(
                mapOf("x" to "y")
            ).build()
        assertFalse(event.isPostback())
    }

    @Test
    fun `Test isOpenUrl() - happy`() {
        val event = Event.Builder("name", "type", "source")
            .setEventData(
                mapOf(
                    "triggeredconsequence" to mapOf(
                        "id" to "",
                        "type" to "url",
                        "detail" to null
                    )
                )
            ).build()
        assertTrue(event.isOpenUrl())
    }

    @Test
    fun `Test isCollectPii() - happy`() {
        val event = Event.Builder("name", "type", "source")
            .setEventData(
                mapOf(
                    "triggeredconsequence" to mapOf(
                        "id" to "",
                        "type" to "pii",
                        "detail" to null
                    )
                )
            ).build()
        assertTrue(event.isCollectPii())
    }

    @Test
    fun `Test contentType() - happy`() {
        val event = Event.Builder("name", "type", "source")
            .setEventData(
                mapOf(
                    "triggeredconsequence" to mapOf(
                        "id" to "",
                        "type" to "pii",
                        "detail" to mapOf(
                            "contenttype" to "application/json",
                            "templatebody" to "{\"key\":\"value\"}",
                            "templateurl" to "https://www.postback.com",
                            "timeout" to 4
                        )
                    )
                )
            ).build()
        assertEquals("application/json", event.contentType())
    }

    @Test
    fun `Test contentType() - no detail`() {
        val event = Event.Builder("name", "type", "source")
            .setEventData(
                mapOf(
                    "triggeredconsequence" to mapOf(
                        "id" to "",
                        "type" to "pii"
                    )
                )
            ).build()
        assertEquals("", event.contentType())
    }

    @Test
    fun `Test contentType() - no contenttype key`() {
        val event = Event.Builder("name", "type", "source")
            .setEventData(
                mapOf(
                    "triggeredconsequence" to mapOf(
                        "id" to "",
                        "type" to "pii",
                        "detail" to mapOf(
                            "templatebody" to "{\"key\":\"value\"}",
                            "templateurl" to "https://www.postback.com",
                            "timeout" to 4
                        )
                    )
                )
            ).build()
        assertEquals("", event.contentType())
    }

    @Test
    fun `Test templateUrl() - happy`() {
        val event = Event.Builder("name", "type", "source")
            .setEventData(
                mapOf(
                    "triggeredconsequence" to mapOf(
                        "id" to "",
                        "type" to "pii",
                        "detail" to mapOf(
                            "contenttype" to "application/json",
                            "templatebody" to "{\"key\":\"value\"}",
                            "templateurl" to "https://www.postback.com",
                            "timeout" to 4
                        )
                    )
                )
            ).build()
        assertEquals("https://www.postback.com", event.templateUrl())
    }

    @Test
    fun `Test templateBody() - happy`() {
        val event = Event.Builder("name", "type", "source")
            .setEventData(
                mapOf(
                    "triggeredconsequence" to mapOf(
                        "id" to "",
                        "type" to "pii",
                        "detail" to mapOf(
                            "contenttype" to "application/json",
                            "templatebody" to "{\"key\":\"value\"}",
                            "templateurl" to "https://www.postback.com",
                            "timeout" to 4
                        )
                    )
                )
            ).build()
        assertEquals("{\"key\":\"value\"}", event.templateBody())
    }

    @Test
    fun `Test urlToOpen() - happy`() {
        val event = Event.Builder("name", "type", "source")
            .setEventData(
                mapOf(
                    "triggeredconsequence" to mapOf(
                        "id" to "",
                        "type" to "url",
                        "detail" to mapOf(
                            "url" to "https://www.testingopenurl.com"
                        )
                    )
                )
            ).build()
        assertEquals("https://www.testingopenurl.com", event.urlToOpen())
    }
}
