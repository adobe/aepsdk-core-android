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

package com.adobe.marketing.mobile.services.ui

/**
 * A gating mechanism for implementers to restrict the display of a [Presentable] based on specific
 * set of conditions.
 */
interface PresentationLever {
    /**
     * Returns true if [presentable] can be shown, false otherwise.
     * @param presentable the [Presentable] to check if it can be shown
     * @return true if [presentable] can be shown, false otherwise
     */
    fun canShow(presentable: Presentable<*>): Boolean
}
