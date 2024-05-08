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

import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.annotation.VisibleForTesting
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.services.ui.notification.templates.AEPPushTemplate
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

internal object PushTemplateUtils {
    private const val SELF_TAG = "PushTemplateUtils"

    /**
     * Returns the resource id for the drawable with the given name. The file must be in the
     * res/drawable directory. If the drawable file is not found, 0 is returned.
     *
     * @param iconName the name of the icon file
     * @param context the application [Context]
     * @return the resource id for the icon with the given name
     */
    internal fun getIconWithResourceName(
        iconName: String?,
        context: Context
    ): Int {
        return if (iconName.isNullOrEmpty()) {
            0
        } else context.resources.getIdentifier(iconName, "drawable", context.packageName)
    }

    /**
     * Checks if the icon is valid.
     *
     * @param icon the icon to be checked
     * @return true if the icon is valid, false otherwise
     */
    internal fun isValidIcon(icon: Int): Boolean {
        return icon > 0
    }

    internal fun getDefaultAppIcon(context: Context): Int {
        val packageName = context.packageName
        try {
            return context.packageManager.getApplicationInfo(packageName, 0).icon
        } catch (e: PackageManager.NameNotFoundException) {
            Log.warning(
                PushTemplateConstants.LOG_TAG,
                SELF_TAG,
                "Package manager NameNotFoundException while reading default application icon: ${e.localizedMessage}"
            )
        }
        return -1
    }

    /**
     * Returns the Uri for the sound file with the given name. The sound file must be in the res/raw
     * directory. The sound file should be in format of .mp3, .wav, or .ogg
     *
     * @param soundName [String] containing the name of the sound file
     * @param context the application [Context]
     * @return the [Uri] for the sound file with the given name
     */
    internal fun getSoundUriForResourceName(
        soundName: String?,
        context: Context
    ): Uri {
        return Uri.parse(
            ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" +
                context.packageName +
                "/raw/" +
                soundName
        )
    }

    internal fun getActionButtonsFromString(actionButtons: String?): List<AEPPushTemplate.ActionButton>? {
        if (actionButtons == null) {
            Log.debug(
                PushTemplateConstants.LOG_TAG,
                SELF_TAG,
                "Exception in converting actionButtons json string to json object, Error :" +
                    " actionButtons is null"
            )
            return null
        }
        val actionButtonList = mutableListOf<AEPPushTemplate.ActionButton>()
        try {
            val jsonArray = JSONArray(actionButtons)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val button = getActionButtonFromJSONObject(jsonObject) ?: continue
                actionButtonList.add(button)
            }
        } catch (e: JSONException) {
            Log.warning(
                PushTemplateConstants.LOG_TAG,
                SELF_TAG,
                "Exception in converting actionButtons json string to json object, Error : ${e.localizedMessage}"
            )
            return null
        }
        return actionButtonList
    }

    @VisibleForTesting
    internal fun getActionButtonFromJSONObject(jsonObject: JSONObject): AEPPushTemplate.ActionButton? {
        return try {
            val label = jsonObject.getString(PushTemplateConstants.ActionButtons.LABEL)
            if (label.isEmpty()) {
                Log.debug(
                    PushTemplateConstants.LOG_TAG,
                    SELF_TAG, "Label is empty"
                )
                return null
            }
            var uri: String? = null
            val type = jsonObject.getString(PushTemplateConstants.ActionButtons.TYPE)
            if (type == PushTemplateConstants.ActionType.WEBURL.name || type == PushTemplateConstants.ActionType.DEEPLINK.name) {
                uri = jsonObject.optString(PushTemplateConstants.ActionButtons.URI)
            }
            Log.trace(
                PushTemplateConstants.LOG_TAG,
                SELF_TAG,
                "Creating an ActionButton with label ($label), uri ($uri), and type ($type)."
            )
            AEPPushTemplate.ActionButton(label, uri, type)
        } catch (e: JSONException) {
            Log.warning(
                PushTemplateConstants.LOG_TAG,
                SELF_TAG,
                "Exception in converting actionButtons json string to json object, Error : ${e.localizedMessage}."
            )
            null
        }
    }
}
