/*
  Copyright 2023 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.services.ui.alert

import com.adobe.marketing.mobile.services.ui.Alert

/**
 * Settings for an [Alert] presentation.
 * @param title the title of the alert
 * @param message the message of the alert
 * @param positiveButtonText the text for the positive button
 * @param negativeButtonText the text for the negative button
 */
class AlertSettings private constructor(
    val title: String,
    val message: String,
    val positiveButtonText: String?,
    val negativeButtonText: String?
) {

    class Builder {
        private var title: String = ""
        private var message: String = ""
        private var positiveButtonText: String? = null
        private var negativeButtonText: String? = null

        /**
         * Sets the title of the alert.
         * @param title the title of the alert
         */
        fun title(title: String) = apply { this.title = title }

        /**
         * Sets the message of the alert.
         * @param message the message of the alert
         */
        fun message(message: String) = apply { this.message = message }

        /**
         * Sets the text for the positive button.
         * @param positiveButtonText the text for the positive button
         */
        fun positiveButtonText(positiveButtonText: String) =
            apply { this.positiveButtonText = positiveButtonText }

        /**
         * Sets the text for the negative button.
         * @param negativeButtonText the text for the negative button
         */
        fun negativeButtonText(negativeButtonText: String) =
            apply { this.negativeButtonText = negativeButtonText }

        fun build() = run {
            if (positiveButtonText == null && negativeButtonText == null) {
                throw IllegalArgumentException("At least one button must be defined.")
            }

            AlertSettings(
                title,
                message,
                positiveButtonText,
                negativeButtonText
            )
        }
    }
}
