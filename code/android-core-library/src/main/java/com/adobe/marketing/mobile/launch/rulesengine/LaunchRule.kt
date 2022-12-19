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

import com.adobe.marketing.mobile.rulesengine.Evaluable
import com.adobe.marketing.mobile.rulesengine.Rule

/**
 * The data class representing the given [Evaluable] and a list of [RuleConsequence] objects.
 *
 * @property condition an object of [Evaluable]
 * @property consequenceList a list of [RuleConsequence] objects
 * @constructor Constructs a new [LaunchRule]
 */
data class LaunchRule(val condition: Evaluable, val consequenceList: List<RuleConsequence>) : Rule {
    override fun getEvaluable(): Evaluable {
        return condition
    }
}
