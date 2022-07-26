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
import com.adobe.marketing.mobile.utils.DataReader

class SignalExtension(extensionApi: ExtensionApi?) : Extension(extensionApi) {
    private val hitQueue: HitQueuing

    companion object {
        private const val LOG_TAG = "SignalExtension"
    }

    init {
        val dataQueue =
                ServiceProvider.getInstance().dataQueueService.getDataQueue(SignalConstants.EXTENSION_NAME)
        hitQueue = PersistentHitQueue(dataQueue, SignalHitProcessor())
    }

    override fun onRegistered() {
        api?.registerEventListener(EventType1.RULES_ENGINE, EventSource1.RESPONSE_CONTENT) {
            handleRulesEngineResponse(it)
        }
        api?.registerEventListener(EventType1.CONFIGURATION, EventSource1.RESPONSE_CONTENT) {
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

    private fun handleConfigurationResponse(event: Event?) {
        if (event == null) return
        val privacyStatus = try {
            DataReader.getString(
                    event.eventData,
                    SignalConstants.EventDataKeys.Configuration.GLOBAL_CONFIG_PRIVACY
            )
        } catch (e: Exception) {
            MobilePrivacyStatus.UNKNOWN
        }
        // TODO: call HitQueuing.handlePrivacyChange(status) once the missing method is added to the Android Core(https://github.com/adobe/aepsdk-core-android/issues/121)
        if (privacyStatus == MobilePrivacyStatus.OPT_OUT) {
            Log.debug(LOG_TAG, "Device has opted-out of tracking. Clearing the Signal queue.")
        }
    }

    private fun handleRulesEngineResponse(event: Event?) {
        if (event == null) return
        if (shouldIgnore(event)) {
            return
        }
        if (event.isCollectPii() || event.isPostback()) {
            handlePostback(event)
        } else if (event.isOpenUrl()) {
            handleOpenURL(event)
        }
    }

    override fun readyForEvent(event: Event?): Boolean {
        if (event == null) return false
        return api.getSharedState(
                SignalConstants.EventDataKeys.Configuration.MODULE_NAME,
                event,
                false,
                SharedStateResolution.LAST_SET
        )?.status == SharedStateStatus.SET
    }

    private fun handleOpenURL(event: Event) {
        val url = event.urlToOpen() ?: run {
            // TODO: logs
            return
        }
        Log.debug(LOG_TAG, "Opening URL %s.", url)
        ServiceProvider.getInstance().uiService.showUrl(url)
    }

    private fun handlePostback(event: Event) {
        val url = event.templateUrl() ?: run {
            // TODO: logs
            return
        }
        if (event.isCollectPii() && !url.startsWith("https")) {
            Log.warning(LOG_TAG, "Dropping collect pii call, url must be https: %s.", url)
            return
        }
        val body = event.templateBody() ?: run {
            // TODO: logs
            return
        }
        val contentType = event.contentType() ?: run {
            // TODO: logs
            return
        }
        val timeout = event.timeout() ?: run {
            // TODO: logs
            return
        }
        val dataEntity = SignalConsequence(url, body, contentType, timeout).toDataEntity()
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