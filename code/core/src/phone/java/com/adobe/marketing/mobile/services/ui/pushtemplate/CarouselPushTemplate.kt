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

package com.adobe.marketing.mobile.services.ui.pushtemplate

import com.adobe.marketing.mobile.util.DataReader
import com.adobe.marketing.mobile.util.DataReaderException
import com.adobe.marketing.mobile.util.JSONUtils
import com.adobe.marketing.mobile.util.StringUtils
import org.json.JSONArray
import org.json.JSONException

internal class CarouselPushTemplate(messageData: MutableMap<String, String>) :
    AEPPushTemplate(messageData) {
    // Optional, Determines how the carousel will be operated. Valid values are "auto" or "manual".
    // Default is "auto".
    private val carouselOperationMode: String

    // Required, One or more Items in the carousel defined by the CarouselItem class
    private val carouselItems: ArrayList<CarouselItem> =
        ArrayList()

    // Required, "default" or "filmstrip"
    private var carouselLayoutType: String

    class CarouselItem(
        // Required, URI to an image to be shown for the carousel item
        val imageUri: String,
        // Optional, caption to show when the carousel item is visible
        val captionText: String?,
        // Optional, URI to handle when the item is touched by the user. If no uri is provided for the item, adb_uri will be handled instead.
        val interactionUri: String?
    )

    fun getCarouselOperationMode(): String {
        return carouselOperationMode
    }

    fun getCarouselLayoutType(): String {
        return carouselLayoutType
    }

    fun getCarouselItems(): ArrayList<CarouselItem> {
        return carouselItems
    }

    init {
        try {
            carouselLayoutType = DataReader.getString(
                messageData, PushTemplateConstants.PushPayloadKeys.CAROUSEL_LAYOUT
            )
        } catch (dataReaderException: DataReaderException) {
            throw IllegalArgumentException("Required field \"adb_car_layout\" not found.")
        }
        val carouselItemsString: String = try {
            DataReader.getString(
                messageData, PushTemplateConstants.PushPayloadKeys.CAROUSEL_ITEMS
            )
        } catch (dataReaderException: DataReaderException) {
            throw IllegalArgumentException("Required field \"adb_items\" not found.")
        }
        val carouselItemJSONArray: JSONArray
        val carouselItemObjects: MutableList<Any>?
        try {
            carouselItemJSONArray = JSONArray(carouselItemsString)
            carouselItemObjects = JSONUtils.toList(carouselItemJSONArray)
        } catch (exception: JSONException) {
            throw IllegalArgumentException(
                "Unable to create a JSONObject from the carousel items string."
            )
        }
        carouselOperationMode = DataReader.optString(
            messageData,
            PushTemplateConstants.PushPayloadKeys.CAROUSEL_OPERATION_MODE,
            PushTemplateConstants.DefaultValues.AUTO_CAROUSEL_MODE
        )
        carouselItemObjects?.let {
            for (carouselObject in carouselItemObjects) {
                val carouselItemMap = carouselObject as Map<String, String>
                // the image uri is required, do not create a CarouselItem if it is missing
                val carouselImage =
                    carouselItemMap[PushTemplateConstants.PushPayloadKeys.CAROUSEL_ITEM_IMAGE]
                if (StringUtils.isNullOrEmpty(carouselImage)) break
                carouselImage?.let {
                    val text =
                        carouselItemMap[PushTemplateConstants.PushPayloadKeys.CAROUSEL_ITEM_TEXT]
                    val uri =
                        carouselItemMap[PushTemplateConstants.PushPayloadKeys.CAROUSEL_ITEM_URI]
                    val carouselItem =
                        CarouselItem(
                            carouselImage,
                            text,
                            uri
                        )
                    carouselItems.add(carouselItem)
                }
            }
        }
    }
}
