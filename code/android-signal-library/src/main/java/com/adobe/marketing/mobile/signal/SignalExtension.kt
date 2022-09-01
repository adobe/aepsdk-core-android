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
package com.adobe.marketing.mobile.signal

import com.adobe.marketing.mobile.*
import com.adobe.marketing.mobile.services.HitQueuing
import com.adobe.marketing.mobile.services.PersistentHitQueue
import com.adobe.marketing.mobile.services.ServiceProvider
import com.adobe.marketing.mobile.signal.SignalConstants.LOG_TAG
import com.adobe.marketing.mobile.util.DataReader

class SignalExtension(extensionApi: ExtensionApi) : Extension(extensionApi) {
    private val hitQueue: HitQueuing

    companion object {
        private const val CLASS_NAME = "SignalExtension"
    }

    init {
        val dataQueue =
            ServiceProvider.getInstance().dataQueueService.getDataQueue(SignalConstants.EXTENSION_NAME)
        hitQueue = PersistentHitQueue(dataQueue, SignalHitProcessor())
    }

    override fun onRegistered() {
        api.registerEventListener(EventType.RULES_ENGINE, EventSource.RESPONSE_CONTENT) {
            handleRulesEngineResponse(it)
        }
        api.registerEventListener(EventType.CONFIGURATION, EventSource.RESPONSE_CONTENT) {
            handleConfigurationResponse(it)
        }
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

    private fun handleConfigurationResponse(event: Event) {
        val privacyStatus = try {
            MobilePrivacyStatus.valueOf(
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
            Log.debug(LOG_TAG, "Device has opted-out of tracking. Clearing the Signal queue.")
        }
    }

    private fun handleRulesEngineResponse(event: Event) {
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

    private fun handleOpenURL(event: Event) {
        val url = event.urlToOpen() ?: run {
            Log.warning(
                LOG_TAG,
                "Unable to process OpenURL consequence - no URL was found in EventData."
            )
            return
        }
        Log.debug(LOG_TAG, "Opening URL $url.")
        ServiceProvider.getInstance().uiService.showUrl(url)
    }

    private fun handlePostback(event: Event) {
        val url = event.templateUrl() ?: run {
            Log.warning(LOG_TAG, "Rule consequence Event for Signal doesn't contain url.")
            return
        }
        if (event.isCollectPii() && !url.startsWith("https")) {
            Log.warning(
                LOG_TAG,
                "Rule consequence Event for Signal will not be processed, url must be https."
            )
            return
        }
        val body = event.templateBody() ?: run {
            Log.warning(
                LOG_TAG,
                "Rule consequence Event for Signal will not be processed, url must be https."
            )
            return
        }
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
        ).value ?: return true
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