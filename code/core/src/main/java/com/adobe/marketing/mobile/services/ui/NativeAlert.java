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

package com.adobe.marketing.mobile.services.ui;

public interface NativeAlert extends Presentable {

    /**
     * Gets the {@code NativeAlert} title text.
     *
     * @return {@link String} containing the {@link NativeAlert} title text.
     */
    String getTitle();

    /**
     * Gets the {@code NativeAlert} message body text.
     *
     * @return {@link String} containing the {@link NativeAlert} message body text.
     */
    String getMessage();

    /**
     * Gets the {@code NativeAlert} default button text.
     *
     * @return {@link String} containing the {@link NativeAlert} default button text.
     */
    String getDefaultButton();

    /**
     * Gets the {@code NativeAlert} default button url.
     *
     * @return {@link String} containing the {@link NativeAlert} default button url.
     */
    String getDefaultButtonUrl();

    /**
     * Gets the {@code NativeAlert} cancel button text.
     *
     * @return {@link String} containing the {@link NativeAlert} cancel button text.
     */
    String getCancelButton();

    /**
     * Gets the {@code NativeAlert} cancel button url.
     *
     * @return {@link String} containing the {@link NativeAlert} cancel button url.
     */
    String getCancelButtonUrl();

    /**
     * Gets the {@code NativeAlert} style.
     *
     * @return {@link String} containing the {@link NativeAlert} style.
     */
    String getStyle();
}
