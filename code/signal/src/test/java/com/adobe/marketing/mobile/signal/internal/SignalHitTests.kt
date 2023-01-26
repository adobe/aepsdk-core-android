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

package com.adobe.marketing.mobile.signal.internal

import com.adobe.marketing.mobile.services.DataEntity
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test

class SignalHitTests {
    @Test
    fun `initialize SignalConsequence from DataEntity`() {
        val json = """
            {
              "contentType": "application/json",
              "body": "{\"key\":\"value\"}",
              "url": "https://www.postback.com",
              "timeout": 4
            }
        """.trimIndent()
        val dataEntity = DataEntity(json)
        val signalConsequence = SignalHit.from(dataEntity)
        assertEquals("application/json", signalConsequence.contentType)
        assertEquals("{\"key\":\"value\"}", signalConsequence.body)
        assertEquals("https://www.postback.com", signalConsequence.url)
        assertEquals(4, signalConsequence.timeout(0))
    }

    @Test
    fun `initialize DataEntity from SignalConsequence`() {
        val signalConsequence = SignalHit(
            "https://www.postback.com",
            "{\"key\":\"value\"}",
            "application/json",
            2
        )
        val dataEntity = signalConsequence.toDataEntity()
        val json = dataEntity.data
        val jsonObject = JSONObject(json)
        assertEquals("application/json", jsonObject.getString("contentType"))
        assertEquals("{\"key\":\"value\"}", jsonObject.getString("body"))
        assertEquals("https://www.postback.com", jsonObject.getString("url"))
        assertEquals(2, jsonObject.getInt("timeout"))
    }

    @Test
    fun `Return default timeout`() {
        val signalConsequence = SignalHit(
            "https://www.postback.com",
            "{\"key\":\"value\"}",
            "application/json",
            -1
        )
        assertEquals(2, signalConsequence.timeout(2))
    }
}
