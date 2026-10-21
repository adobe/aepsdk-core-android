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
 * Plugin contract for a conversational voice agent.
 *
 * A host extension resolves it with `MobileCore.getPlugin(IVoicePlugin::class.java)` and hides its
 * voice entry point when the result is `null`. The registry holds one plugin instance for the
 * whole app, so all per-conversation state lives in the [IVoiceSession] returned by
 * [startSession]. Several hosts can therefore run sessions without interfering with each other.
 *
 * The contract uses only Kotlin types, so the host and the implementing add-on depend on Core only,
 * never on each other or on the add-on's media libraries. Implementations resolve the application
 * `Context` from Core's `ServiceProvider`.
 *
 * Implementations must not crash or block the host. Failures are reported to the listener as
 * [VoiceSessionState.Failed].
 */
interface IVoicePlugin : IAepPlugin {

    /**
     * Starts a new, independent voice session and returns immediately.
     *
     * The returned session starts in [VoiceSessionState.Connecting]. Its state changes and
     * transcript updates are reported to [listener] on the main thread. The session keeps a
     * reference to [listener] until it reaches a terminal state ([VoiceSessionState.Ended] or
     * [VoiceSessionState.Failed]) and releases it afterwards.
     *
     * The app must hold the `android.permission.RECORD_AUDIO` runtime permission before this method
     * is called; a plugin cannot show the permission prompt. Without it, the session fails.
     *
     * @param connection opaque connection fields the host obtained from its backend (for example
     *     `url` and `token`). The host passes them through without interpreting them; only the
     *     plugin knows what they mean. If they are invalid, the session reports
     *     [VoiceSessionState.Failed].
     * @param listener receives state changes and transcript updates for this session only
     * @return the new session. This method never throws.
     */
    fun startSession(connection: Map<String, String>, listener: IVoiceSessionListener): IVoiceSession
}
