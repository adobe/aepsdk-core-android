package com.adobe.marketing.mobile.signal

import com.adobe.marketing.mobile.Event
import org.junit.Assert.*
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
        assertNull(event.contentType())
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
        assertNull(event.contentType())
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