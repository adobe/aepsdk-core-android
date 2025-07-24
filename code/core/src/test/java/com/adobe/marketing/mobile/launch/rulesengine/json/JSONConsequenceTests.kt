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
import kotlin.test.assertNull

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
        val attachedData = eventdata["attached_data"] as? Map<*, *>
        assertNotNull(attachedData)
        assertEquals(2, attachedData.size)
        assertEquals("value1", attachedData["key1"] as? String)
        assertEquals("{%~state.com.adobe.module.lifecycle/lifecyclecontextdata.launches%}", attachedData["launches"] as? String)
    }

    @Test
    fun testToRuleConsequence_NullId_ReturnsNull() {
        val jsonString = """
        {
          "id": null,
          "type": "add",
          "detail": {
            "eventdata": {
              "key": "value"
            }
          }
        }
        """
        val jsonObject = buildJSONObject(jsonString)
        val consequence = JSONConsequence(jsonObject)?.toRuleConsequence()
        assertNull(consequence)
    }

    @Test
    fun testToRuleConsequence_MissingId_ReturnsNull() {
        val jsonString = """
        {
          "type": "add",
          "detail": {
            "eventdata": {
              "key": "value"
            }
          }
        }
        """
        val jsonObject = buildJSONObject(jsonString)
        val consequence = JSONConsequence(jsonObject)?.toRuleConsequence()
        assertNull(consequence)
    }

    @Test
    fun testToRuleConsequence_EmptyId_ReturnsNull() {
        val jsonString = """
        {
          "id": "",
          "type": "add",
          "detail": {
            "eventdata": {
              "key": "value"
            }
          }
        }
        """
        val jsonObject = buildJSONObject(jsonString)
        val consequence = JSONConsequence(jsonObject)?.toRuleConsequence()
        assertNull(consequence)
    }

    @Test
    fun testToRuleConsequence_NullType_ReturnsNull() {
        val jsonString = """
        {
          "id": "valid-id",
          "type": null,
          "detail": {
            "eventdata": {
              "key": "value"
            }
          }
        }
        """
        val jsonObject = buildJSONObject(jsonString)
        val consequence = JSONConsequence(jsonObject)?.toRuleConsequence()
        assertNull(consequence)
    }

    @Test
    fun testToRuleConsequence_MissingType_ReturnsNull() {
        val jsonString = """
        {
          "id": "valid-id",
          "detail": {
            "eventdata": {
              "key": "value"
            }
          }
        }
        """
        val jsonObject = buildJSONObject(jsonString)
        val consequence = JSONConsequence(jsonObject)?.toRuleConsequence()
        assertNull(consequence)
    }

    @Test
    fun testToRuleConsequence_EmptyType_ReturnsNull() {
        val jsonString = """
        {
          "id": "valid-id",
          "type": "",
          "detail": {
            "eventdata": {
              "key": "value"
            }
          }
        }
        """
        val jsonObject = buildJSONObject(jsonString)
        val consequence = JSONConsequence(jsonObject)?.toRuleConsequence()
        assertNull(consequence)
    }

    @Test
    fun testToRuleConsequence_NullDetail_ReturnsNull() {
        val jsonString = """
        {
          "id": "valid-id",
          "type": "add",
          "detail": null
        }
        """
        val jsonObject = buildJSONObject(jsonString)
        val consequence = JSONConsequence(jsonObject)?.toRuleConsequence()
        assertNull(consequence)
    }

    @Test
    fun testToRuleConsequence_MissingDetail_ReturnsNull() {
        val jsonString = """
        {
          "id": "valid-id",
          "type": "add"
        }
        """
        val jsonObject = buildJSONObject(jsonString)
        val consequence = JSONConsequence(jsonObject)?.toRuleConsequence()
        assertNull(consequence)
    }

    @Test
    fun testToRuleConsequence_EmptyDetail_ReturnsNull() {
        val jsonString = """
        {
          "id": "valid-id",
          "type": "add",
          "detail": {}
        }
        """
        val jsonObject = buildJSONObject(jsonString)
        val consequence = JSONConsequence(jsonObject)?.toRuleConsequence()
        assertNull(consequence)
    }

    @Test
    fun testToRuleConsequence_AllFieldsInvalid_ReturnsNull() {
        val jsonString = """
        {
          "id": null,
          "type": null,
          "detail": null
        }
        """
        val jsonObject = buildJSONObject(jsonString)
        val consequence = JSONConsequence(jsonObject)?.toRuleConsequence()
        assertNull(consequence)
    }

    @Test
    fun testToRuleConsequence_ValidInputs_ReturnsRuleConsequence() {
        val jsonString = """
        {
          "id": "test-consequence-id",
          "type": "dispatch",
          "detail": {
            "type": "com.adobe.eventType.edge",
            "source": "com.adobe.eventSource.requestContent",
            "eventdataaction": "copy"
          }
        }
        """
        val jsonObject = buildJSONObject(jsonString)
        val consequence = JSONConsequence(jsonObject)?.toRuleConsequence()

        assertNotNull(consequence)
        assertEquals("test-consequence-id", consequence.id)
        assertEquals("dispatch", consequence.type)
        assertNotNull(consequence.detail)
        assertEquals(3, consequence.detail.size)
        assertEquals("com.adobe.eventType.edge", consequence.detail["type"])
        assertEquals("com.adobe.eventSource.requestContent", consequence.detail["source"])
        assertEquals("copy", consequence.detail["eventdataaction"])
    }
}
