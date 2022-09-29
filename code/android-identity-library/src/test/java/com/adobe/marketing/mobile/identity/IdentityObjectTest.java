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

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;


public class IdentityObjectTest {

    IdentityResponseObject identityResponseObject;

    @Before
    public void setup() {
        identityResponseObject = new IdentityResponseObject();
    }

    @Test
    public void testSetBlob_Should_SetBlob() {
        identityResponseObject.blob = "blob_this";
        assertEquals("blob_this", identityResponseObject.blob);
    }

    @Test
    public void testSetMid_Should_SetMid() {
        identityResponseObject.mid = "mid_this";
        assertEquals("mid_this", identityResponseObject.mid);
    }

    @Test
    public void testSetHint_Should_SetHint() {
        identityResponseObject.hint = "hint_this";
        assertEquals("hint_this", identityResponseObject.hint);
    }

    @Test
    public void testSetError_Should_SetError() {
        identityResponseObject.error = "error_this";
        assertEquals("error_this", identityResponseObject.error);
    }

    @Test
    public void testSetTTl_Should_SetTTL() {
        identityResponseObject.ttl = 100;
        assertEquals(100, identityResponseObject.ttl);
    }

    @Test
    public void testSetOptOutList_Should_SetOptOut() {

        ArrayList<String> list = new ArrayList<String>() {
            {
                add("abc");
                add("def");
            }
        };

        identityResponseObject.optOutList = list;
        assertEquals(2, identityResponseObject.optOutList.size());
    }


}


