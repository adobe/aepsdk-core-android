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

package com.adobe.marketing.mobile.identity;

import java.util.List;

/** Class to represent IdentityExtension network call json response. */
class IdentityResponseObject {

    /**
     * Blob value as received in the visitor id service network response json.
     *
     * <p>Expected value type: {@link String}
     */
    String blob;

    /**
     * Marketing cloud id value as received in the visitor id service network response json.
     *
     * <p>Expected value type: {@link String}
     */
    String mid;

    /**
     * Location value as received in the visitor id service network response json.
     *
     * <p>Expected value type: {@link String}
     */
    String hint;

    /**
     * Error value as received in the visitor id service network response json.
     *
     * <p>Expected value type: {@link String}
     */
    String error;

    /**
     * ttl value as received in the visitor id service network response json.
     *
     * <p>Expected value type: {@link Long}
     */
    long ttl;

    /**
     * ArrayList of global opt out as received in the visitor id service network response json.
     *
     * <p>Expected value type: {@code List}
     */
    List<String> optOutList;

    /**
     * Constructor initializes blob, mid, hint, error, optOutList to null, and ttl to its defualt
     * value of DEFAULT_TTL_VALUE.
     */
    IdentityResponseObject() {
        ttl = IdentityConstants.Defaults.DEFAULT_TTL_VALUE;
    }
}
