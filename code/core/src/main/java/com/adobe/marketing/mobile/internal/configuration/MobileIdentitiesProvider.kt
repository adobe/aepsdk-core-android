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

package com.adobe.marketing.mobile.internal.configuration

import androidx.annotation.VisibleForTesting
import com.adobe.marketing.mobile.Event
import com.adobe.marketing.mobile.ExtensionApi
import com.adobe.marketing.mobile.SharedStateResolution
import com.adobe.marketing.mobile.SharedStateResult
import com.adobe.marketing.mobile.SharedStateStatus
import com.adobe.marketing.mobile.VisitorID
import com.adobe.marketing.mobile.internal.util.VisitorIDSerializer
import com.adobe.marketing.mobile.util.DataReader
import org.json.JSONException
import org.json.JSONObject

/**
 * Responsible for providing the mobile identities known to the sdk from all extensions.
 */
internal object MobileIdentitiesProvider {

    // JSON key constants
    private const val JSON_KEY_COMPANY_CONTEXTS = "companyContexts"
    private const val JSON_KEY_USERS = "users"
    private const val JSON_KEY_USER_IDS = "userIDs"
    private const val JSON_KEY_NAMESPACE = "namespace"
    private const val JSON_KEY_VALUE = "value"
    private const val JSON_KEY_TYPE = "type"

    // JSON value constants for SDKIdentifier namespace
    private const val JSON_VALUE_NAMESPACE_COMPANY_CONTEXTS = "imsOrgID"
    private const val JSON_VALUE_NAMESPACE_ANALYTICS_AID = "AVID"
    private const val JSON_VALUE_NAMESPACE_AUDIENCE_UUID = "0"
    private const val JSON_VALUE_NAMESPACE_MCID = "4"
    private const val JSON_VALUE_NAMESPACE_TARGET_THIRD_PARTY_ID = "3rdpartyid"
    private const val JSON_VALUE_NAMESPACE_TARGET_TNTID = "tntid"
    private const val JSON_VALUE_NAMESPACE_USER_IDENTIFIER = "vid"
    private const val JSON_VALUE_NAMESPACE_MCPNS_DPID = "20919"

    // JSON value constants for SDKIdentifier type
    private const val JSON_VALUE_TYPE_ANALYTICS = "analytics"
    private const val JSON_VALUE_TYPE_INTEGRATION_CODE = "integrationCode"
    private const val JSON_VALUE_TYPE_TARGET = "target"
    private const val JSON_VALUE_TYPE_NAMESPACE_ID = "namespaceId"

    private val REQUIRED_READY_EXTENSIONS = listOf(
        SharedStateKeys.Analytics.EXTENSION_NAME,
        SharedStateKeys.Audience.EXTENSION_NAME,
        SharedStateKeys.Configuration.EXTENSION_NAME,
        SharedStateKeys.Target.EXTENSION_NAME,
        SharedStateKeys.Identity.EXTENSION_NAME
    )

    @VisibleForTesting
    internal object SharedStateKeys {
        internal object Analytics {
            internal const val EXTENSION_NAME = "com.adobe.module.analytics"
            internal const val ANALYTICS_ID = "aid"
            internal const val VISITOR_IDENTIFIER = "vid"
        }

        internal object Audience {
            internal const val EXTENSION_NAME = "com.adobe.module.audience"
            internal const val DPID = "dpid"
            internal const val DPUUID = "dpuuid"
            internal const val UUID = "uuid"
        }

        internal object Identity {
            internal const val EXTENSION_NAME = "com.adobe.module.identity"
            internal const val MID = "mid"
            internal const val VISITOR_IDS_LIST = "visitoridslist"
            internal const val ADVERTISING_IDENTIFIER = "advertisingidentifier"
            internal const val PUSH_IDENTIFIER = "pushidentifier"
        }

        internal object Target {
            internal const val EXTENSION_NAME = "com.adobe.module.target"
            internal const val TNT_ID = "tntid"
            internal const val THIRD_PARTY_ID = "thirdpartyid"
        }

        internal object Configuration {
            internal const val EXTENSION_NAME = "com.adobe.module.configuration"
            internal const val CONFIG_EXPERIENCE_CLOUD_ORG_ID = "experienceCloud.org"
        }
    }

    /**
     * Represents an identifier known to the SDK
     */
    private data class ID(val namespace: String, val value: String, val type: String)

    /**
     * Collects all the identities from various extensions and packages them into a JSON string
     *
     * @param event the [Event] generated by the GetSDKIdentities API
     * @param extensionApi the extensionApi used for fetching the states
     */
    internal fun collectSdkIdentifiers(event: Event, extensionApi: ExtensionApi): String {
        // Collect all UserIDs
        val identifiers = mutableListOf<ID>()
        identifiers.addAll(getAnalyticsIdentifiers(event, extensionApi))
        identifiers.addAll(getAudienceIdentifiers(event, extensionApi))
        identifiers.addAll(getVisitorIdentifiers(event, extensionApi))
        identifiers.addAll(getTargetIdentifiers(event, extensionApi))

        val identifierMaps = identifiers.map {
            mapOf(
                JSON_KEY_NAMESPACE to it.namespace,
                JSON_KEY_VALUE to it.value,
                JSON_KEY_TYPE to it.type
            )
        }

        // Collect company contexts
        val companyContexts: MutableList<Map<String, String>> = mutableListOf()
        getCompanyContext(event, extensionApi)?.also { orgId ->
            companyContexts.add(
                mapOf(
                    JSON_KEY_NAMESPACE to JSON_VALUE_NAMESPACE_COMPANY_CONTEXTS,
                    JSON_KEY_VALUE to orgId
                )
            )
        }

        // Prepare SDK Identities Json
        val sdkIdentities: MutableMap<String, Any?> = mutableMapOf()
        if (companyContexts.isNotEmpty()) sdkIdentities[JSON_KEY_COMPANY_CONTEXTS] = companyContexts
        if (identifierMaps.isNotEmpty()) sdkIdentities[JSON_KEY_USERS] = listOf(mapOf(JSON_KEY_USER_IDS to identifierMaps))

        val sdkIdentitiesJson = try {
            JSONObject(sdkIdentities).toString()
        } catch (e: JSONException) {
            JSONObject().toString()
        }

        return sdkIdentitiesJson
    }

    /**
     * Gets the required identities from Identity extension.
     *
     * @param event the event at which the shared state is to be retrieved
     * @param extensionApi the [ExtensionApi] used to retrieve the shared state
     * @return a [List] of [ID]'s populated using Identity shared state
     */
    private fun getVisitorIdentifiers(
        event: Event,
        extensionApi: ExtensionApi
    ): List<ID> {
        val visitorIdentifiers = mutableListOf<ID>()

        val identitySharedState =
            getSharedState(SharedStateKeys.Identity.EXTENSION_NAME, event, extensionApi)

        // MID
        DataReader.optString(identitySharedState?.value, SharedStateKeys.Identity.MID, null)
            ?.also { marketingCloudId ->
                visitorIdentifiers.add(
                    ID(
                        JSON_VALUE_NAMESPACE_MCID,
                        marketingCloudId,
                        JSON_VALUE_TYPE_NAMESPACE_ID
                    )
                )
            }

        // Visitor Id list
        DataReader.optTypedList(
            Map::class.java,
            identitySharedState?.value,
            SharedStateKeys.Identity.VISITOR_IDS_LIST,
            emptyList()
        )?.also { customVisitorIDs ->
            val visitorIDs: List<VisitorID> =
                VisitorIDSerializer.convertToVisitorIds(customVisitorIDs)
            visitorIDs.forEach { visitorID ->
                if (!visitorID.id.isNullOrEmpty()) {
                    visitorIdentifiers.add(
                        ID(
                            visitorID.idType,
                            visitorID.id,
                            JSON_VALUE_TYPE_INTEGRATION_CODE
                        )
                    )
                }
            }
        }

        // push identifier
        DataReader.optString(
            identitySharedState?.value,
            SharedStateKeys.Identity.PUSH_IDENTIFIER,
            null
        )?.also { pushIdentifier ->
            if (pushIdentifier.isNotEmpty()) {
                visitorIdentifiers.add(
                    ID(
                        JSON_VALUE_NAMESPACE_MCPNS_DPID,
                        pushIdentifier,
                        JSON_VALUE_TYPE_INTEGRATION_CODE
                    )
                )
            }
        }
        return visitorIdentifiers
    }

    /**
     * Gets the required identities from Analytics extension.
     *
     * @param event the event at which the shared state is to be retrieved
     * @param extensionApi the [ExtensionApi] used to retrieve the shared state
     * @return a [List] of [ID]'s populated using Analytics shared state
     */
    private fun getAnalyticsIdentifiers(
        event: Event,
        extensionApi: ExtensionApi
    ): List<ID> {
        val analyticsSharedState =
            getSharedState(SharedStateKeys.Analytics.EXTENSION_NAME, event, extensionApi)

        val analyticsIdentifiers = mutableListOf<ID>()
        if (!isSharedStateSet(analyticsSharedState)) return analyticsIdentifiers

        // Analytics ID
        DataReader.optString(
            analyticsSharedState?.value,
            SharedStateKeys.Analytics.ANALYTICS_ID,
            null
        )?.also { aid ->
            if (aid.isNotEmpty()) {
                analyticsIdentifiers.add(
                    ID(
                        JSON_VALUE_NAMESPACE_ANALYTICS_AID,
                        aid,
                        JSON_VALUE_TYPE_INTEGRATION_CODE
                    )
                )
            }
        }

        // Visitor ID
        DataReader.optString(
            analyticsSharedState?.value,
            SharedStateKeys.Analytics.VISITOR_IDENTIFIER,
            null
        )?.also { vid ->
            if (vid.isNotEmpty()) {
                analyticsIdentifiers.add(
                    ID(
                        JSON_VALUE_NAMESPACE_USER_IDENTIFIER,
                        vid,
                        JSON_VALUE_TYPE_ANALYTICS
                    )
                )
            }
        }

        return analyticsIdentifiers
    }

    /**
     * Gets the required identities from Audience extension.
     *
     * @param event the event at which the shared state is to be retrieved
     * @param extensionApi the [ExtensionApi] used to retrieve the shared state
     * @return a [List] of [ID]'s populated using Audience shared state
     */
    private fun getAudienceIdentifiers(
        event: Event,
        extensionApi: ExtensionApi
    ): List<ID> {
        val audienceSharedState =
            getSharedState(SharedStateKeys.Audience.EXTENSION_NAME, event, extensionApi)

        val audienceIdentifiers = mutableListOf<ID>()

        if (!isSharedStateSet(audienceSharedState)) return audienceIdentifiers

        // Data provider unique user id
        DataReader.optString(audienceSharedState?.value, SharedStateKeys.Audience.DPUUID, null)
            ?.also { dpuuid ->
                if (dpuuid.isNotEmpty()) {
                    val dpid = DataReader.optString(
                        audienceSharedState?.value,
                        SharedStateKeys.Audience.DPID,
                        ""
                    )
                    audienceIdentifiers.add(ID(dpid, dpuuid, JSON_VALUE_TYPE_NAMESPACE_ID))
                }
            }

        // Audience unique user id
        DataReader.optString(audienceSharedState?.value, SharedStateKeys.Audience.UUID, null)
            ?.also { uuid ->
                if (uuid.isNotEmpty()) {
                    audienceIdentifiers.add(
                        ID(
                            JSON_VALUE_NAMESPACE_AUDIENCE_UUID,
                            uuid,
                            JSON_VALUE_TYPE_NAMESPACE_ID
                        )
                    )
                }
            }

        return audienceIdentifiers
    }

    /**
     * Gets the required identities from Target extension.
     *
     * @param event the event at which the shared state is to be retrieved
     * @param extensionApi the [ExtensionApi] used to retrieve the shared state
     * @return a [List] of [ID]'s populated using Target shared state
     */
    private fun getTargetIdentifiers(
        event: Event,
        extensionApi: ExtensionApi
    ): List<ID> {
        val targetIdentifiers = mutableListOf<ID>()

        val targetSharedState =
            getSharedState(SharedStateKeys.Target.EXTENSION_NAME, event, extensionApi)

        if (!isSharedStateSet(targetSharedState)) return targetIdentifiers

        // Target user identifier
        DataReader.optString(
            targetSharedState?.value,
            SharedStateKeys.Target.TNT_ID,
            null
        )?.also { tntId ->
            if (tntId.isNotEmpty()) {
                targetIdentifiers.add(
                    ID(
                        JSON_VALUE_NAMESPACE_TARGET_TNTID,
                        tntId,
                        JSON_VALUE_TYPE_TARGET
                    )
                )
            }
        }

        // Target 3rd party ID
        DataReader.optString(
            targetSharedState?.value,
            SharedStateKeys.Target.THIRD_PARTY_ID,
            null
        )?.also { thirdPartyId ->
            if (thirdPartyId.isNotEmpty()) {
                targetIdentifiers.add(
                    ID(
                        JSON_VALUE_NAMESPACE_TARGET_THIRD_PARTY_ID,
                        thirdPartyId,
                        JSON_VALUE_TYPE_TARGET
                    )
                )
            }
        }

        return targetIdentifiers
    }

    /**
     * Gets the list of orgId's from Configuration extension.
     *
     * @param event the event at which the shared state is to be retrieved
     * @param extensionApi the [ExtensionApi] used to retrieve the shared state
     * @return a organization Id's as populated using Configuration shared state
     */
    private fun getCompanyContext(
        event: Event,
        extensionApi: ExtensionApi
    ): String? {
        var orgId: String? = null
        val configurationSharedState =
            getSharedState(SharedStateKeys.Configuration.EXTENSION_NAME, event, extensionApi)

        if (!isSharedStateSet(configurationSharedState)) {
            return orgId
        }

        DataReader.optString(
            configurationSharedState?.value,
            SharedStateKeys.Configuration.CONFIG_EXPERIENCE_CLOUD_ORG_ID,
            null
        )?.also { marketingCloudOrgId ->
            if (marketingCloudOrgId.isNotEmpty()) {
                orgId = marketingCloudOrgId
            }
        }

        return orgId
    }

    /**
     * Gets the shared state for the extension with name [name] at event [event]
     *
     * @param name the name of the extension for which the shared state is to be fetched
     * @param event the event at which the shared state is to be retrieved
     * @param extensionApi the [ExtensionApi] used to retrieve the shared state
     *
     * @return the [SharedStateResult] for the extension if available; null otherwise
     */
    private fun getSharedState(
        name: String,
        event: Event,
        extensionApi: ExtensionApi
    ): SharedStateResult? {
        return extensionApi.getSharedState(name, event, false, SharedStateResolution.ANY)
    }

    private fun isSharedStateSet(sharedState: SharedStateResult?): Boolean {
        return sharedState?.status == SharedStateStatus.SET
    }
}
