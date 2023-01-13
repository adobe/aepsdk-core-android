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
import com.adobe.marketing.mobile.ExtensionApi
import com.adobe.marketing.mobile.MobilePrivacyStatus
import com.adobe.marketing.mobile.SharedStateResolution
import com.adobe.marketing.mobile.SharedStateResult
import com.adobe.marketing.mobile.SharedStateStatus
import com.adobe.marketing.mobile.services.HitQueuing
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.never
import org.mockito.Mockito.spy
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull

@RunWith(MockitoJUnitRunner.Silent::class)
class SignalFunctionalTests {

    @Mock
    private lateinit var mockedExtensionApi: ExtensionApi

    @Mock
    private lateinit var mockedHitQueue: HitQueuing

    private lateinit var signalExtension: SignalExtension

    @Before
    fun setup() {
        Mockito.reset(mockedExtensionApi)
        signalExtension = SignalExtension(mockedExtensionApi, mockedHitQueue)
    }

    @Test
    fun `readyForEvent() - return true with valid configuration `() {
        val event = Event.Builder("event", "type", "source").build()
        Mockito.`when`(
            mockedExtensionApi.getSharedState(
                "com.adobe.module.configuration",
                event,
                false,
                SharedStateResolution.LAST_SET
            )
        ).thenReturn(SharedStateResult(SharedStateStatus.SET, mapOf("key" to "value")))

        assertTrue(signalExtension.readyForEvent(event))
    }

    @Test
    fun `readyForEvent() - return false without configuration `() {
        val event = Event.Builder("event", "type", "source").build()
        `when`(
            mockedExtensionApi.getSharedState(
                "com.adobe.module.configuration",
                event,
                false,
                SharedStateResolution.LAST_SET
            )
        ).thenReturn(null)

        assertFalse(signalExtension.readyForEvent(event))
    }

    @Test
    fun `readyForEvent() - return false with pending configuration `() {
        val event = Event.Builder("event", "type", "source").build()
        `when`(
            mockedExtensionApi.getSharedState(
                "com.adobe.module.configuration",
                event,
                false,
                SharedStateResolution.LAST_SET
            )
        ).thenReturn(SharedStateResult(SharedStateStatus.PENDING, null))

        assertFalse(signalExtension.readyForEvent(event))
    }

    @Test
    fun `handleConfigurationResponse() - privacy status is invalid`() {
        val event = Event.Builder("event", "type", "source").setEventData(
            mapOf(
                "global.privacy" to null
            )
        ).build()
        signalExtension.handleConfigurationResponse(event)

        val privacyStatusClassCaptor = ArgumentCaptor.forClass(
            MobilePrivacyStatus::class.java
        )
        verify(mockedHitQueue, times(1)).handlePrivacyChange(privacyStatusClassCaptor.capture())
        assertEquals(MobilePrivacyStatus.UNKNOWN, privacyStatusClassCaptor.value)
    }

    @Test
    fun `handleConfigurationResponse() - privacy status is OPT_IN`() {
        val event = Event.Builder("event", "type", "source").setEventData(
            mapOf(
                "global.privacy" to "optedin"
            )
        ).build()
        signalExtension.handleConfigurationResponse(event)

        val privacyStatusClassCaptor = ArgumentCaptor.forClass(
            MobilePrivacyStatus::class.java
        )
        verify(mockedHitQueue, times(1)).handlePrivacyChange(privacyStatusClassCaptor.capture())
        assertEquals(MobilePrivacyStatus.OPT_IN, privacyStatusClassCaptor.value)
    }

    @Test
    fun `handleConfigurationResponse() - privacy status is OPT_OUT`() {
        val event = Event.Builder("event", "type", "source").setEventData(
            mapOf(
                "global.privacy" to "optedout"
            )
        ).build()
        signalExtension.handleConfigurationResponse(event)

        val privacyStatusClassCaptor = ArgumentCaptor.forClass(
            MobilePrivacyStatus::class.java
        )
        verify(mockedHitQueue, times(1)).handlePrivacyChange(privacyStatusClassCaptor.capture())
        assertEquals(MobilePrivacyStatus.OPT_OUT, privacyStatusClassCaptor.value)
    }

    @Test
    fun `handleRulesEngineResponse() - ignore event if no configuration`() {
        val event = Event.Builder("event", "type", "source").build()
        `when`(
            mockedExtensionApi.getSharedState(
                any(),
                any(),
                anyOrNull(),
                any()
            )
        ).thenReturn(SharedStateResult(SharedStateStatus.PENDING, null))
        val spiedSignalExtension = spy(signalExtension)
        spiedSignalExtension.handleRulesEngineResponse(event)
        verify(spiedSignalExtension, never()).handlePostback(anyOrNull())
        verify(spiedSignalExtension, never()).handleOpenURL(anyOrNull())
    }

    @Test
    fun `handleRulesEngineResponse() - ignore event if privacy status is invalid`() {
        val event = Event.Builder("event", "type", "source").build()
        `when`(
            mockedExtensionApi.getSharedState(
                any(),
                any(),
                anyOrNull(),
                any()
            )
        ).thenReturn(
            SharedStateResult(
                SharedStateStatus.SET,
                mapOf(
                    "global.privacy" to null
                )
            )
        )
        val spiedSignalExtension = spy(signalExtension)
        spiedSignalExtension.handleRulesEngineResponse(event)
        verify(spiedSignalExtension, never()).handlePostback(anyOrNull())
        verify(spiedSignalExtension, never()).handleOpenURL(anyOrNull())
    }

    @Test
    fun `handleRulesEngineResponse() - open url`() {
        val event = Event.Builder("event", "type", "source").setEventData(
            mapOf(
                "triggeredconsequence" to mapOf(
                    "type" to "url"
                )
            )
        ).build()
        `when`(
            mockedExtensionApi.getSharedState(
                any(),
                any(),
                anyOrNull(),
                any()
            )
        ).thenReturn(
            SharedStateResult(
                SharedStateStatus.SET,
                mapOf(
                    "global.privacy" to "optedin"
                )
            )
        )
        val spiedSignalExtension = spy(signalExtension)
        spiedSignalExtension.handleRulesEngineResponse(event)
        verify(spiedSignalExtension, never()).handlePostback(anyOrNull())
        verify(spiedSignalExtension, times(1)).handleOpenURL(anyOrNull())
    }

    @Test
    fun `handleRulesEngineResponse() - collect pii without valid url`() {
        val event = Event.Builder("event", "type", "source").setEventData(
            mapOf(
                "triggeredconsequence" to mapOf(
                    "type" to "pii",
                    "detail" to mapOf(
                        "timeout" to 0,
                        "templateurl" to null
                    )
                )
            )
        ).build()
        `when`(
            mockedExtensionApi.getSharedState(
                any(),
                any(),
                anyOrNull(),
                any()
            )
        ).thenReturn(
            SharedStateResult(
                SharedStateStatus.SET,
                mapOf(
                    "global.privacy" to "optedin"
                )
            )
        )
        val spiedSignalExtension = spy(signalExtension)
        spiedSignalExtension.handleRulesEngineResponse(event)
        verify(spiedSignalExtension, times(1)).handlePostback(anyOrNull())
        verify(spiedSignalExtension, never()).handleOpenURL(anyOrNull())
        verify(mockedHitQueue, never()).queue(any())
    }

    @Test
    fun `handleRulesEngineResponse() - collect pii without https url`() {
        val event = Event.Builder("event", "type", "source").setEventData(
            mapOf(
                "triggeredconsequence" to mapOf(
                    "type" to "pii",
                    "detail" to mapOf(
                        "timeout" to 0,
                        "templateurl" to "http://www.pii.com?name={%contextdata.name%}"
                    )
                )
            )
        ).build()
        `when`(
            mockedExtensionApi.getSharedState(
                any(),
                any(),
                anyOrNull(),
                any()
            )
        ).thenReturn(
            SharedStateResult(
                SharedStateStatus.SET,
                mapOf(
                    "global.privacy" to "optedin"
                )
            )
        )
        val spiedSignalExtension = spy(signalExtension)
        spiedSignalExtension.handleRulesEngineResponse(event)
        verify(spiedSignalExtension, times(1)).handlePostback(anyOrNull())
        verify(spiedSignalExtension, never()).handleOpenURL(anyOrNull())
        verify(mockedHitQueue, never()).queue(any())
    }

    @Test
    fun `handleRulesEngineResponse() - collect pii`() {
        val event = Event.Builder("event", "type", "source").setEventData(
            mapOf(
                "triggeredconsequence" to mapOf(
                    "type" to "pii",
                    "detail" to mapOf(
                        "timeout" to 0,
                        "templateurl" to "https://www.pii.com?name={%contextdata.name%}"
                    )
                )
            )
        ).build()
        `when`(
            mockedExtensionApi.getSharedState(
                any(),
                any(),
                anyOrNull(),
                any()
            )
        ).thenReturn(
            SharedStateResult(
                SharedStateStatus.SET,
                mapOf(
                    "global.privacy" to "optedin"
                )
            )
        )
        val spiedSignalExtension = spy(signalExtension)
        spiedSignalExtension.handleRulesEngineResponse(event)
        verify(spiedSignalExtension, times(1)).handlePostback(anyOrNull())
        verify(spiedSignalExtension, never()).handleOpenURL(anyOrNull())
        verify(mockedHitQueue, times(1)).queue(any())
    }

    @Test
    fun `handleRulesEngineResponse() - post back`() {
        val event = Event.Builder("event", "type", "source").setEventData(
            mapOf(
                "triggeredconsequence" to mapOf(
                    "type" to "pb",
                    "detail" to mapOf(
                        "timeout" to 0,
                        "templateurl" to "https://www.signal.com?name={%name%}",
                        "templatebody" to "name={%name%}",
                        "contenttype" to "zip"
                    )
                )
            )
        ).build()
        `when`(
            mockedExtensionApi.getSharedState(
                any(),
                any(),
                anyOrNull(),
                any()
            )
        ).thenReturn(
            SharedStateResult(
                SharedStateStatus.SET,
                mapOf(
                    "global.privacy" to "optedin"
                )
            )
        )
        val spiedSignalExtension = spy(signalExtension)
        spiedSignalExtension.handleRulesEngineResponse(event)
        verify(spiedSignalExtension, times(1)).handlePostback(anyOrNull())
        verify(spiedSignalExtension, never()).handleOpenURL(anyOrNull())
    }
}
