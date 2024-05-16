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

package com.adobe.marketing.mobile.services.ui.notification.extensions

import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.services.ui.notification.PushTemplateConstants

private const val SELF_TAG = "AppResourceExtensions"

/**
 * Returns the resource id for the drawable with the given name. The file must be in the
 * res/drawable directory. If the drawable file is not found, 0 is returned.
 *
 * @param iconName the name of the icon file
 * @return the resource id for the icon with the given name
 */
internal fun Context.getIconWithResourceName(
    iconName: String?
): Int {
    return if (iconName.isNullOrEmpty()) {
        0
    } else resources.getIdentifier(iconName, "drawable", packageName)
}

/**
 * Returns the default application icon.
 *
 * @return the resource id for the default application icon
 */
internal fun Context.getDefaultAppIcon(): Int {
    val packageName = packageName
    try {
        return packageManager.getApplicationInfo(packageName, 0).icon
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
 * @return the [Uri] for the sound file with the given name
 */
internal fun Context.getSoundUriForResourceName(
    soundName: String?
): Uri {
    return Uri.parse(
        ContentResolver.SCHEME_ANDROID_RESOURCE +
            "://" +
            packageName +
            "/raw/" +
            soundName
    )
}
