/*
  Copyright 2024 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.services.ui.notification.templates

import android.content.Intent
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.services.ui.notification.PushTemplateConstants
import com.adobe.marketing.mobile.util.DataReader
import org.json.JSONArray
import org.json.JSONException

internal open class CarouselPushTemplate : AEPPushTemplate {
    // Optional, Determines how the carousel will be operated. Valid values are "auto" or "manual".
    // Default is "auto".
    internal var carouselOperationMode: String
        private set

    // Required, One or more Items in the carousel defined by the CarouselItem class
    internal var carouselItems = mutableListOf<CarouselItem>()
        private set

    // Required, "default" or "filmstrip"
    internal var carouselLayoutType: String
        private set

    // Contains the carousel items as a string
    internal var rawCarouselItems: String
        private set

    data class CarouselItem(
        // Required, URI to an image to be shown for the carousel item
        val imageUri: String,
        // Optional, caption to show when the carousel item is visible
        val captionText: String?,
        // Optional, URI to handle when the item is touched by the user. If no uri is provided for the item, adb_uri will be handled instead.
        val interactionUri: String?
    )

    protected constructor(data: Map<String, String>) : super(data) {
        carouselLayoutType = DataReader.optString(
            data, PushTemplateConstants.PushPayloadKeys.CAROUSEL_LAYOUT, null
        ) ?: throw IllegalArgumentException("Required field \"${PushTemplateConstants.PushPayloadKeys.CAROUSEL_LAYOUT}\" not found.")

        rawCarouselItems = DataReader.optString(
            data, PushTemplateConstants.PushPayloadKeys.CAROUSEL_ITEMS, null
        ) ?: throw IllegalArgumentException("Required field \"${PushTemplateConstants.PushPayloadKeys.CAROUSEL_ITEMS}\" not found.")

        carouselOperationMode = DataReader.optString(
            data,
            PushTemplateConstants.PushPayloadKeys.CAROUSEL_OPERATION_MODE,
            PushTemplateConstants.DefaultValues.AUTO_CAROUSEL_MODE
        )
        carouselItems = parseCarouselItemsFromString(rawCarouselItems)
    }

    constructor(intent: Intent) : super(intent) {
        val intentExtras =
            intent.extras ?: throw IllegalArgumentException("Intent extras are null")
        carouselOperationMode =
            intentExtras.getString(PushTemplateConstants.IntentKeys.CAROUSEL_OPERATION_MODE)
                ?: PushTemplateConstants.DefaultValues.AUTO_CAROUSEL_MODE
        carouselLayoutType =
            intentExtras.getString(PushTemplateConstants.IntentKeys.CAROUSEL_LAYOUT_TYPE)
                ?: PushTemplateConstants.DefaultValues.DEFAULT_MANUAL_CAROUSEL_MODE
        rawCarouselItems =
            intentExtras.getString(PushTemplateConstants.IntentKeys.CAROUSEL_ITEMS) ?: ""
        carouselItems = parseCarouselItemsFromString(rawCarouselItems)
    }

    companion object {
        private const val SELF_TAG = "CarouselPushTemplate"

        fun createCarouselPushTemplate(data: Map<String, String>): CarouselPushTemplate {
            val carouselOperationMode = DataReader.optString(
                data,
                PushTemplateConstants.PushPayloadKeys.CAROUSEL_OPERATION_MODE,
                PushTemplateConstants.DefaultValues.AUTO_CAROUSEL_MODE
            )
            return if (carouselOperationMode == PushTemplateConstants.DefaultValues.AUTO_CAROUSEL_MODE) {
                AutoCarouselPushTemplate(data)
            } else
                ManualCarouselPushTemplate(data)
        }

        private fun parseCarouselItemsFromString(carouselItemsString: String?): MutableList<CarouselItem> {
            val carouselItems = mutableListOf<CarouselItem>()
            if (carouselItemsString.isNullOrEmpty()) {
                Log.debug(
                    PushTemplateConstants.LOG_TAG,
                    SELF_TAG,
                    "No carousel items found in the push template."
                )
                return carouselItems
            }
            try {
                val jsonArray = JSONArray(carouselItemsString)
                for (i in 0 until jsonArray.length()) {
                    val item = jsonArray.getJSONObject(i)
                    val imageUri = item.getString(PushTemplateConstants.CarouselItemKeys.IMAGE)
                    val captionText =
                        item.optString(PushTemplateConstants.CarouselItemKeys.TEXT, "")
                    val interactionUri =
                        item.optString(PushTemplateConstants.CarouselItemKeys.URL, "")
                    carouselItems.add(
                        CarouselItem(
                            imageUri,
                            captionText,
                            interactionUri
                        )
                    )
                }
            } catch (e: JSONException) {
                Log.debug(
                    PushTemplateConstants.LOG_TAG,
                    SELF_TAG,
                    "Failed to parse carousel items from the push template: ${e.localizedMessage}"
                )
            }
            return carouselItems
        }
    }
}
