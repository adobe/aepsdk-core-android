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

import androidx.annotation.VisibleForTesting
import com.adobe.marketing.mobile.Event
import com.adobe.marketing.mobile.EventSource
import com.adobe.marketing.mobile.EventType
import com.adobe.marketing.mobile.Extension
import com.adobe.marketing.mobile.ExtensionApi
import com.adobe.marketing.mobile.MobilePrivacyStatus
import com.adobe.marketing.mobile.SharedStateResolution
import com.adobe.marketing.mobile.SharedStateStatus
import com.adobe.marketing.mobile.Signal
import com.adobe.marketing.mobile.services.HitQueuing
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.services.PersistentHitQueue
import com.adobe.marketing.mobile.services.ServiceProvider
import com.adobe.marketing.mobile.util.DataReader
import com.adobe.marketing.mobile.util.SQLiteUtils

class SignalExtension : Extension {
    private val hitQueue: HitQueuing

    companion object {
        private const val CLASS_NAME = "SignalExtension"
    }

    constructor(extensionApi: ExtensionApi) : super(extensionApi) {
        val dataQueue =
            ServiceProvider.getInstance().dataQueueService.getDataQueue(SignalConstants.EXTENSION_NAME)
        hitQueue = PersistentHitQueue(dataQueue, SignalHitProcessor())
    }

    @VisibleForTesting
    constructor(extensionApi: ExtensionApi, hitQueue: HitQueuing) : super(extensionApi) {
        this.hitQueue = hitQueue
    }

    override fun onRegistered() {
        api.registerEventListener(EventType.RULES_ENGINE, EventSource.RESPONSE_CONTENT) {
            handleRulesEngineResponse(it)
        }
        api.registerEventListener(EventType.CONFIGURATION, EventSource.RESPONSE_CONTENT) {
            handleConfigurationResponse(it)
        }
        deleteDeprecatedV5HitDatabase()
    }

    private fun deleteDeprecatedV5HitDatabase() {
        SQLiteUtils.deleteDBFromCacheDir(SignalConstants.DEPRECATED_1X_HIT_DATABASE_FILENAME)
    }

    override fun getName(): String {
        return SignalConstants.EXTENSION_NAME
    }

    override fun getFriendlyName(): String {
        return SignalConstants.FRIENDLY_NAME
    }

    override fun getVersion(): String {
        return Signal.extensionVersion()
    }

    @VisibleForTesting
    internal fun handleConfigurationResponse(event: Event) {
        val privacyStatus = try {
            MobilePrivacyStatus.fromString(
                DataReader.getString(
                    event.eventData,
                    SignalConstants.EventDataKeys.Configuration.GLOBAL_CONFIG_PRIVACY
                )
            )
        } catch (e: Exception) {
            MobilePrivacyStatus.UNKNOWN
        }
        hitQueue.handlePrivacyChange(privacyStatus)
        if (privacyStatus == MobilePrivacyStatus.OPT_OUT) {
            Log.debug(
                SignalConstants.LOG_TAG,
                CLASS_NAME,
                "Device has opted-out of tracking. Clearing the Signal queue."
            )
        }
    }

    @VisibleForTesting
    internal fun handleRulesEngineResponse(event: Event) {
        if (shouldIgnore(event)) {
            return
        }
        if (event.isCollectPii() || event.isPostback()) {
            handlePostback(event)
        } else if (event.isOpenUrl()) {
            handleOpenURL(event)
        }
    }

    override fun readyForEvent(event: Event): Boolean {
        return api.getSharedState(
            SignalConstants.EventDataKeys.Configuration.MODULE_NAME,
            event,
            false,
            SharedStateResolution.LAST_SET
        )?.status == SharedStateStatus.SET
    }

    @VisibleForTesting
    internal fun handleOpenURL(event: Event) {
        val url = event.urlToOpen() ?: run {
            Log.warning(
                SignalConstants.LOG_TAG,
                CLASS_NAME,
                "Unable to process OpenURL consequence - no URL was found in EventData."
            )
            return
        }
        Log.debug(SignalConstants.LOG_TAG, CLASS_NAME, "Opening URL $url.")
        ServiceProvider.getInstance().uiService.showUrl(url)
    }

    @VisibleForTesting
    internal fun handlePostback(event: Event) {
        val url = event.templateUrl() ?: run {
            Log.warning(
                SignalConstants.LOG_TAG,
                CLASS_NAME,
                "Rule consequence Event for Signal doesn't contain url."
            )
            return
        }
        if (event.isCollectPii() && !url.startsWith("https")) {
            Log.warning(
                SignalConstants.LOG_TAG,
                CLASS_NAME,
                "Rule consequence Event for Signal will not be processed, url must be https."
            )
            return
        }
        val body = event.templateBody() ?: ""
        val contentType = event.contentType()
        val timeout = event.timeout()
        val dataEntity = SignalHit(url, body, contentType, timeout).toDataEntity()
        hitQueue.queue(dataEntity)
    }

    private fun shouldIgnore(event: Event): Boolean {
        val configuration = api.getSharedState(
            SignalConstants.EventDataKeys.Configuration.MODULE_NAME,
            event,
            false,
            SharedStateResolution.ANY
        )?.value ?: return true
        val privacyStatus = try {
            DataReader.getString(
                configuration,
                SignalConstants.EventDataKeys.Configuration.GLOBAL_CONFIG_PRIVACY
            )
        } catch (e: Exception) {
            MobilePrivacyStatus.UNKNOWN
        }
        return MobilePrivacyStatus.OPT_OUT == privacyStatus
    }
}
