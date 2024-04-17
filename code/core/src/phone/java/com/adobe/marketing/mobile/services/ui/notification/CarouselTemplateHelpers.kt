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

package com.adobe.marketing.mobile.services.ui.notification

import android.content.Intent
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.util.StringUtils
import org.json.JSONArray
import org.json.JSONException

internal object CarouselTemplateHelpers {
    private const val SELF_TAG = "CarouselTemplateHelpers"

    /**
     * Calculates a new left, center, and right index given the current center index, total number
     * of images, and the intent action.
     *
     * @param centerIndex [Int] containing the current center image index
     * @param listSize `Int` containing the total number of images
     * @param action [String] containing the action found in the broadcast [Intent]
     * @return [List] containing the new calculated left, center, and right indices
     */
    internal fun calculateNewIndices(
        centerIndex: Int,
        listSize: Int?,
        action: String?
    ): List<Int> {
        if (listSize == null || listSize < CarouselPushTemplate.MINIMUM_FILMSTRIP_SIZE) return emptyList()
        val newIndices = mutableListOf<Int>()
        var newCenterIndex = 0
        var newLeftIndex = 0
        var newRightIndex = 0
        Log.trace(
            PushTemplateConstants.LOG_TAG,
            SELF_TAG,
            "Current center index is %d and list size is %d.",
            centerIndex,
            listSize
        )
        if ((action == PushTemplateConstants.IntentActions.FILMSTRIP_LEFT_CLICKED) || (action == PushTemplateConstants.IntentActions.MANUAL_CAROUSEL_LEFT_CLICKED)) {
            newCenterIndex = (centerIndex - 1 + listSize) % listSize
            newLeftIndex = (newCenterIndex - 1 + listSize) % listSize
            newRightIndex = centerIndex
        } else if ((
            (action == PushTemplateConstants.IntentActions.FILMSTRIP_RIGHT_CLICKED) || (
                action ==
                    PushTemplateConstants.IntentActions.MANUAL_CAROUSEL_RIGHT_CLICKED
                )
            )
        ) {
            newCenterIndex = (centerIndex + 1) % listSize
            newLeftIndex = centerIndex
            newRightIndex = (newCenterIndex + 1) % listSize
        }
        newIndices.add(newLeftIndex)
        newIndices.add(newCenterIndex)
        newIndices.add(newRightIndex)
        Log.trace(
            PushTemplateConstants.LOG_TAG,
            SELF_TAG,
            (
                "Calculated new indices. New center index is %d, new left index is %d, and new" +
                    " right index is %d."
                ),
            newCenterIndex,
            newLeftIndex,
            newRightIndex
        )
        return newIndices
    }

    internal fun parseCarouselItems(carouselItemsString: String?): MutableList<CarouselPushTemplate.CarouselItem> {
        val carouselItems = mutableListOf<CarouselPushTemplate.CarouselItem>()
        if (StringUtils.isNullOrEmpty(carouselItemsString)) {
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
                val captionText = item.optString(PushTemplateConstants.CarouselItemKeys.TEXT, "")
                val interactionUri = item.optString(PushTemplateConstants.CarouselItemKeys.URL, "")
                carouselItems.add(
                    CarouselPushTemplate.CarouselItem(
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
