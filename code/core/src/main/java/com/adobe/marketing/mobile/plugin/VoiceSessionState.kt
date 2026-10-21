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

/**
 * The lifecycle of an [IVoiceSession]: [Connecting], then [Listening], then [Ended] or [Failed].
 * [Ended] and [Failed] are terminal; a session never leaves them.
 */
sealed class VoiceSessionState {

    /** Whether this is a terminal state ([Ended] or [Failed]). */
    val isTerminal: Boolean
        get() = this is Ended || this is Failed

    /** The session is connecting to the voice agent. */
    object Connecting : VoiceSessionState() {
        override fun toString(): String = "Connecting"
    }

    /** The session is connected and the conversation is live. */
    object Listening : VoiceSessionState() {
        override fun toString(): String = "Listening"
    }

    /** The session ended normally, for example after [IVoiceSession.stop]. */
    object Ended : VoiceSessionState() {
        override fun toString(): String = "Ended"
    }

    /**
     * The session could not start or was lost.
     *
     * This is intentionally a plain (non-`data`) class so fields can be added later without
     * breaking binary compatibility.
     *
     * @property reason a description of the failure, for logging rather than display
     */
    class Failed(val reason: String) : VoiceSessionState() {
        override fun equals(other: Any?): Boolean = other is Failed && other.reason == reason

        override fun hashCode(): Int = reason.hashCode()

        override fun toString(): String = "Failed(reason=$reason)"
    }
}
