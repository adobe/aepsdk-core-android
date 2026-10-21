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

/** One voice conversation started by [IVoicePlugin.startSession]. */
interface IVoiceSession {

    /** The current state. It matches the last state reported to the listener. */
    val state: VoiceSessionState

    /**
     * Ends the session. Returns immediately; the end is reported to the listener as
     * [VoiceSessionState.Ended].
     *
     * Calling it again, or after the session has already ended or failed, has no effect. It may be
     * called from any thread.
     */
    fun stop()
}
