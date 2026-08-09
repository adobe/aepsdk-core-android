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

import android.content.Context

/**
 * Plugin contract for handling a Live Update push.
 *
 * The push message is passed as [Any] so the platform push object (for example a Firebase
 * `RemoteMessage`) can be forwarded from the host SDK through Core untouched - no bundling or
 * unbundling into a map. The implementing add-on casts it back to the concrete type it expects.
 */
interface ILiveupdatePlugin : IAepPlugin {

    /**
     * Handles a Live Update push. Invoked by the host SDK when a Live Update payload is detected.
     *
     * @param context the application [Context]
     * @param message the platform push message (for example a Firebase `RemoteMessage`), passed as
     *     [Any] to avoid any conversion; the implementation casts it to the type it expects.
     */
    fun handleLiveUpdatePush(context: Context, message: Any)
}
