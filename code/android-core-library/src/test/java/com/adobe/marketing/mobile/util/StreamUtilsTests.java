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

package com.adobe.marketing.mobile.util;

import static org.junit.Assert.*;

import com.adobe.marketing.mobile.TestHelper;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.junit.Test;

public final class StreamUtilsTests {

    @Test
    public void testClassIsWellDefined() {
        try {
            TestHelper.assertUtilityClassWellDefined(StreamUtils.class);
        } catch (Exception e) {
            fail("StringUtils class is not well defined, throwing exception " + e);
        }
    }

    @Test
    public void testStreamToString_when_nullInputStream() {
        assertNull(StreamUtils.readAsString(null));
    }

    @Test
    public void testStreamToString_when_validInputStream() throws Exception {
        InputStream stream = new ByteArrayInputStream("myTestExample".getBytes("UTF-8"));
        assertEquals("myTestExample", StreamUtils.readAsString(stream));
    }
}
