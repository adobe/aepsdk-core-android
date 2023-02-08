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

package com.adobe.marketing.mobile.services.ui;

public class AlertSetting {

    private String title;
    private String message;
    private String positiveButtonText;
    private String negativeButtonText;

    private AlertSetting() {}

    /**
     * @return the alert title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return the alert message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return the positive response button text
     */
    public String getPositiveButtonText() {
        return positiveButtonText;
    }

    /**
     * @return the negative response button text
     */
    public String getNegativeButtonText() {
        return negativeButtonText;
    }

    /**
     * Build an {@link AlertSetting} instance used for showing an alert.
     *
     * @param title String alert title
     * @param message String alert message
     * @param positiveButtonText String positive response button text. Positive button will not be
     *     displayed if this value is null or empty
     * @param negativeButtonText String negative response button text. Negative button will not be
     *     displayed if this value is null or empty
     * @return An {@link AlertSetting} instance
     */
    public static AlertSetting build(
            final String title,
            final String message,
            final String positiveButtonText,
            final String negativeButtonText) {
        AlertSetting alertDetail = new AlertSetting();
        alertDetail.title = title;
        alertDetail.message = message;
        alertDetail.positiveButtonText = positiveButtonText;
        alertDetail.negativeButtonText = negativeButtonText;
        return alertDetail;
    }
}
