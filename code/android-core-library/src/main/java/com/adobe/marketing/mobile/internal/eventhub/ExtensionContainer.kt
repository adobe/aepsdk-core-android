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

package com.adobe.marketing.mobile.internal.eventhub

import com.adobe.marketing.mobile.Event
import com.adobe.marketing.mobile.Extension
import com.adobe.marketing.mobile.ExtensionApi
import com.adobe.marketing.mobile.ExtensionError
import com.adobe.marketing.mobile.ExtensionErrorCallback
import com.adobe.marketing.mobile.ExtensionListener
import java.util.concurrent.ExecutorService

internal class ExtensionRuntime() : ExtensionApi() {
    var extension: Extension? = null
        set(value) {
            field = value
            extensionName = value?.name
            extensionFriendlyName = value?.friendlyName
            extensionVersion = value?.version
        }

    // Fetch these values on initialization
    var extensionName: String? = null
        private set
    var extensionFriendlyName: String? = null
        private set
    var extensionVersion: String? = null
        private set

    override fun <T : ExtensionListener?> registerEventListener(
        eventType: String?,
        eventSource: String?,
        extensionListenerClass: Class<T>?,
        errorCallback: ExtensionErrorCallback<ExtensionError>?
    ): Boolean {
        return false
    }

    override fun <T : ExtensionListener?> registerWildcardListener(
        extensionListenerClass: Class<T>?,
        errorCallback: ExtensionErrorCallback<ExtensionError>?
    ): Boolean {
        return false
    }

    override fun setSharedEventState(
        state: MutableMap<String, Any>?,
        event: Event?,
        errorCallback: ExtensionErrorCallback<ExtensionError>?
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun setXDMSharedEventState(
        state: MutableMap<String, Any>?,
        event: Event?,
        errorCallback: ExtensionErrorCallback<ExtensionError>?
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun clearSharedEventStates(errorCallback: ExtensionErrorCallback<ExtensionError>?): Boolean {
        TODO("Not yet implemented")
    }

    override fun clearXDMSharedEventStates(errorCallback: ExtensionErrorCallback<ExtensionError>?): Boolean {
        TODO("Not yet implemented")
    }

    override fun getSharedEventState(
        stateName: String?,
        event: Event?,
        errorCallback: ExtensionErrorCallback<ExtensionError>?
    ): MutableMap<String, Any> {
        TODO("Not yet implemented")
    }

    override fun getXDMSharedEventState(
        stateName: String?,
        event: Event?,
        errorCallback: ExtensionErrorCallback<ExtensionError>?
    ): MutableMap<String, Any> {
        TODO("Not yet implemented")
    }

    override fun unregisterExtension() {
        if (extension == null) {
            return
        }
        EventHub.shared.unregisterExtension(extension?.javaClass) {}
    }
}

internal class ExtensionContainer constructor(
    private val extensionClass: Class<out Extension>,
    private val taskExecutor: ExecutorService,
    callback: (EventHubError) -> Unit
) {

    private val extensionRuntime = ExtensionRuntime()

    val sharedStateName: String?
        get() = extensionRuntime.extensionName

    val friendlyName: String?
        get() = extensionRuntime.extensionFriendlyName

    val version: String?
        get() = extensionRuntime.extensionVersion

    init {
        taskExecutor.submit {
            val extension = extensionClass.initWith(extensionRuntime)
            if (extension == null) {
                callback(EventHubError.ExtensionInitializationFailure)
                return@submit
            }

            if (extension.name == null) {
                callback(EventHubError.InvalidExtensionName)
                return@submit
            }

            // We set this circular reference because ExtensionApi exposes an API to get the underlying extension.
            extensionRuntime.extension = extension
            callback(EventHubError.None)
        }
    }

    fun shutdown() {
        taskExecutor.run {
            extensionRuntime.extension?.onUnregistered()
        }
        taskExecutor.shutdown()
    }
}
