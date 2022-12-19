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

/**
 * Class to represent a pair of Objects. May be used to return two Object results from a method.
 *
 * @param <T>
 * @param <S>
 */
class IdentityGenericPair<T, S> {

    private final T first;
    private final S second;

    IdentityGenericPair(final T first, final S second) {
        this.first = first;
        this.second = second;
    }

    T getFirst() {
        return first;
    }

    S getSecond() {
        return second;
    }
}
