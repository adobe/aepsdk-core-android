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

package com.adobe.marketing.mobile.launch.rulesengine

/**
 * The data class representing a rule's consequence object
 *
 * @property id the consequence id
 * @property type the consequence type
 * @property detail the meta data of the consequence object
 * @constructor Constructs a new [RuleConsequence]
 */

data class RuleConsequence(
    val id: String,
    val type: String,
    val detail: Map<String, Any?>?
)
