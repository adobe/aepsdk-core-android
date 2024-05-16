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

internal class ProductCatalogPushTemplate : AEPPushTemplate {
    // Required, Text to be shown on the CTA button
    internal var ctaButtonText: String
        private set

    // Required, Color for the CTA button. Represented as six character hex, e.g. 00FF00
    internal var ctaButtonColor: String
        private set

    // Required, URI to be handled when the user clicks the CTA button
    internal var ctaButtonUri: String
        private set

    // Required, Determines if the layout of the catalog goes left-to-right or top-to-bottom.
    // Value will either be "horizontal" (left-to-right) or "vertical" (top-to-bottom).
    internal var displayLayout: String
        private set

    // Required, Three entries describing the items in the product catalog.
    // The value is an encoded JSON string.
    internal var rawCatalogItems: String
        private set

    // Required, One or more items in the product catalog defined by the CatalogItem class
    internal var catalogItems = mutableListOf<CatalogItem>()
        private set

    internal var currentIndex: Int = PushTemplateConstants.DefaultValues.PRODUCT_CATALOG_START_INDEX

    data class CatalogItem(
        // Required, Text to use in the title if this product is selected
        val title: String,

        // Required, Text to use in the body if this product is selected
        val body: String,

        // Required, URI to an image to use in notification when this product is selected
        val img: String,

        // Required, Price of this product to display when the notification is selected
        val price: String,

        // Required, URI to be handled when the user clicks the large image of the selected item
        val uri: String
    )

    constructor(data: Map<String, String>) : super(data) {
        ctaButtonText = DataReader.optString(
            data, PushTemplateConstants.PushPayloadKeys.CATALOG_CTA_BUTTON_TEXT, null
        )
            ?: throw IllegalArgumentException("Required field \"${PushTemplateConstants.PushPayloadKeys.CATALOG_CTA_BUTTON_TEXT}\" not found.")
        ctaButtonColor = DataReader.optString(
            data, PushTemplateConstants.PushPayloadKeys.CATALOG_CTA_BUTTON_COLOR, null
        )
            ?: throw IllegalArgumentException("Required field \"${PushTemplateConstants.PushPayloadKeys.CATALOG_CTA_BUTTON_COLOR}\" not found.")
        ctaButtonUri = DataReader.optString(
            data, PushTemplateConstants.PushPayloadKeys.CATALOG_CTA_BUTTON_URI, null
        )
            ?: throw IllegalArgumentException("Required field \"${PushTemplateConstants.PushPayloadKeys.CATALOG_CTA_BUTTON_URI}\" not found.")
        displayLayout = DataReader.optString(
            data, PushTemplateConstants.PushPayloadKeys.CATALOG_LAYOUT, null
        )
            ?: throw IllegalArgumentException("Required field \"${PushTemplateConstants.PushPayloadKeys.CATALOG_LAYOUT}\" not found.")
        rawCatalogItems = DataReader.optString(
            data, PushTemplateConstants.PushPayloadKeys.CATALOG_ITEMS, null
        )
            ?: throw IllegalArgumentException("Required field \"${PushTemplateConstants.PushPayloadKeys.CATALOG_ITEMS}\" not found.")
        catalogItems = parseCatalogItemsFromString(rawCatalogItems)
    }

    constructor(intent: Intent) : super(intent) {
        val buttonText =
            intent.getStringExtra(PushTemplateConstants.IntentKeys.CATALOG_CTA_BUTTON_TEXT)
        ctaButtonText = buttonText
            ?: throw IllegalArgumentException("Required field \"${PushTemplateConstants.IntentKeys.CATALOG_CTA_BUTTON_TEXT}\" not found.")
        val buttonColor =
            intent.getStringExtra(PushTemplateConstants.IntentKeys.CATALOG_CTA_BUTTON_COLOR)
        ctaButtonColor = buttonColor
            ?: throw IllegalArgumentException("Required field \"${PushTemplateConstants.IntentKeys.CATALOG_CTA_BUTTON_COLOR}\" not found.")
        val buttonUri =
            intent.getStringExtra(PushTemplateConstants.IntentKeys.CATALOG_CTA_BUTTON_URI)
        ctaButtonUri = buttonUri
            ?: throw IllegalArgumentException("Required field \"${PushTemplateConstants.IntentKeys.CATALOG_CTA_BUTTON_URI}\" not found.")
        val layout = intent.getStringExtra(PushTemplateConstants.IntentKeys.CATALOG_LAYOUT)
        displayLayout = layout
            ?: throw IllegalArgumentException("Required field \"${PushTemplateConstants.IntentKeys.CATALOG_LAYOUT}\" not found.")
        val items = intent.getStringExtra(PushTemplateConstants.IntentKeys.CATALOG_ITEMS)
        rawCatalogItems = items
            ?: throw IllegalArgumentException("Required field \"${PushTemplateConstants.IntentKeys.CATALOG_ITEMS}\" not found.")
        catalogItems = parseCatalogItemsFromString(rawCatalogItems)
        currentIndex = intent.getIntExtra(
            PushTemplateConstants.IntentKeys.CATALOG_ITEM_INDEX,
            PushTemplateConstants.DefaultValues.PRODUCT_CATALOG_START_INDEX
        )
    }

    companion object {
        private const val SELF_TAG = "ProductCatalogPushTemplate"

        private fun parseCatalogItemsFromString(catalogItemsString: String?): MutableList<CatalogItem> {
            val catalogItems = mutableListOf<CatalogItem>()
            val jsonArray: JSONArray?
            try {
                jsonArray = JSONArray(catalogItemsString)
            } catch (e: JSONException) {
                Log.error(
                    PushTemplateConstants.LOG_TAG,
                    SELF_TAG,
                    "Exception occurred when creating json array from the catalog items string: ${e.localizedMessage}"
                )
                throw IllegalArgumentException("Catalog items string containing a valid json array was not found.")
            }

            // fast fail if the array is not the expected size
            if (jsonArray.length() != 3) {
                throw IllegalArgumentException("3 catalog items are required for a Product Catalog notification.")
            }

            for (i in 0 until jsonArray.length()) {
                try {
                    val item = jsonArray.getJSONObject(i)
                    // all values are required for a catalog item. if any are missing we have an invalid catalog item and we know the notification as a whole is invalid
                    // as three catalog items are required.
                    val title = item.getString(PushTemplateConstants.CatalogItemKeys.TITLE)
                    val body =
                        item.getString(PushTemplateConstants.CatalogItemKeys.BODY)
                    val image =
                        item.getString(PushTemplateConstants.CatalogItemKeys.IMAGE)
                    val price =
                        item.getString(PushTemplateConstants.CatalogItemKeys.PRICE)
                    val uri = item.getString(PushTemplateConstants.CatalogItemKeys.URI)

                    catalogItems.add(
                        CatalogItem(
                            title,
                            body,
                            image,
                            price,
                            uri
                        )
                    )
                } catch (e: JSONException) {
                    Log.error(
                        PushTemplateConstants.LOG_TAG,
                        SELF_TAG,
                        "Failed to parse catalog item at index $i: ${e.localizedMessage}"
                    )
                    throw IllegalArgumentException("3 catalog items are required for a Product Catalog notification.")
                }
            }
            return catalogItems
        }
    }
}
