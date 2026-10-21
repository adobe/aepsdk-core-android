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

/** Receives updates for one [IVoiceSession]. Every method is called on the main thread. */
interface IVoiceSessionListener {

    /**
     * Called whenever the session's state changes.
     *
     * @param session the session whose state changed
     * @param state the new state, which is also [IVoiceSession.state]
     */
    fun onStateChanged(session: IVoiceSession, state: VoiceSessionState)

    /**
     * Called whenever a transcript turn is updated.
     *
     * @param session the session the transcript belongs to
     * @param text the full text of the current turn so far (cumulative, not a delta)
     * @param role who is speaking
     * @param isFinal `true` when the turn is complete. The next update for the same role starts a
     *     new turn.
     */
    fun onTranscriptUpdated(
        session: IVoiceSession,
        text: String,
        role: VoiceTranscriptRole,
        isFinal: Boolean
    )
}
