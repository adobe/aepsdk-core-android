/*
  Copyright 2024 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StringEncoderTests {
    @Test
    fun `Test sha256 hashing with different inputs`() {
        assertEquals(
            "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824",
            StringEncoder.sha2hash("hello")
        )
        assertEquals(
            "5e3235a8346e5a4585f8c58562f5052b8fe26a3bb122e1e96c76784964dfc461",
            StringEncoder.sha2hash("hello ")
        )
        assertEquals(
            "61c2ba73ffe637de9d0be2dbb4d466d6d44acdd5d689730c5cfb62e5dd2fceb0",
            StringEncoder.sha2hash(" hello ")
        )
        assertEquals(
            "2becfab876e74d11384fc1a733a06c8c7ef925d26ffac331db502c55c6f12175",
            StringEncoder.sha2hash("aep sdk")
        )
    }

    @Test
    fun `Test sha2hash returns null on invalid input`() {
        assertNull(StringEncoder.sha2hash(""))
        assertNull(StringEncoder.sha2hash(null))
    }
}
