/*
  Copyright 2026 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.plugin

import com.adobe.marketing.mobile.internal.PluginRegistry
import org.junit.After
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class VoiceContractTest {

    @After
    fun tearDown() {
        PluginRegistry.clear()
    }

    @Test
    fun failedStates_withSameReason_areEqual() {
        assertEquals(VoiceSessionState.Failed("timeout"), VoiceSessionState.Failed("timeout"))
        assertEquals(
            VoiceSessionState.Failed("timeout").hashCode(),
            VoiceSessionState.Failed("timeout").hashCode()
        )
        assertNotEquals(VoiceSessionState.Failed("timeout"), VoiceSessionState.Failed("denied"))
    }

    @Test
    fun states_areDistinct() {
        val states = listOf(
            VoiceSessionState.Connecting,
            VoiceSessionState.Listening,
            VoiceSessionState.Ended,
            VoiceSessionState.Failed("reason")
        )
        assertEquals(states.size, states.toSet().size)
        assertNotEquals<VoiceSessionState>(VoiceSessionState.Ended, VoiceSessionState.Failed("Ended"))
    }

    @Test
    fun isTerminal_isTrueOnlyForEndedAndFailed() {
        assertFalse(VoiceSessionState.Connecting.isTerminal)
        assertFalse(VoiceSessionState.Listening.isTerminal)
        assertTrue(VoiceSessionState.Ended.isTerminal)
        assertTrue(VoiceSessionState.Failed("reason").isTerminal)
    }

    @Test
    fun toString_namesTheStateAndReason() {
        assertEquals("Connecting", VoiceSessionState.Connecting.toString())
        assertEquals("Listening", VoiceSessionState.Listening.toString())
        assertEquals("Ended", VoiceSessionState.Ended.toString())
        assertEquals("Failed(reason=denied)", VoiceSessionState.Failed("denied").toString())
    }

    @Test
    fun getPlugin_resolvesVoicePluginByContract() {
        val plugin = FakeVoicePlugin()

        PluginRegistry.addPlugin(plugin)

        assertSame(plugin, PluginRegistry.getPlugin(IVoicePlugin::class.java))
    }

    @Test
    fun getPlugin_withoutVoicePlugin_returnsNull() {
        PluginRegistry.addPlugin(object : IAepPlugin {})

        assertNull(PluginRegistry.getPlugin(IVoicePlugin::class.java))
    }

    @Test
    fun startSession_deliversUpdatesToTheSessionListener() {
        val plugin = FakeVoicePlugin()
        val listener = RecordingListener()

        val session = plugin.startSession(mapOf("url" to "wss://example.com"), listener)
        plugin.emitTranscript("Hello", VoiceTranscriptRole.ASSISTANT, isFinal = true)
        session.stop()
        session.stop()

        assertEquals(mapOf("url" to "wss://example.com"), plugin.lastConnection)
        assertEquals(listOf<VoiceSessionState>(VoiceSessionState.Ended), listener.states)
        assertEquals(listOf(Triple("Hello", VoiceTranscriptRole.ASSISTANT, true)), listener.transcripts)
        assertEquals(VoiceSessionState.Ended, session.state)
    }

    private class FakeVoicePlugin : IVoicePlugin {
        var lastConnection: Map<String, String>? = null
        private var session: FakeSession? = null

        override fun startSession(
            connection: Map<String, String>,
            listener: IVoiceSessionListener
        ): IVoiceSession {
            lastConnection = connection
            return FakeSession(listener).also { session = it }
        }

        fun emitTranscript(text: String, role: VoiceTranscriptRole, isFinal: Boolean) {
            val current = session ?: return
            current.listener.onTranscriptUpdated(current, text, role, isFinal)
        }
    }

    private class FakeSession(val listener: IVoiceSessionListener) : IVoiceSession {
        override var state: VoiceSessionState = VoiceSessionState.Connecting
            private set

        override fun stop() {
            if (state.isTerminal) return
            state = VoiceSessionState.Ended
            listener.onStateChanged(this, state)
        }
    }

    private class RecordingListener : IVoiceSessionListener {
        val states = mutableListOf<VoiceSessionState>()
        val transcripts = mutableListOf<Triple<String, VoiceTranscriptRole, Boolean>>()

        override fun onStateChanged(session: IVoiceSession, state: VoiceSessionState) {
            states.add(state)
        }

        override fun onTranscriptUpdated(
            session: IVoiceSession,
            text: String,
            role: VoiceTranscriptRole,
            isFinal: Boolean
        ) {
            transcripts.add(Triple(text, role, isFinal))
        }
    }
}
