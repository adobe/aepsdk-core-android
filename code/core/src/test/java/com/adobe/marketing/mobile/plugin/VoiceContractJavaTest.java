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

package com.adobe.marketing.mobile.plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import androidx.annotation.NonNull;
import java.util.Collections;
import java.util.Map;
import org.junit.Test;

/** Verifies that the voice contract can be implemented and consumed from Java. */
public class VoiceContractJavaTest {

    @Test
    public void javaImplementation_reportsStatesAndReadsFailureReason() {
        final JavaVoicePlugin plugin = new JavaVoicePlugin();
        final RecordingListener listener = new RecordingListener();

        final IVoiceSession session =
                plugin.startSession(Collections.singletonMap("token", "abc"), listener);

        assertSame(VoiceSessionState.Connecting.INSTANCE, session.getState());
        session.stop();
        assertTrue(listener.lastState instanceof VoiceSessionState.Failed);
        assertEquals("stopped", ((VoiceSessionState.Failed) listener.lastState).getReason());
        assertTrue(listener.lastState.isTerminal());
        assertEquals(VoiceTranscriptRole.USER, VoiceTranscriptRole.valueOf("USER"));
    }

    private static final class JavaVoicePlugin implements IVoicePlugin {
        @NonNull @Override
        public IVoiceSession startSession(
                @NonNull final Map<String, String> connection,
                @NonNull final IVoiceSessionListener listener) {
            return new JavaVoiceSession(listener);
        }
    }

    private static final class JavaVoiceSession implements IVoiceSession {
        private final IVoiceSessionListener listener;
        private VoiceSessionState state = VoiceSessionState.Connecting.INSTANCE;

        JavaVoiceSession(final IVoiceSessionListener listener) {
            this.listener = listener;
        }

        @NonNull @Override
        public VoiceSessionState getState() {
            return state;
        }

        @Override
        public void stop() {
            state = new VoiceSessionState.Failed("stopped");
            listener.onStateChanged(this, state);
        }
    }

    private static final class RecordingListener implements IVoiceSessionListener {
        VoiceSessionState lastState;

        @Override
        public void onStateChanged(
                @NonNull final IVoiceSession session, @NonNull final VoiceSessionState state) {
            lastState = state;
        }

        @Override
        public void onTranscriptUpdated(
                @NonNull final IVoiceSession session,
                @NonNull final String text,
                @NonNull final VoiceTranscriptRole role,
                final boolean isFinal) {}
    }
}
