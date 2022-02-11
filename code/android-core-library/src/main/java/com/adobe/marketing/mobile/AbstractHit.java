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

package com.adobe.marketing.mobile;

import java.io.Serializable;

/**
 * Base for any class that will represent a database record.
 * <p>
 * Extenders of this class will be used in conjunction with an implementation of {@link AbstractHitsDatabase}.
 * <p>
 * The base class includes the following fields:
 * <ul>
 *     <li>{@link String} identifier</li>
 *     <li>{@code long} timestamp</li>
 * </ul>
 */
abstract class AbstractHit {
	String identifier;
	long timestamp;
}