/*
  Copyright 2023 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CloneFailedExceptionTests {

    @Test
    public void testConstructorWithStringMessage() {
        final CloneFailedException cloneFailedException = new CloneFailedException("Message");
        assertEquals("Message", cloneFailedException.getMessage());
        assertEquals(CloneFailedException.Reason.UNKNOWN, cloneFailedException.getReason());
    }

    @Test
    public void testConstructorWithReason() {
        final CloneFailedException cloneFailedMaxDepth =
                new CloneFailedException(CloneFailedException.Reason.MAX_DEPTH_REACHED);
        assertEquals(
                CloneFailedException.Reason.MAX_DEPTH_REACHED.toString(),
                cloneFailedMaxDepth.getMessage());
        assertEquals(
                CloneFailedException.Reason.MAX_DEPTH_REACHED, cloneFailedMaxDepth.getReason());

        final CloneFailedException cloneFailedUnsupportedType =
                new CloneFailedException(CloneFailedException.Reason.UNSUPPORTED_TYPE);
        assertEquals(
                CloneFailedException.Reason.UNSUPPORTED_TYPE.toString(),
                cloneFailedUnsupportedType.getMessage());
        assertEquals(
                CloneFailedException.Reason.UNSUPPORTED_TYPE,
                cloneFailedUnsupportedType.getReason());
    }
}
