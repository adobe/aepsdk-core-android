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

package com.adobe.marketing.mobile.lifecycle;

/**
 * XDM Environment type enum definition. Supported values by the Android SDK are:
 *
 * <ul>
 *   <li>application
 * </ul>
 *
 * Other possible values, not supported at this time:
 *
 * <ul>
 *   <li>browser
 *   <li>iot
 *   <li>external
 *   <li>widget
 * </ul>
 */
@SuppressWarnings("unused")
enum XDMLifecycleEnvironmentTypeEnum {
    APPLICATION("application"); // Application

    private final String value;

    XDMLifecycleEnvironmentTypeEnum(final String enumValue) {
        this.value = enumValue;
    }

    @Override
    public String toString() {
        return value;
    }
}
