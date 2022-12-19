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

package com.adobe.marketing.mobile.launch.rulesengine.json

import com.adobe.marketing.mobile.test.util.buildJSONObject
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class JSONConsequenceTests {
    @Test
    fun testJSONConsequence() {
        val jsonString = """
        {
          "id": "RCa839e401f54a459a9049328f9b609a07",
          "type": "add",
          "detail": {
            "eventdata": {
              "attached_data": {
                "key1": "value1",
                "launches": "{%~state.com.adobe.module.lifecycle/lifecyclecontextdata.launches%}"
              }
            }
          }
        }   
        """

        val jsonObject = buildJSONObject(jsonString)
        val consequence = JSONConsequence(jsonObject)?.toRuleConsequence()
        assertEquals("RCa839e401f54a459a9049328f9b609a07", consequence?.id)
        assertEquals("add", consequence?.type)
        assertNotNull(consequence?.detail)
        assertEquals(1, consequence?.detail?.size)
        assertEquals(true, consequence?.detail?.containsKey("eventdata"))
        val eventdata = consequence?.detail?.get("eventdata") as? Map<*, *>
        assertNotNull(eventdata)
        assertEquals(1, eventdata.size)
        val attachedData = eventdata.get("attached_data") as? Map<*, *>
        assertNotNull(attachedData)
        assertEquals(2, attachedData.size)
        assertEquals("value1", attachedData["key1"] as? String)
        assertEquals("{%~state.com.adobe.module.lifecycle/lifecyclecontextdata.launches%}", attachedData["launches"] as? String)
    }
}
